package gsi.investalia.domain;

public class PreviousMessagesData {
	private int firstIdMessage;
	private int fistIdMessageFollowing;
	private int firstIdMessageRecommended;
	
	public PreviousMessagesData(int firstIdMessage,
			int fistIdMessageFollowing, int firstIdMessageRecommended) {
		super();
		this.firstIdMessage = firstIdMessage;
		this.fistIdMessageFollowing = fistIdMessageFollowing;
		this.firstIdMessageRecommended = firstIdMessageRecommended;
	}
	
	public int getFirstIdMessage() {
		return firstIdMessage;
	}
	public void setFirstIdMessage(int firstIdMessage) {
		this.firstIdMessage = firstIdMessage;
	}
	public int getFistIdMessageFollowing() {
		return fistIdMessageFollowing;
	}
	public void setFistIdMessageFollowing(int fistIdMessageFollowing) {
		this.fistIdMessageFollowing = fistIdMessageFollowing;
	}
	public int getFirstIdMessageRecommended() {
		return firstIdMessageRecommended;
	}
	public void setFirstIdMessageRecommended(int firstIdMessageRecommended) {
		this.firstIdMessageRecommended = firstIdMessageRecommended;
	}
}
