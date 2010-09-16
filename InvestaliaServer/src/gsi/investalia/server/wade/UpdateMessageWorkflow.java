package gsi.investalia.server.wade;

import gsi.investalia.domain.Message;
import gsi.investalia.domain.User;
import gsi.investalia.json.JSONAdapter;
import gsi.investalia.server.db.MysqlInterface;
import jade.core.AID;
import jade.lang.acl.ACLMessage;

import com.tilab.wade.performer.layout.TransitionLayout;
import com.tilab.wade.performer.layout.MarkerLayout;
import com.tilab.wade.performer.layout.ActivityLayout;
import com.tilab.wade.performer.layout.WorkflowLayout;
import com.tilab.wade.performer.CodeExecutionBehaviour;
import com.tilab.wade.performer.Transition;
import com.tilab.wade.performer.WorkflowBehaviour;

@WorkflowLayout(transitions = { @TransitionLayout(routingpoints = "(215,41)", to = "WaitForUpdate", from = "UpdateFailure") }, entryPoint = @MarkerLayout(position = "(87,148)", activityName = "WaitForLogin"),activities = {
	@ActivityLayout(position = "(345,143)", name = "CheckUpdate"), 
	@ActivityLayout(position = "(344,16)", name = "UpdateFailure"), 
	@ActivityLayout(position = "(165,144)", name = "WaitForUpdate"), 
	@ActivityLayout(position = "(344,313)", name = "UpdateSuccessful"), 
	@ActivityLayout(position = "(165,313)", name = "OrderRecommendation")})
	
public class UpdateMessageWorkflow extends WorkflowBehaviour {

	private ACLMessage aclMessage;
	

	/* Activities for this workflow */
	public static final String WAITFORUPDATE_ACTIVITY = "WaitForUpdate";
	public static final String CHECKUPDATE_ACTIVITY = "CheckUpdate";
	public static final String UPDATEFAILURE_ACTIVITY = "UpdateFailure";
	public static final String UPDATESUCCESSFUL_ACTIVITY = "UpdateSuccessful";
	public static final String ORDERRECOMMENDATION_ACTIVITY = "OrderRecommendation";


	/* Conditions for the transitions */
	public static final String CORRECTUPDATE_CONDITION = "CorrectUpdate";
	public static final String WRONGUPDATE_CONDITION = "WrongUpdate";
	public static final String MESSAGERECEIVED_CONDITION = "MessageReceived";
	public static final String ORDERRECOMMEND_CONDITION = "OrderRecommend";
	public static final String NORECOMMEND_CONDITION = "NoRecommend";

	private boolean messageReceived, successfulUpdate, orderRecommend;
	
	public static final int NUMMESSAGESTHRESHOLD = 5;
	int numMessagesUpdated = 0;

	private void defineActivities() {
		CodeExecutionBehaviour waitForUpdateActivity = new CodeExecutionBehaviour(
				WAITFORUPDATE_ACTIVITY, this);
		registerActivity(waitForUpdateActivity, INITIAL);
		CodeExecutionBehaviour updateFailureActivity = new CodeExecutionBehaviour(
				UPDATEFAILURE_ACTIVITY, this);
		registerActivity(updateFailureActivity);
		CodeExecutionBehaviour checkUpdateActivity = new CodeExecutionBehaviour(
				CHECKUPDATE_ACTIVITY, this);
		registerActivity(checkUpdateActivity);
		CodeExecutionBehaviour updateSuccessfulActivity = new CodeExecutionBehaviour(
				UPDATESUCCESSFUL_ACTIVITY, this);
		registerActivity(updateSuccessfulActivity);
		CodeExecutionBehaviour orderRecommendationActivity = new CodeExecutionBehaviour(
				ORDERRECOMMENDATION_ACTIVITY, this);
		registerActivity(orderRecommendationActivity);
	}

	private void defineTransitions() {
		registerTransition(new Transition(MESSAGERECEIVED_CONDITION, this),
				WAITFORUPDATE_ACTIVITY, CHECKUPDATE_ACTIVITY);
		registerTransition(new Transition(CORRECTUPDATE_CONDITION, this),
				CHECKUPDATE_ACTIVITY, UPDATESUCCESSFUL_ACTIVITY);
		registerTransition(new Transition(ORDERRECOMMEND_CONDITION, this),
				UPDATESUCCESSFUL_ACTIVITY, ORDERRECOMMENDATION_ACTIVITY);
		registerTransition(new Transition(NORECOMMEND_CONDITION, this),
				UPDATESUCCESSFUL_ACTIVITY, WAITFORUPDATE_ACTIVITY);
		registerTransition(new Transition(), ORDERRECOMMENDATION_ACTIVITY,
				WAITFORUPDATE_ACTIVITY);
		registerTransition(new Transition(WRONGUPDATE_CONDITION, this), 
				CHECKUPDATE_ACTIVITY, UPDATEFAILURE_ACTIVITY);
		registerTransition(new Transition(), UPDATEFAILURE_ACTIVITY,
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
		System.out.println("Update message: " + content);
		Message message = JSONAdapter.JSONToMessage(content);
			
		// Update the message
		successfulUpdate = MysqlInterface.updateReadAndLiked(message);
	}

	protected void executeUpdateFailure() throws Exception {
		System.out.println("Message update failure");
		// TODO 
		/*
		ACLMessage loginFailure = aclMessage.createReply();
		loginFailure.setPerformative(ACLMessage.REJECT_PROPOSAL);
		myAgent.send(loginFailure);*/
	}
	
	protected void executeUpdateSuccessful() throws Exception {
		// Log
		System.out.println("Message update successful");
	
		numMessagesUpdated++;
		
		// It doesn't need a reply	
		if(numMessagesUpdated == NUMMESSAGESTHRESHOLD) {
			orderRecommend = true;
			numMessagesUpdated = 0;
		}
	}
	
	protected void executeOrderRecommendation() throws Exception {
	
		// Send it as a message
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);

		// Set the content
		String content = "newRecommendation";
		msg.setContent(content);

		// Set the agent receiver
		msg.addReceiver(new AID("recommendation", AID.ISLOCALNAME));

		myAgent.send(msg);
		
		orderRecommend = false;
		
		System.out.println("Message sent to recommendation agent");
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
	
	protected boolean checkOrderRecommend() throws Exception {
		return orderRecommend;
	}
	
	protected boolean checkNoRecommend() throws Exception {
		return !orderRecommend;
	}
}