package gsi.investalia.domain;

public class PreviousMessagesData {
	private Message firstMessage;
	private Message fistMessageFollowing;
	private Message firstMessageRecommended;
	
	public PreviousMessagesData(Message firstMessage,
			Message fistMessageFollowing, Message firstMessageRecommended) {
		super();
		this.firstMessage = firstMessage;
		this.fistMessageFollowing = fistMessageFollowing;
		this.firstMessageRecommended = firstMessageRecommended;
	}

	public Message getFirstMessage() {
		return firstMessage;
	}

	public void setFirstMessage(Message firstMessage) {
		this.firstMessage = firstMessage;
	}

	public Message getFistMessageFollowing() {
		return fistMessageFollowing;
	}

	public void setFistMessageFollowing(Message fistMessageFollowing) {
		this.fistMessageFollowing = fistMessageFollowing;
	}

	public Message getFirstMessageRecommended() {
		return firstMessageRecommended;
	}

	public void setFirstMessageRecommended(Message firstMessageRecommended) {
		this.firstMessageRecommended = firstMessageRecommended;
	}
}
