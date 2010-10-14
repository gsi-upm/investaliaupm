package gsi.investalia.server.wade;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gsi.investalia.domain.Message;
import gsi.investalia.domain.Tag;
import gsi.investalia.domain.User;
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


@WorkflowLayout(entryPoint = @MarkerLayout(position = "(29,74)", activityName = "WaitForLogin"), activities = { @ActivityLayout(position = "(336,71)", name = "Refresh"), @ActivityLayout(position = "(137,69)", name = "WaitForProposal"), @ActivityLayout(position = "(195,34)", name = "LoginSuccesful"), @ActivityLayout(position = "(207,121)", name = "CheckLogin"), @ActivityLayout(position = "(85,151)", name = "WaitForLogin"), @ActivityLayout(position = "(333,199)", name = "LoginFailure") })
public class DownloadMessagesWorkflow extends WorkflowBehaviour {

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
		if (aclMessage != null && aclMessage.getPerformative() == ACLMessage.CFP) {
			/* We have received the refresh message */
			refreshMessage = true;
		}
	}

	protected void executeRefresh() throws Exception {

        // Get data
        String jsonStr = aclMessage.getContent();
        int lastUpdate = JSONAdapter.JSONToLastUpdate(jsonStr); 
        int lastTag = JSONAdapter.JSONToLastTag(jsonStr);
        String userName = aclMessage.getSender().getLocalName();
        
        // Log
        System.out.println("Executing refresh");
        System.out.println("content received: " + jsonStr);
        System.out.println("username: " + userName);
        
        // Get the message list, tags and recommendations from db
        List<Message> messages = MysqlInterface.getUserMessagesSinceLast(userName, lastUpdate);
        
        /*Add messages from the API*/
        //TODO: Mirar cuándo fue la fecha de la última actualización.
        List<Message> blogsFromAPI = MessagesFromAPI.getBlogsFromAPI("0000000000");
        List<Message> commentsFromAPI = MessagesFromAPI.getCommentsFromAPI("0000000000");
        List<Message> notesFromAPI = MessagesFromAPI.getNotesInTheWallFromAPI("0000000000");

        for(Message mes:blogsFromAPI){
                messages.add(mes);
        }
        for(Message mes:commentsFromAPI){
                messages.add(mes);
        }
        for(Message mes:notesFromAPI){
                messages.add(mes);
        }

        System.out.println("message count: " + messages.size());
        List<Tag> tags = MysqlInterface.getTagsSinceLast(lastTag);
        System.out.println("tags count: " + tags.size());
        HashMap<Long,Float> recommendations = MysqlInterface.getUserRecommendationData(userName);
        System.out.println("recommendations count: " + recommendations.size());

        // Generate the content
        String content = JSONAdapter.messageListAndTagListAndRecommendationsToJSON(messages, tags, recommendations).toString();
        System.out.println("content sent: " + content);
        
        // Send it as reply
        ACLMessage reply = aclMessage.createReply();
        reply.setPerformative(ACLMessage.PROPOSE);
        reply.setContent(content);
        myAgent.send(reply);
}
	protected boolean checkMessageReceived() throws Exception{
		return refreshMessage;
	}
}
