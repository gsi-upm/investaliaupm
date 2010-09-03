package gsi.investalia.server.wade;

import gsi.investalia.domain.User;
import gsi.investalia.json.JSONAdapter;
import gsi.investalia.server.db.HsqldbInterface;
import jade.lang.acl.ACLMessage;

import com.tilab.wade.performer.layout.TransitionLayout;
import com.tilab.wade.performer.layout.MarkerLayout;
import com.tilab.wade.performer.layout.ActivityLayout;
import com.tilab.wade.performer.layout.WorkflowLayout;
import com.tilab.wade.performer.CodeExecutionBehaviour;
import com.tilab.wade.performer.Transition;
import com.tilab.wade.performer.WorkflowBehaviour;

@WorkflowLayout(entryPoint = @MarkerLayout(position = "(58,56)", activityName = "WaitForLogin"), activities = { @ActivityLayout(position = "(283,143)", name = "CheckUpdate"), @ActivityLayout(position = "(395,254)", name = "UpdateFailure"), @ActivityLayout(position = "(165,144)", name = "WaitForUpdate"), @ActivityLayout(position = "(406,11)", name = "UpdateSuccessful"), @ActivityLayout(position = "(336,71)", name = "Refresh"), @ActivityLayout(position = "(137,69)", name = "WaitForProposal"), @ActivityLayout(position = "(195,34)", name = "LoginSuccesful"), @ActivityLayout(position = "(207,121)", name = "CheckLogin"), @ActivityLayout(position = "(85,151)", name = "WaitForLogin"), @ActivityLayout(position = "(333,199)", name = "LoginFailure") })
public class UpdateUserWorkflow extends WorkflowBehaviour {

	private ACLMessage aclMessage;
	private User updateUser;
	

	/* Activities for this workflow */
	public static final String WAITFORUPDATE_ACTIVITY = "WaitForUpdate";
	public static final String UPDATEFAILURE_ACTIVITY = "UpdateFailure";
	public static final String CHECKUPDATE_ACTIVITY = "CheckUpdate";
	public static final String UPDATESUCCESSFUL_ACTIVITY = "UpdateSuccessful";

	/* Conditions for the transitions */
	public static final String CORRECTUPDATE_CONDITION = "CorrectUpdate";
	public static final String WRONGUPDATE_CONDITION = "WrongUpdate";
	public static final String MESSAGERECEIVED_CONDITION = "MessageReceived";

	private boolean messageReceived, successfulUpdate;

	private void defineActivities() {
		CodeExecutionBehaviour waitForLoginActivity = new CodeExecutionBehaviour(
				WAITFORUPDATE_ACTIVITY, this);
		registerActivity(waitForLoginActivity, INITIAL);
		CodeExecutionBehaviour loginFailureActivity = new CodeExecutionBehaviour(
				UPDATEFAILURE_ACTIVITY, this);
		registerActivity(loginFailureActivity);
		CodeExecutionBehaviour checkLoginActivity = new CodeExecutionBehaviour(
				CHECKUPDATE_ACTIVITY, this);
		registerActivity(checkLoginActivity);
		CodeExecutionBehaviour loginSuccessfulActivity = new CodeExecutionBehaviour(
				UPDATESUCCESSFUL_ACTIVITY, this);
		registerActivity(loginSuccessfulActivity);
	}

	private void defineTransitions() {
		registerTransition(new Transition(MESSAGERECEIVED_CONDITION, this),
				WAITFORUPDATE_ACTIVITY, CHECKUPDATE_ACTIVITY);
		registerTransition(new Transition(CORRECTUPDATE_CONDITION, this),
				CHECKUPDATE_ACTIVITY, UPDATESUCCESSFUL_ACTIVITY);
		registerTransition(new Transition(WRONGUPDATE_CONDITION, this), 
				CHECKUPDATE_ACTIVITY, UPDATEFAILURE_ACTIVITY);
		registerTransition(new Transition(), UPDATEFAILURE_ACTIVITY,
				WAITFORUPDATE_ACTIVITY);
		registerTransition(new Transition(), UPDATESUCCESSFUL_ACTIVITY,
				WAITFORUPDATE_ACTIVITY);
	}

	protected void executeWaitForUpdate() throws Exception {
		aclMessage = myAgent.blockingReceive();
		if (aclMessage != null && aclMessage.getPerformative() == ACLMessage.CFP) {
			/* We have received the message */
			messageReceived = true;
		}
	}

	protected void executeCheckUpdate() throws Exception {
	
		// Json to User
		String content = aclMessage.getContent();
		updateUser = JSONAdapter.JSONToUser(content);
			
		// User with the new username from database
		User newUsernameUser = HsqldbInterface.getUser(updateUser.getUserName());
		// Check if the new username (if changed) is not used
		successfulUpdate = HsqldbInterface.updateUser(updateUser);
	}

	protected void executeUpdateFailure() throws Exception {
		// TODO 
		/*
		System.out.println("Update failure");
		ACLMessage loginFailure = aclMessage.createReply();
		loginFailure.setPerformative(ACLMessage.REJECT_PROPOSAL);
		myAgent.send(loginFailure);*/
	}
	
	protected void executeUpdateSuccessful() throws Exception {
		
		// Log
		System.out.println("Update successful");
		System.out.println("user: " + JSONAdapter.userToJSON(updateUser).toString());
		
		// Reply
		ACLMessage reply = aclMessage.createReply();
		reply.setPerformative(ACLMessage.INFORM_REF);
		reply.setContent(JSONAdapter.userToJSON(updateUser).toString());
		myAgent.send(reply);
	}

	protected boolean checkMessageReceived() throws Exception{
		return messageReceived;
	}

	protected boolean checkCorrectUpdate() throws Exception{
		return successfulUpdate;
	}
	
	protected boolean checkWrongLogin() throws Exception{
		return !successfulUpdate;
	}
}
