package gsi.investalia.server.jade;


public class NewUserAgent extends InvestaliaAgent {
	
	@Override
	protected String getAgentName() {
		return "newUser";
	}

	@Override
	protected String getWorkflow() {
		return "NewUserWorkflow";
	}
}

