package gsi.investalia.server.jade;


public class RecommendationAgent extends InvestaliaAgent {

	@Override
	protected String getAgentName() {
		return "recommendation";
	}

	@Override
	protected String getWorkflow() {
		return "RecommendationWorkflow";
	}
}

