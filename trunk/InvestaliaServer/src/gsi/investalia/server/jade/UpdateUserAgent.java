package gsi.investalia.server.jade;


public class UpdateUserAgent extends InvestaliaAgent {
	
	@Override
	protected String getAgentName() {
		return "updateUser";
	}

	@Override
	protected String getWorkflow() {
		return "UpdateUserWorkflow";
	}
}

