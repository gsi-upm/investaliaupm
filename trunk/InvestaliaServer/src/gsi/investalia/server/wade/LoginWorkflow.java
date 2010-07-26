package gsi.investalia.server.wade;

import gsi.investalia.domain.User;
import gsi.investalia.json.JSONAdapter;
import jade.lang.acl.ACLMessage;

import jade.lang.acl.MessageTemplate;

import com.tilab.wade.performer.layout.TransitionLayout;
import com.tilab.wade.performer.layout.MarkerLayout;
import com.tilab.wade.performer.layout.ActivityLayout;
import com.tilab.wade.performer.layout.WorkflowLayout;
import com.tilab.wade.performer.CodeExecutionBehaviour;
import com.tilab.wade.performer.Transition;
import com.tilab.wade.performer.WorkflowBehaviour;

@WorkflowLayout(transitions = { @TransitionLayout(routingpoints = "(310,146)", to = "WaitForLogin", from = "LoginFailure"), @TransitionLayout(routingpoints = "(117,224)", to = "WaitForLogin", from = "INITIAL") }, entryPoint = @MarkerLayout(position = "(31,203)", activityName = "WaitForLogin"), activities = { @ActivityLayout(position = "(478,252)", name = "LoginSuccesful"), @ActivityLayout(position = "(466,121)", name = "LoginFailure"), @ActivityLayout(size = "(135,50)", position = "(292,173)", name = "CheckLogin"), @ActivityLayout(size = "(122,50)", position = "(118,194)", name = "WaitForLogin") })
public class LoginWorkflow extends WorkflowBehaviour {

	ACLMessage login;

	/* Activities for this workflow */
	public static final String WAITFORLOGIN_ACTIVITY = "WaitForLogin";
	public static final String LOGINFAILURE_ACTIVITY = "LoginFailure";
	public static final String CHECKLOGIN_ACTIVITY = "CheckLogin";
	public static final String LOGINSUCCESFUL_ACTIVITY = "LoginSuccesful";
	public static final String RECOMMENDATION_ACTIVITY = "Recommendation";

	/* Conditions for the transitions */
	public static final String CORRECTLOGIN_CONDITION = "CorrectLogin";
	public static final String WRONGLOGIN_CONDITION = "WrongLogin";
	public static final String MESSAGERECEIVED_CONDITION = "MessageReceived";

	private boolean loginMessage, succesfulLogin , unsuccesfulLogin;

	private void defineActivities() {
		CodeExecutionBehaviour waitForLoginActivity = new CodeExecutionBehaviour(
				WAITFORLOGIN_ACTIVITY, this);
		registerActivity(waitForLoginActivity, INITIAL);
		CodeExecutionBehaviour loginFailureActivity = new CodeExecutionBehaviour(
				LOGINFAILURE_ACTIVITY, this);
		registerActivity(loginFailureActivity);
		CodeExecutionBehaviour checkLoginActivity = new CodeExecutionBehaviour(
				CHECKLOGIN_ACTIVITY, this);
		registerActivity(checkLoginActivity);
		CodeExecutionBehaviour loginSuccesfulActivity = new CodeExecutionBehaviour(
				LOGINSUCCESFUL_ACTIVITY, this);
		registerActivity(loginSuccesfulActivity);
		CodeExecutionBehaviour recommendationActivity = new CodeExecutionBehaviour(
				RECOMMENDATION_ACTIVITY, this);
		registerActivity(recommendationActivity);

	}

	private void defineTransitions() {
		registerTransition(new Transition(MESSAGERECEIVED_CONDITION, this),
				WAITFORLOGIN_ACTIVITY, CHECKLOGIN_ACTIVITY);
		registerTransition(new Transition(CORRECTLOGIN_CONDITION, this),
				CHECKLOGIN_ACTIVITY, LOGINSUCCESFUL_ACTIVITY);
		registerTransition(new Transition(), CHECKLOGIN_ACTIVITY,
				LOGINFAILURE_ACTIVITY);
		registerTransition(new Transition(), LOGINFAILURE_ACTIVITY,
				WAITFORLOGIN_ACTIVITY);
		registerTransition(new Transition(MESSAGERECEIVED_CONDITION,this), 
				LOGINSUCCESFUL_ACTIVITY, RECOMMENDATION_ACTIVITY);
		registerTransition(new Transition(), RECOMMENDATION_ACTIVITY,
				WAITFORLOGIN_ACTIVITY);

	}

	protected void executeWaitForLogin() throws Exception {
		login = myAgent.blockingReceive();
		if (login != null && login.getPerformative() == ACLMessage.CFP) {
			/* We have received the login message */
			loginMessage = true;
		}

	}

	protected void executeCheckLogin() throws Exception {
	
		/* JsontoUser */
		String content = login.getContent();
		User user = JSONAdapter.JSONToUser(content);
		String userName = user.getUserName();
		String password = user.getPassword();
		int lastUpdate = user.getLastUpdate();
	
		
		
		boolean checkPasswordLogin = false;
		//TODO: check if login and password match ( checkPasswordLogin would be true)
		
		if(checkPasswordLogin){
			succesfulLogin = true;
		}else{
			unsuccesfulLogin = true;
		}
		
	}
	
	protected void executeRecommendation() throws Exception {
		//TODO : Update the database with new recommendations , or something similar

	}

	protected void executeLoginFailure() throws Exception {
		ACLMessage loginFailure = login.createReply();
		loginFailure.setPerformative(ACLMessage.REJECT_PROPOSAL);
		myAgent.send(loginFailure);
	}
	
	protected void executeLoginSuccesful() throws Exception {
		//TODO: Call the database to send the new messages
	}

	protected boolean checkMessageReceived() throws Exception{
		return loginMessage;
	}

	protected boolean checkCorrectLogin() throws Exception{
		return succesfulLogin;
	}
	
	protected boolean checkWrongLogin() throws Exception{
		return unsuccesfulLogin;
	}
}
