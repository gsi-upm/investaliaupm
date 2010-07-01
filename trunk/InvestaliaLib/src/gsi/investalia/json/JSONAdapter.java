package gsi.investalia.json;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gsi.investalia.domain.Message;
import gsi.investalia.domain.Tag;
import gsi.investalia.domain.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JSONAdapter {
	
	public static final String DATE_MILIS = "date";
	public static final String EMAIL = "email";
	public static final String ID = "id";
	public static final String LIKED = "liked";
	public static final String LOCATION = "location";
	public static final String NAME = "name";
	public static final String RATING = "rating";
	public static final String READ = "read";
	public static final String TAG = "tag";
	public static final String TAGS = "tags";
	public static final String TEXT = "text";
	public static final String TIMES_READ ="times_read";
	public static final String TITLE = "title";	
	public static final String USER_NAME = "user_name";
	public static final String PASSWORD = "password";
	public static final String LAST_UPDATE = "last_update";
	
	public static JSONObject messageToJSON(Message message) throws JSONException {
		if (message == null) {
			throw new IllegalArgumentException("null message");
		}
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(ID, message.getId());
		jsonObj.put(USER_NAME, message.getUserName());
		jsonObj.put(TITLE, message.getTitle());
		jsonObj.put(TEXT, message.getText());
		jsonObj.put(TAGS, tagListToJSON(message.getTags()));
		jsonObj.put(DATE_MILIS, message.getDate().getTime());
		jsonObj.put(READ, message.isRead());
		jsonObj.put(LIKED, message.isLiked());
		jsonObj.put(RATING, message.getRating());
		jsonObj.put(TIMES_READ, message.getTimesRead());	
		return jsonObj;
	}

	public static Message JSONToMessage(String jsonStr) throws JSONException {
		JSONObject jsonObj = new JSONObject(jsonStr);
		
		return new Message(jsonObj.getInt(ID), jsonObj.getString(USER_NAME),
				jsonObj.getString(TITLE), jsonObj.getString(TEXT), 
				JSONToTagList(jsonObj.getString(TAGS)), 
				new Date(jsonObj.getLong(DATE_MILIS)), 
				jsonObj.getBoolean(READ), jsonObj.getBoolean(LIKED), 
				jsonObj.getInt(RATING), jsonObj.getInt(TIMES_READ));
	}
	
	public static List<Message> JSONToMessageList(String jsonStr) throws JSONException {
		List<Message> messages = new ArrayList<Message>();
		JSONArray jsonArray = new JSONArray(jsonStr);
		for (int i = 0; i < jsonArray.length(); i++) {
			messages.add(JSONToMessage(jsonArray.getJSONObject(i).toString()));
		}
		return messages;
	}
	
	public static JSONArray messageListToJSON (List<Message> messages) throws JSONException {
		JSONArray jsonArr = new JSONArray();
		for(Message message : messages) {
			jsonArr.put(messageToJSON(message));
		}
		return jsonArr;
	}

	public static JSONObject tagToJSON(Tag tag) throws JSONException {
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(ID, tag.getId());
		jsonObj.put(TAG, tag.getTagName());
		return jsonObj;
	}

	public static Tag JSONToTag(String jsonStr) throws JSONException {
		JSONObject jsonObj = new JSONObject(jsonStr);
		return new Tag(jsonObj.getInt(ID), jsonObj.getString(TAG));
	}

	public static List<Tag> JSONToTagList(String jsonStr) throws JSONException {
		List<Tag> tags = new ArrayList<Tag>();
		JSONArray jsonArray = new JSONArray(jsonStr);
		for (int i = 0; i < jsonArray.length(); i++) {
			tags.add(JSONToTag(jsonArray.getJSONObject(i).toString()));
		}
		return tags;
	}
	
	public static JSONArray tagListToJSON (List<Tag> tags) throws JSONException {
		JSONArray jsonArr = new JSONArray();
		for(Tag tag : tags) {
			jsonArr.put(tagToJSON(tag));
		}
		return jsonArr;
	}

	public static JSONObject userToJSON(User user) throws JSONException {
		if (user == null) {
			throw new IllegalArgumentException("null user");
		}
		JSONObject jsonObj = new JSONObject();
		jsonObj.put(ID, user.getId());
		jsonObj.put(USER_NAME, user.getUserName());
		jsonObj.put(PASSWORD, user.getPassword());
		jsonObj.put(NAME, user.getName());
		jsonObj.put(LOCATION, user.getLocation());
		jsonObj.put(EMAIL, user.getEmail());
		jsonObj.put(TAGS, tagListToJSON(user.getTagsFollowing()));
		jsonObj.put(LAST_UPDATE, user.getLastUpdate());
		return jsonObj;
	}

	public static User JSONToUser(String jsonStr) throws JSONException {
		JSONObject jsonObj = new JSONObject(jsonStr);
		return new User(jsonObj.getInt(ID), jsonObj.getString(USER_NAME), 
				jsonObj.getString(PASSWORD),
				jsonObj.getString(NAME), jsonObj.getString(LOCATION), 
				jsonObj.getString(EMAIL), JSONToTagList(jsonObj.getString(TAGS)),
						jsonObj.getInt(LAST_UPDATE));
	}
	
	public static List<User> JSONToUserList(String jsonStr) throws JSONException {
		List<User> users = new ArrayList<User>();
		JSONArray jsonArray = new JSONArray(jsonStr);
		for (int i = 0; i < jsonArray.length(); i++) {
			users.add(JSONToUser(jsonArray.getJSONObject(i).toString()));
		}
		return users;
	}
	
	public static JSONArray userListToJSON (List<User> users) throws JSONException {
		JSONArray jsonArr = new JSONArray();
		for(User user : users) {
			jsonArr.put(userToJSON(user));
		}
		return jsonArr;
	}
}
