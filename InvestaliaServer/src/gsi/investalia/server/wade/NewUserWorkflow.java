package gsi.investalia.server.wade;


import gsi.investalia.domain.User;
import gsi.investalia.json.JSONAdapter;
import gsi.investalia.server.db.MysqlInterface;
import jade.lang.acl.ACLMessage;

import com.tilab.wade.performer.layout.MarkerLayout;
import com.tilab.wade.performer.layout.ActivityLayout;
import com.tilab.wade.performer.layout.WorkflowLayout;
import com.tilab.wade.performer.CodeExecutionBehaviour;
import com.tilab.wade.performer.Transition;
import com.tilab.wade.performer.WorkflowBehaviour;

@WorkflowLayout(entryPoint = @MarkerLayout(position = "(211,88)", activityName = "WaitForNewUser"), activities = {
		@ActivityLayout(position = "(688,27)", name = "NewUserSuccessful"),
		@ActivityLayout(position = "(550,84)", name = "CheckNewUser"),
		@ActivityLayout(position = "(644,193)", name = "NewUserFailure"),
		@ActivityLayout(position = "(349,78)", name = "WaitForNewUser") })
public class NewUserWorkflow extends WorkflowBehaviour {

	private ACLMessage aclMessage;

	/* Activities for this workflow */
	public static final String WAITFORNEWUSER_ACTIVITY = "WaitForNewUser";
	public static final String NEWUSERFAILURE_ACTIVITY = "NewUserFailure";
	public static final String CHECKNEWUSER_ACTIVITY = "CheckNewUser";
	public static final String NEWUSERSUCCESSFUL_ACTIVITY = "NewUserSuccessful";

	/* Conditions for the transitions */
	public static final String CORRECTUSER_CONDITION = "CorrectLogin";
	public static final String WRONGUSER_CONDITION = "WrongLogin";
	public static final String MESSAGERECEIVED_CONDITION = "MessageReceived";

	private boolean newUserMessage, successfulNewUser;

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
		CodeExecutionBehaviour loginSuccessfulActivity = new CodeExecutionBehaviour(
				NEWUSERSUCCESSFUL_ACTIVITY, this);
		registerActivity(loginSuccessfulActivity);
	}

	private void defineTransitions() {
		registerTransition(new Transition(MESSAGERECEIVED_CONDITION, this),
				WAITFORNEWUSER_ACTIVITY, CHECKNEWUSER_ACTIVITY);
		registerTransition(new Transition(CORRECTUSER_CONDITION, this),
				CHECKNEWUSER_ACTIVITY, NEWUSERSUCCESSFUL_ACTIVITY);
		registerTransition(new Transition(WRONGUSER_CONDITION, this),
				CHECKNEWUSER_ACTIVITY, NEWUSERFAILURE_ACTIVITY);
		registerTransition(new Transition(), NEWUSERSUCCESSFUL_ACTIVITY,
				WAITFORNEWUSER_ACTIVITY);
		registerTransition(new Transition(), NEWUSERFAILURE_ACTIVITY,
				WAITFORNEWUSER_ACTIVITY);
	}

	protected void executeWaitForNewUser() throws Exception {
		aclMessage = myAgent.blockingReceive();
		if (aclMessage != null
				&& aclMessage.getPerformative() == ACLMessage.CFP) {
			/* We have received the message */
			newUserMessage = true;
		}
	}

	protected void executeCheckNewUser() throws Exception {

		// Json to User
		String content = aclMessage.getContent();
		User newUser = JSONAdapter.JSONToUser(content);

		// Insert into database
		successfulNewUser = MysqlInterface.saveNewUser(newUser);
	}

	protected void executeNewUserFailure() throws Exception {
		System.out.println("New user failure");
		ACLMessage reply = aclMessage.createReply();
		reply.setPerformative(ACLMessage.DISCONFIRM);
		myAgent.send(reply);
	}

	protected void executeNewUserSuccessful() throws Exception {
		System.out.println("New user successful");
		ACLMessage reply = aclMessage.createReply();
		reply.setPerformative(ACLMessage.AGREE);
		myAgent.send(reply);
	}

	protected boolean checkMessageReceived() throws Exception {
		return newUserMessage;
	}

	protected boolean checkCorrectLogin() throws Exception {
		return successfulNewUser;
	}

	protected boolean checkWrongLogin() throws Exception {
		return !successfulNewUser;
	}
}
