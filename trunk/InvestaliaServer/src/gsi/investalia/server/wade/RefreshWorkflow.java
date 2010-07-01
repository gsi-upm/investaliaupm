package gsi.investalia.server.wade;

import jade.lang.acl.ACLMessage;


import jade.lang.acl.MessageTemplate;

import com.tilab.wade.performer.layout.TransitionLayout;
import com.tilab.wade.performer.layout.MarkerLayout;
import com.tilab.wade.performer.layout.ActivityLayout;
import com.tilab.wade.performer.layout.WorkflowLayout;
import com.tilab.wade.performer.CodeExecutionBehaviour;
import com.tilab.wade.performer.Transition;
import com.tilab.wade.performer.WorkflowBehaviour;


@WorkflowLayout(entryPoint = @MarkerLayout(position = "(29,74)", activityName = "WaitForLogin"), activities = { @ActivityLayout(position = "(336,71)", name = "Refresh"), @ActivityLayout(position = "(137,69)", name = "WaitForProposal"), @ActivityLayout(position = "(195,34)", name = "LoginSuccesful"), @ActivityLayout(position = "(207,121)", name = "CheckLogin"), @ActivityLayout(position = "(85,151)", name = "WaitForLogin"), @ActivityLayout(position = "(333,199)", name = "LoginFailure") })
public class RefreshWorkflow extends WorkflowBehaviour {

	ACLMessage askForRefresh;

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
		askForRefresh = myAgent.blockingReceive();
		if (askForRefresh != null && askForRefresh.getPerformative() == ACLMessage.CFP) {
			/* We have received the refresh message */
			refreshMessage = true;
		}

	}

	protected void executeRefresh() throws Exception {
	
		/* Example of login message : "21" */
		String lastUpdate = askForRefresh.getContent();
		//TODO: send ACLMessages with the new messages.		
	}

	protected boolean checkMessageReceived() throws Exception{
		return refreshMessage;
	}


}
