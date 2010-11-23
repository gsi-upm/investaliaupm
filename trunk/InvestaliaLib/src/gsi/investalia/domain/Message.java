package gsi.investalia.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message {
	private int id;
	private String userName;
	private String title;
	private String text;
	private List<Tag> tags;
	private Date date;
	private boolean read;
	private boolean liked; //This adds +1 to the total rating
	private int rating;
	private int timesRead;
	private double affinity;
	private long idMessageAPI; 
	
	public Message(int id, String userName, String title, String text,
			List<Tag> tags, Date date, boolean read, boolean liked,
			int rating, int timesRead, double affinity, 
			long idMessageAPI) {
		super();
		this.id = id;
		this.userName = userName;
		this.title = title;
		this.text = text;
		this.tags = tags;
		this.date = date;
		this.read = read;
		this.liked = liked;
		this.rating = rating;
		this.timesRead = timesRead;
		this.affinity = affinity;
		this.idMessageAPI = idMessageAPI;
	}
	
	public static Message getZeroMessage() {
		return new Message(0, "", "", "", new ArrayList<Tag>(), 
				new Date(0), false, false,
				0, 0, 0, 0);
	}	

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public List<Tag> getTags() {
		return tags;
	}

	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isLiked() {
		return liked;
	}

	public void setLiked(boolean liked) {
		this.liked = liked;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public int getTimesRead() {
		return timesRead;
	}

	public void setTimesRead(int timesRead) {
		this.timesRead = timesRead;
	}

	public double getAffinity() {
		return affinity;
	}

	public void setAffinity(double affinity) {
		this.affinity = affinity;
	}
	
	public long getIdMessageAPI() {
		return idMessageAPI;
	}

	public void setIdMessageAPI(long idMessageAPI) {
		this.idMessageAPI = idMessageAPI;
	}

	public String toString () {
		String str = "Message: id:" + id + ", @" + userName + ", text:\"" + text + "\", tags:";
		for(Tag tag : tags) {
			str += " " + tag;
		}
		return str;
	}	
}
