package gsi.investalia.server.jade;


public class UpdateMessageAgent extends InvestaliaAgent {
	
	@Override
	protected String getAgentName() {
		return "updateMessage";
	}

	@Override
	protected String getWorkflow() {
		return "UpdateMessageWorkflow";
	}
}

