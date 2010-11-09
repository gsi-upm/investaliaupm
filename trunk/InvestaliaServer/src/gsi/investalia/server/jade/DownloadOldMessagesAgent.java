package gsi.investalia.server.jade;


public class DownloadOldMessagesAgent extends InvestaliaAgent {
	
	@Override
	protected String getAgentName() {
		return "downloadOldMessages";
	}

	@Override
	protected String getWorkflow() {
		return "DownloadOldMessagesWorkflow";
	}
}

