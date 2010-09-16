package gsi.investalia.server.wade;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import org.json.JSONException;

import es.upm.multidimensional.RecommendationGenerator;
import gsi.investalia.domain.Message;
import gsi.investalia.domain.Tag;
import gsi.investalia.json.JSONAdapter;
import gsi.investalia.server.db.MysqlInterface;
import jade.core.AID;
import jade.domain.AMSService;
import jade.domain.FIPAAgentManagement.AMSAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import com.tilab.wade.performer.layout.MarkerLayout;
import com.tilab.wade.performer.layout.ActivityLayout;
import com.tilab.wade.performer.layout.WorkflowLayout;
import com.tilab.wade.performer.CodeExecutionBehaviour;
import com.tilab.wade.performer.Transition;
import com.tilab.wade.performer.WorkflowBehaviour;


@WorkflowLayout(entryPoint = @MarkerLayout(position = "(169,70)", activityName = "Recommendation"), activities = {
	@ActivityLayout(position = "(139,276)", name = "WaitForProposal"),
	@ActivityLayout(position = "(138,151)", name = "Recommendation"),
	@ActivityLayout(position = "(138,391)", name = "Refresh") })

	public class RecommendationWorkflow extends WorkflowBehaviour {

	ACLMessage aclMessage;
 
	/* Activities for this workflow */
	public static final String WAITFORPROPOSAL_ACTIVITY = "WaitForProposal";
	public static final String RECOMMENDATION_ACTIVITY = "Recommendation";
	public static final String REFRESHRECOMMENDATION_ACTIVITY = "RefreshRecommendation";

	/* Conditions for the transitions */
	public static final String REFRESH_CONDITION = "Refresh";

	String[] dimensions = {"READ","LIKED"};
	RecommendationGenerator recommender = null;
	HashMap<String, Object> recommendationData = new HashMap<String, Object> ();
	int numOfRecommendations = 30;
	Date timestamp;
		
	private boolean refresh;

	private void defineActivities() {
		
		CodeExecutionBehaviour recommendationActivity = new CodeExecutionBehaviour(
				RECOMMENDATION_ACTIVITY, this);
		registerActivity(recommendationActivity, INITIAL);
		
		CodeExecutionBehaviour waitForProposalActivity = new CodeExecutionBehaviour(
				WAITFORPROPOSAL_ACTIVITY, this);
		registerActivity(waitForProposalActivity);
		
		CodeExecutionBehaviour refreshRecommendationActivity = new CodeExecutionBehaviour(
				REFRESHRECOMMENDATION_ACTIVITY, this);
		registerActivity(refreshRecommendationActivity);
		
	}

	private void defineTransitions() {
	
		registerTransition(new Transition(), RECOMMENDATION_ACTIVITY, 
				WAITFORPROPOSAL_ACTIVITY);
		registerTransition(new Transition(REFRESH_CONDITION, this), WAITFORPROPOSAL_ACTIVITY, 
				REFRESHRECOMMENDATION_ACTIVITY);
		registerTransition(new Transition(), REFRESHRECOMMENDATION_ACTIVITY, WAITFORPROPOSAL_ACTIVITY);
	}
	

	protected void executeRecommendation() throws Exception {
		
		recommendationData.put(dimensions[0], new HashMap<Long,HashMap<Long,Float>>());
		recommendationData.put(dimensions[1], new HashMap<Long,HashMap<Long,Float>>());		
		
		timestamp = Calendar.getInstance().getTime();
		MysqlInterface.takeRecommendationData (recommendationData, dimensions, "users_messages", "iduser", "idmessage", "liked");
		
		HashMap<String, Double > ponderations = new HashMap<String, Double> ();
		ponderations.put(dimensions[0],1.0);
		ponderations.put(dimensions[1],1.0);

		HashMap<String, String > similarityAlgorithms = new HashMap<String, String> ();
		similarityAlgorithms.put(dimensions[0],RecommendationGenerator.TANIMOTO);
		similarityAlgorithms.put(dimensions[1],RecommendationGenerator.EUCLIDIAN);	

		try {
			recommender = new RecommendationGenerator(recommendationData,ponderations,similarityAlgorithms);
			recommender.showInfo();
			
			AMSAgentDescription[] activeAgents = searchActiveAgents();
			
			for(Long userID : ((HashMap<Long,HashMap<Long,Float>>) recommendationData.get(dimensions[0])).keySet()) {
				
				HashMap<Long,Float> userRecommendations = recommender.getRecommendations(userID, numOfRecommendations);
			//	System.out.println(userID+" recommendation list: "+userRecommendations);
				MysqlInterface.updateUserRecommendationData(userID.intValue(), userRecommendations);
				if(userIsOn(activeAgents, userID))
					sendRecommendations(userID, userRecommendations);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("End recommendation process");
	}
	
	protected void executeWaitForProposal() throws Exception{
		
		MessageTemplate mt = MessageTemplate.and (  
		           MessageTemplate.MatchSender(new AID("updateMessage", AID.ISLOCALNAME)),
		           MessageTemplate.MatchContent("newRecommendation"));
		
		aclMessage = myAgent.blockingReceive(mt);
		if (aclMessage != null
				&& aclMessage.getPerformative() == ACLMessage.CFP ) 
			refresh = true;
		
		System.out.println(aclMessage.getSender().getName() + aclMessage.getContent());
		
		System.out.println("Refresh");
	}
	
	
	protected void executeRefreshRecommendation() throws Exception {
	
		MysqlInterface.updateRecommendationData (recommender, dimensions, timestamp, "users_messages", 
				"iduser", "idmessage", "liked","updatedate");
		
		timestamp = Calendar.getInstance().getTime();
		AMSAgentDescription[] activeAgents = searchActiveAgents();
				
		try {
			recommender.refresh();
			recommender.showInfo();
			
			for(Long userID : ((HashMap<Long,HashMap<Long,Float>>) recommendationData.get(dimensions[0])).keySet()) {
					
				HashMap<Long,Float> userRecommendations = recommender.getRecommendations(userID, numOfRecommendations);
				
				MysqlInterface.updateUserRecommendationData(userID.intValue(), userRecommendations);
				
				if(userIsOn(activeAgents, userID))
					sendRecommendations(userID, userRecommendations);		
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		System.out.println("End refreshing recommendation process");
	}
	
	private AMSAgentDescription[] searchActiveAgents() {
		
		AMSAgentDescription[] agents = null;
      	
		try {
            SearchConstraints c = new SearchConstraints();
            c.setMaxResults (new Long(-1));
			agents = AMSService.search(this.getAgent(), new AMSAgentDescription (), c );
		}
		catch (Exception e) {
            System.out.println( "Problem searching AMS: " + e );
            e.printStackTrace();
		}
		
		return agents;
	}
	
	private boolean userIsOn(AMSAgentDescription[] agents, Long userID) {
		
		String userName = MysqlInterface.getUser(userID.intValue()).getUserName();
		for (int i=0; i<agents.length;i++) {
			if(agents[i].getName().getLocalName().equals(userName))
				return true;
		}
		return false;
	}

	private void sendRecommendations(Long userID, HashMap<Long, Float> userRecommendations) {
		
		// Send it as a message
		ACLMessage msg = new ACLMessage(ACLMessage.CFP);

		// Set the content
		String content = null;
		try {
			content = JSONAdapter.recommendationsToJSON(userRecommendations).toString();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		msg.setContent(content);

		// Set the agent receiver
		msg.addReceiver(new AID(MysqlInterface.getUser(userID.intValue()).getUserName(), AID.ISLOCALNAME));

		myAgent.send(msg);
		
		System.out.println("Message sent");
	}
	
	protected boolean checkRefresh() throws Exception{
		return refresh;
	}

}
