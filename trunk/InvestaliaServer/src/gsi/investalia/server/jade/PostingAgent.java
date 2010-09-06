package gsi.investalia.server.jade;


public class PostingAgent extends InvestaliaAgent {
	
	@Override
	protected String getAgentName() {
		return "posting";
	}

	@Override
	protected String getWorkflow() {
		return "PostingWorkflow";
	}
}

