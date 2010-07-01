package gsi.investalia.server.db;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gsi.investalia.json.JSONAdapter;

/**
 * Saves and gets all the needed information of the database. It takes and 
 * returns the information in JSON format
 * @author luis
 */
public class JSONInterface {

	/**
	 * Saves a new message
	 */
	public static void saveMessage(String jsonStr) throws JSONException {
		HsqldbInterface.saveMessage(JSONAdapter.JSONToMessage(jsonStr));
	}
	
	/**
	 * Updates the read and liked properties for a message list and user
	 */
	public static void updateReadAndLikedList(String messageListJsonStr, int idUser) throws JSONException {
		HsqldbInterface.updateReadAndLiked(JSONAdapter.JSONToMessageList(messageListJsonStr), idUser);
	}
	
	/**
	 * Updates the read and liked properties for a message and user
	 */
	public static void updateReadAndLiked(String messageJsonStr, int idUser) throws JSONException {
		HsqldbInterface.updateReadAndLiked(JSONAdapter.JSONToMessage(messageJsonStr), idUser);
	}
	
	/**
	 * Gets a user by its userName
	 */
	public static JSONObject getUser (String userName) throws JSONException {
		return JSONAdapter.userToJSON(HsqldbInterface.getUser(userName));
	}
	
	/**
	 * Gets a user by its id
	 */
	public static JSONObject getUser (int idUser) throws JSONException {
		return JSONAdapter.userToJSON(HsqldbInterface.getUser(idUser));
	}
	
	/**
	 * Gets the list of all the messages that a user is following
	 */
	public static JSONArray getAllUserMessages (String userName) throws JSONException {
		return JSONAdapter.messageListToJSON(HsqldbInterface.getAllUserMessages(userName));
	}
	
	/**
	 * Gets the list of the messages that a user is following after one given
	 */	
	public static JSONArray getUserMessagesSinceLast (String userName, int idMessageLast) throws JSONException {
		return JSONAdapter.messageListToJSON(HsqldbInterface.getUserMessagesSinceLast(userName, idMessageLast));
	}
	
	
}
