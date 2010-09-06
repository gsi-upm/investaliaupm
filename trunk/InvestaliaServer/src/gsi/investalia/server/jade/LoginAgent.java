package gsi.investalia.server.jade;


public class LoginAgent extends InvestaliaAgent {

	@Override
	protected String getAgentName() {
		return "login";
	}

	@Override
	protected String getWorkflow() {
		return "LoginWorkflow";
	}
}

