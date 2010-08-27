package gsi.investalia.server.wade;

import gsi.investalia.domain.Message;
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


public class PostingWorkflow extends WorkflowBehaviour {

	ACLMessage messageToPost;

	/* Activities for this workflow */ 
	public static final String WAITFORPROPOSAL_ACTIVITY = "WaitForProposal";
	public static final String POSTING_ACTIVITY = "Posting";
	public static final String RECOMMENDATION_ACTIVITY = "Recommendation";


	/* Conditions for the transitions */
	public static final String MESSAGERECEIVED_CONDITION = "MessageReceived";
	public static final String RECEIVEDCORRECTLY_CONDITION = "ReceivedCorrectly";
	public static final String NOTRECEIVED_CONDITION = "NotReceived";

	private boolean messageReceived, receivedCorrectly, notReceived;

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
		registerTransition(new Transition(RECEIVEDCORRECTLY_CONDITION,this), 
				POSTING_ACTIVITY, RECOMMENDATION_ACTIVITY);
		registerTransition(new Transition(), 
				 RECOMMENDATION_ACTIVITY, WAITFORPROPOSAL_ACTIVITY);
		registerTransition(new Transition(NOTRECEIVED_CONDITION,this), 
				POSTING_ACTIVITY, WAITFORPROPOSAL_ACTIVITY);

	}

	protected void executeWaitForProposal() throws Exception {
		messageToPost = myAgent.blockingReceive();
		if (messageToPost != null && messageToPost.getPerformative() == ACLMessage.CFP) {
			/* We have received the new message to post */
			messageReceived = true;
		}

	}
	
	protected void executeRecommendation() throws Exception {
		//TODO : Update the database with new recommendations , or something similar

	}

	protected void executePosting() throws Exception {
	
		ACLMessage reply = messageToPost.createReply();
		String content = messageToPost.getContent(); //Contenido del mensaje en JSON
		Message message = JSONAdapter.JSONToMessage(content);
		
		int userID = Integer.parseInt(messageToPost.getOntology()); //Id del usuario que postea
		System.out.println(userID+" "+message.getTitle()+" "+
				message.getText()+" "+message.getRating()+" "+message.getUserName());
		
		try{
			//TODO: Add message to the database.
			reply.setPerformative(ACLMessage.CONFIRM);
			reply.setContent("Message received and added to the database");
			receivedCorrectly = true;
		}catch (Exception e){ //More likely, a SQLException
			reply.setPerformative(ACLMessage.FAILURE);
			reply.setContent("Message not received.Try sending it again");
			notReceived= true;
		}finally{
			myAgent.send(reply);
		}
		
	}

	protected boolean checkMessageReceived() throws Exception{
		return messageReceived;
	}
	
	protected boolean checkReceivedCorrectly() throws Exception{
		return receivedCorrectly;
	}
	
	protected boolean checkNotReceived() throws Exception{
		return notReceived;
	}


}
