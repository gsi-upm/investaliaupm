package gsi.investalia.server.wade;

import java.util.ArrayList;
import java.util.List;

import gsi.investalia.domain.User;
import gsi.investalia.domain.Tag;
import gsi.investalia.json.JSONAdapter;
import gsi.investalia.server.db.HsqldbInterface;
import jade.lang.acl.ACLMessage;

import jade.lang.acl.MessageTemplate;

import com.tilab.wade.performer.layout.TransitionLayout;
import com.tilab.wade.performer.layout.MarkerLayout;
import com.tilab.wade.performer.layout.ActivityLayout;
import com.tilab.wade.performer.layout.WorkflowLayout;
import com.tilab.wade.performer.CodeExecutionBehaviour;
import com.tilab.wade.performer.Transition;
import com.tilab.wade.performer.WorkflowBehaviour;

@WorkflowLayout(entryPoint = @MarkerLayout(position = "(211,88)", activityName = "WaitForNewUser"), activities = { @ActivityLayout(position = "(688,27)", name = "NewUserSuccesful"), @ActivityLayout(position = "(550,84)", name = "CheckNewUser"), @ActivityLayout(position = "(644,193)", name = "NewUserFailure"), @ActivityLayout(position = "(349,78)", name = "WaitForNewUser") })
public class NewUserWorkflow extends WorkflowBehaviour {

	private ACLMessage aclMessage;
	private User loggedUser;
	

	/* Activities for this workflow */
	public static final String WAITFORNEWUSER_ACTIVITY = "WaitForNewUser";
	public static final String NEWUSERFAILURE_ACTIVITY = "NewUserFailure";
	public static final String CHECKNEWUSER_ACTIVITY = "CheckNewUser";
	public static final String NEWUSERSUCCESFUL_ACTIVITY = "NewUserSuccesful";

	/* Conditions for the transitions */
	public static final String CORRECTUSER_CONDITION = "CorrectLogin";
	public static final String WRONGUSER_CONDITION = "WrongLogin";
	public static final String MESSAGERECEIVED_CONDITION = "MessageReceived";

	private boolean newUserMessage, succesfulNewUser, unsuccesfulNewUser;

	private void defineActivities() {
		CodeExecutionBehaviour waitForLoginActivity = new CodeExecutionBehaviour(
				WAITFORNEWUSER_ACTIVITY, this);
		registerActivity(waitForLoginActivity, INITIAL);
		CodeExecutionBehaviour loginFailureActivity = new CodeExecutionBehaviour(
				NEWUSERFAILURE_ACTIVITY, this);
		registerActivity(loginFailureActivity);
		CodeExecutionBehaviour checkLoginActivity = new CodeExecutionBehaviour(
				CHECKNEWUSER_ACTIVITY, this);
		registerActivity(checkLoginActivity);
		CodeExecutionBehaviour loginSuccesfulActivity = new CodeExecutionBehaviour(
				NEWUSERSUCCESFUL_ACTIVITY, this);
		registerActivity(loginSuccesfulActivity);
		

	}

	private void defineTransitions() {
		registerTransition(new Transition(MESSAGERECEIVED_CONDITION, this),
				WAITFORNEWUSER_ACTIVITY, CHECKNEWUSER_ACTIVITY);
		registerTransition(new Transition(CORRECTUSER_CONDITION, this),
				CHECKNEWUSER_ACTIVITY, NEWUSERSUCCESFUL_ACTIVITY);
		registerTransition(new Transition(WRONGUSER_CONDITION, this),
				CHECKNEWUSER_ACTIVITY, NEWUSERFAILURE_ACTIVITY);
		registerTransition(new Transition(),NEWUSERSUCCESFUL_ACTIVITY,WAITFORNEWUSER_ACTIVITY);
		registerTransition(new Transition(),NEWUSERFAILURE_ACTIVITY,WAITFORNEWUSER_ACTIVITY);

	}

	protected void executeWaitForNewUser() throws Exception {
		aclMessage = myAgent.blockingReceive();
		if (aclMessage != null && aclMessage.getPerformative() == ACLMessage.CFP) {
			/* We have received the login message */
			newUserMessage = true;
		}

	}

	protected void executeCheckNewUser() throws Exception {
	
		// Json to User
		String content = aclMessage.getContent();
		loggedUser = JSONAdapter.JSONToUser(content);
		
		//TODO: aquí habría que añadir el usuario a la base de datos. 
		//Si la inserción ha sido correcta, poner succesfulNewUser a true
		//Si no, poner unsuccesfulNewUser a true
		
		//TODO: BORRAR LA LINEA SIGUIENTE CUANDO ESTÉ HECHO LO ANTERIOR!!!!
		unsuccesfulNewUser = true;
	}
	


	protected void executeNewUserFailure() throws Exception {
		System.out.println("New user failure");
		ACLMessage loginFailure = aclMessage.createReply();
		loginFailure.setPerformative(ACLMessage.DISCONFIRM);
		myAgent.send(loginFailure);
	}
	
	protected void executeNewUserSuccesful() throws Exception {
		System.out.println("new user successful");
		
		// Log
		System.out.println("new user: " + loggedUser.getUserName());
		System.out.println("new password: " + loggedUser.getPassword());
		
		ACLMessage loginSuccesful = aclMessage.createReply();
		loginSuccesful.setPerformative(ACLMessage.AGREE);
		loginSuccesful.setContent(JSONAdapter.userToJSON(loggedUser).toString());
		myAgent.send(loginSuccesful);
	}

	protected boolean checkMessageReceived() throws Exception{
		return newUserMessage;
	}

	protected boolean checkCorrectLogin() throws Exception{
		return succesfulNewUser;
	}
	
	protected boolean checkWrongLogin() throws Exception{
		return unsuccesfulNewUser;
	}
}
