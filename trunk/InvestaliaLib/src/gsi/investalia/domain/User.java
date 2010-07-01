package gsi.investalia.domain;

import java.util.ArrayList;
import java.util.List;

public class User {
	private int id;
	private String userName;
	private String password;
	private String name;
	private String location;
	private String email;
	private List<Tag> tagsFollowing;
	private int lastUpdate;

	public User(int id, String userName, String password, String name, 
			String location, String email, List<Tag> tagsFollowing, 
			int lastUpdate) {
		super();
		this.id = id;
		this.userName = userName;
		this.password = password;
		this.name = name;
		this.location = location;
		this.email = email;
		this.tagsFollowing = tagsFollowing;
		this.lastUpdate = lastUpdate;
	}
	
	

	public User(String userName, String password) {
		super();
		this.userName = userName;
		this.password = password;
		this.id = -1;
		this.name = "";
		this.location = "";
		this.email = "";
		this.tagsFollowing = new ArrayList<Tag>();
		this.lastUpdate = 0;
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public List<Tag> getTagsFollowing() {
		return tagsFollowing;
	}

	public void setTagsFollowing(List<Tag> tagsFollowing) {
		this.tagsFollowing = tagsFollowing;
	}

	public int getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(int lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	@Override
	public String toString() {
		String str = "User: id:" + id + ", @" + userName + ", name:" + name + ", tags:";
		for(Tag tag : tagsFollowing) {
			str += " " + tag;
		}
		return str;
	}
}
