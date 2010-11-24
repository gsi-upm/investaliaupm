package gsi.investalia.server.wade;

import java.util.ArrayList;
import java.util.List;

import gsi.investalia.domain.Message;
import gsi.investalia.domain.PreviousMessagesData;
import gsi.investalia.domain.Tag;
import gsi.investalia.json.JSONAdapter;
import gsi.investalia.server.apirest.MessagesFromAPI;
import gsi.investalia.server.db.MysqlInterface;
import jade.lang.acl.ACLMessage;

import com.tilab.wade.performer.layout.MarkerLayout;
import com.tilab.wade.performer.layout.ActivityLayout;
import com.tilab.wade.performer.layout.WorkflowLayout;
import com.tilab.wade.performer.CodeExecutionBehaviour;
import com.tilab.wade.performer.Transition;
import com.tilab.wade.performer.WorkflowBehaviour;

@WorkflowLayout(entryPoint = @MarkerLayout(position = "(29,74)", activityName = "WaitForLogin"), activities = {
	@ActivityLayout(position = "(336,71)", name = "Refresh"),
	@ActivityLayout(position = "(137,69)", name = "WaitForProposal"),
	@ActivityLayout(position = "(195,34)", name = "LoginSuccesful"),
	@ActivityLayout(position = "(207,121)", name = "CheckLogin"),
	@ActivityLayout(position = "(85,151)", name = "WaitForLogin"),
	@ActivityLayout(position = "(333,199)", name = "LoginFailure") })
	public class DownloadOldMessagesWorkflow extends WorkflowBehaviour {

	ACLMessage aclMessage;

	/* Activities for this workflow */
	public static final String WAITFORPROPOSAL_ACTIVITY = "WaitForProposal";
	public static final String REFRESH_ACTIVITY = "Refresh";

	/* Conditions for the transitions */
	public static final String MESSAGERECEIVED_CONDITION = "MessageReceived";

	private boolean refreshMessage;

	private void defineActivities() {
		CodeExecutionBehaviour waitForProposalActivity = new CodeExecutionBehaviour(
				WAITFORPROPOSAL_ACTIVITY, this);
		registerActivity(waitForProposalActivity, INITIAL);
		CodeExecutionBehaviour refreshActivity = new CodeExecutionBehaviour(
				REFRESH_ACTIVITY, this);
		registerActivity(refreshActivity);
	}

	private void defineTransitions() {
		registerTransition(new Transition(MESSAGERECEIVED_CONDITION, this),
				WAITFORPROPOSAL_ACTIVITY, REFRESH_ACTIVITY);
		registerTransition(new Transition(), REFRESH_ACTIVITY,
				WAITFORPROPOSAL_ACTIVITY);
	}

	protected void executeWaitForProposal() throws Exception {
		aclMessage = myAgent.blockingReceive();
		if (aclMessage != null
				&& aclMessage.getPerformative() == ACLMessage.CFP) {
			/* We have received the refresh message */
			refreshMessage = true;
		}
	}

	protected void executeRefresh() throws Exception {

		// Get data
		String jsonStr = aclMessage.getContent();
		PreviousMessagesData data = JSONAdapter
		.JSONToPreviousMessagesData(jsonStr);
		int lastTag = JSONAdapter.JSONToLastTag(jsonStr);
		String userName = aclMessage.getSender().getLocalName();

		System.out.println("content: " + jsonStr);

		// Get the message list, tags and recommendations from db
		List<Message> messages = 
			MysqlInterface.getOldMessagesIncludingRecommended(userName, 
					data.getFirstMessage(), data.getFistMessageFollowing(), 
					data.getFirstMessageRecommended()); 
		System.out.println("message count: " + messages.size()); 
		List<Tag> tags = MysqlInterface.getTagsSinceLast(lastTag);
		System.out.println("tags count: " + tags.size());

		// Generate the content 
		String content = JSONAdapter.messageListAndTagListToJSON(messages, tags).toString();
		System.out.println("content sent: " + content);

		// Send it as reply. Propose, so the android app will act as when 
		//we are sending new messages 
		ACLMessage reply =
			aclMessage.createReply(); reply.setPerformative(ACLMessage.PROPOSE);
			reply.setContent(content); myAgent.send(reply);

	}

	protected boolean checkMessageReceived() throws Exception {
		return refreshMessage;
	}
}
