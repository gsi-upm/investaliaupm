package gsi.investalia.server.wade;

import gsi.investalia.domain.Message;
import gsi.investalia.json.JSONAdapter;
import gsi.investalia.server.db.HsqldbInterface;
import gsi.investalia.server.db.MysqlInterface;
import jade.lang.acl.ACLMessage;

import jade.lang.acl.MessageTemplate;

import com.tilab.wade.performer.layout.TransitionLayout;
import com.tilab.wade.performer.layout.MarkerLayout;
import com.tilab.wade.performer.layout.ActivityLayout;
import com.tilab.wade.performer.layout.WorkflowLayout;
import com.tilab.wade.performer.CodeExecutionBehaviour;
import com.tilab.wade.performer.Transition;
import com.tilab.wade.performer.WorkflowBehaviour;

@WorkflowLayout(entryPoint = @MarkerLayout(position = "(75,138)", activityName = "WaitForProposal"), activities = {
		@ActivityLayout(position = "(458,259)", name = "Posting"),
		@ActivityLayout(size = "(140,50)", position = "(444,118)", name = "Recommendation"),
		@ActivityLayout(size = "(136,50)", position = "(225,196)", name = "WaitForProposal") })
public class PostingWorkflow extends WorkflowBehaviour {

	ACLMessage aclMessage;

	/* Activities for this workflow */
	public static final String WAITFORPROPOSAL_ACTIVITY = "WaitForProposal";
	public static final String POSTING_ACTIVITY = "Posting";
	public static final String RECOMMENDATION_ACTIVITY = "Recommendation";

	/* Conditions for the transitions */
	public static final String MESSAGERECEIVED_CONDITION = "MessageReceived";
	public static final String RECEIVEDCORRECTLY_CONDITION = "ReceivedCorrectly";
	public static final String NOTRECEIVED_CONDITION = "NotReceived";

	private boolean messageReceived, receivedCorrectly;

	private void defineActivities() {
		CodeExecutionBehaviour waitForProposalActivity = new CodeExecutionBehaviour(
				WAITFORPROPOSAL_ACTIVITY, this);
		registerActivity(waitForProposalActivity, INITIAL);
		CodeExecutionBehaviour postingActivity = new CodeExecutionBehaviour(
				POSTING_ACTIVITY, this);
		registerActivity(postingActivity);
		CodeExecutionBehaviour recommendationActivity = new CodeExecutionBehaviour(
				RECOMMENDATION_ACTIVITY, this);
		registerActivity(recommendationActivity);
	}

	private void defineTransitions() {
		registerTransition(new Transition(MESSAGERECEIVED_CONDITION, this),
				WAITFORPROPOSAL_ACTIVITY, POSTING_ACTIVITY);
		registerTransition(new Transition(RECEIVEDCORRECTLY_CONDITION, this),
				POSTING_ACTIVITY, RECOMMENDATION_ACTIVITY);
		registerTransition(new Transition(), RECOMMENDATION_ACTIVITY,
				WAITFORPROPOSAL_ACTIVITY);
		registerTransition(new Transition(NOTRECEIVED_CONDITION, this),
				POSTING_ACTIVITY, WAITFORPROPOSAL_ACTIVITY);
	}

	protected void executeWaitForProposal() throws Exception {
		aclMessage = myAgent.blockingReceive();
		if (aclMessage != null
				&& aclMessage.getPerformative() == ACLMessage.CFP) {
			/* We have received the new message to post */
			messageReceived = true;
		}
	}

	protected void executeRecommendation() throws Exception {
		// TODO : Update the database with new recommendations , or something
		// similar

	}

	protected void executePosting() throws Exception {

		// Get the message
		String content = aclMessage.getContent();
		Message message = JSONAdapter.JSONToMessage(content);
		System.out.println("Posting: " + content);

		// Save the message
		MysqlInterface.saveMessage(message);

		// Reply
		ACLMessage reply = aclMessage.createReply();
		reply.setPerformative(ACLMessage.CONFIRM);
		reply.setContent("Message received and added to the database");
		receivedCorrectly = true;
		myAgent.send(reply);
	}

	protected boolean checkMessageReceived() throws Exception {
		return messageReceived;
	}

	protected boolean checkReceivedCorrectly() throws Exception {
		return receivedCorrectly;
	}

	protected boolean checkNotReceived() throws Exception {
		return !receivedCorrectly;
	}

}
