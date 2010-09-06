package gsi.investalia.server.jade;


public class DownloadMessagesAgent extends InvestaliaAgent {
	
	@Override
	protected String getAgentName() {
		return "downloadMessages";
	}

	@Override
	protected String getWorkflow() {
		return "DownloadMessagesWorkflow";
	}
}

