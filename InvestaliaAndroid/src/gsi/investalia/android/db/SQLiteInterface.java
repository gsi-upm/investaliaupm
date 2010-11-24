package gsi.investalia.android.db;

import gsi.investalia.android.app.MessageList;
import gsi.investalia.domain.Message;
import gsi.investalia.domain.Tag;
import gsi.investalia.domain.User;
import gsi.investalia.json.JSONAdapter;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

import org.json.JSONException;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class SQLiteInterface {

	private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String PREFERENCES_FILE = "main";
	private static final String LOGGED_USER = "logged_user";
	private static final String TAG_LOGGER = "Database";

	public static final String FOLLOWING = "following";
	public static final String ALL = "all";

	public static void saveMessages(Context context, Activity activity, List<Message> messages) {
		MessagesDBHelper dbHelper = new MessagesDBHelper(context);
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			for (Message m : messages) {
				
				Message mes = getMessage(activity, m.getId());
				
				if (mes == null) {
				
				// Container for the values
				ContentValues messageValues = new ContentValues();

				// Date as String
				String dateStr = new SimpleDateFormat(DATE_FORMAT).format(m
						.getDate());

				// Values into content: messages table
				messageValues.put(MessagesDBHelper.IDMESSAGE, m.getId());
				messageValues.put(MessagesDBHelper.USERNAME, m.getUserName());
				messageValues.put(MessagesDBHelper.TITLE, m.getTitle());
				messageValues.put(MessagesDBHelper.TEXT, m.getText());
				messageValues.put(MessagesDBHelper.DATE, dateStr);
				messageValues.put(MessagesDBHelper.LIKED, m.isLiked());
				messageValues.put(MessagesDBHelper.READ, m.isRead());
				messageValues.put(MessagesDBHelper.RATING, m.getRating());
				messageValues.put(MessagesDBHelper.AFFINITY, m.getAffinity());
				messageValues.put(MessagesDBHelper.IDMESSAGE_API, m
						.getIdMessageAPI());
				messageValues
						.put(MessagesDBHelper.TIMES_READ, m.getTimesRead());

				// Save the message
				db.insertOrThrow(MessagesDBHelper.MESSAGES_TABLE, null,
						messageValues);

				// Values into content: messages_tags table
				for (Tag t : m.getTags()) {
					ContentValues tagsValues = new ContentValues();
					tagsValues.put(MessagesDBHelper.IDMESSAGE, m.getId());
					tagsValues.put(MessagesDBHelper.IDTAG, t.getId());
					// Save the tag
					db.insertOrThrow(MessagesDBHelper.MESSAGES_TAGS_TABLE,
							null, tagsValues);
				}
					Log.i("DATABASE", "Inserted into db");
				}
			}
		} catch (Exception e) {
			Log.e("DATABASE", "Error in db " + e);
		} finally {
			// Always close the dbHelper
			dbHelper.close();
		}
	}

	public static void updateMessage(Context context, Message message) {
		MessagesDBHelper dbHelper = new MessagesDBHelper(context);
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			// Container for the values
			ContentValues messageValues = new ContentValues();
			messageValues.put(MessagesDBHelper.LIKED, message.isLiked());
			messageValues.put(MessagesDBHelper.READ, message.isRead());
			messageValues.put(MessagesDBHelper.TIMES_READ, message
					.getTimesRead());
			messageValues.put(MessagesDBHelper.RATING, message.getRating());

			// Update the message
			db.update(MessagesDBHelper.MESSAGES_TABLE, messageValues,
					MessagesDBHelper.IDMESSAGE + "=" + message.getId(), null);

			Log.i("DATABASE", "Message updated");
		} finally {
			// Always close the dbHelper
			dbHelper.close();
		}
	}

	public static void deleteAllMessages(Context context) {
		MessagesDBHelper dbHelper = new MessagesDBHelper(context);
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			db.delete(MessagesDBHelper.MESSAGES_TABLE, null, null);
			db.delete(MessagesDBHelper.TAGS_TABLE, null, null);
			db.delete(MessagesDBHelper.MESSAGES_TAGS_TABLE, null, null);
		} finally {
			// Always close the dbHelper
			dbHelper.close();
		}
	}

	private static String getUserTagsAsStringList(Activity activity) {
		User user = getLoggedUser(activity);
		String str = "";
		for (int i = 0; i < user.getTagsFollowing().size(); i++) {
			str += user.getTagsFollowing().get(i).getId();
			if (i < user.getTagsFollowing().size() - 1) {
				str += ", ";
			}
		}
		return str;
	}

	public static void addMessages(Activity activity, List<Message> messages,
			String which, String orderBy) {
		// Get the helper
		MessagesDBHelper dbHelper = new MessagesDBHelper(activity);
		// Clear the list
		messages.clear();

		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d("DATABASE", "Database obtained");

			if(orderBy.equals(MessagesDBHelper.DATE)) {
				orderBy += " DESC, " + MessagesDBHelper.IDMESSAGE;
			}
			String query = "";
			// If ordering by date, show the newer ones before
			if (which.equals(ALL)) {
				query = "SELECT * FROM messages ORDER BY " + orderBy + " DESC";
			} else if (which.equals(FOLLOWING)) {
				query = "SELECT DISTINCT m.* FROM messages AS m, messages_tags AS mt "
						+ "WHERE m.idmessage = mt.idmessage AND idtag IN ("
						+ getUserTagsAsStringList(activity)
						+ ") ORDER BY "
						+ orderBy + " DESC";
			}

			Cursor cursor = db.rawQuery(query, null);
			activity.startManagingCursor(cursor);
			Log.d("DATABASE", "Query for messages executed");

			addMessagesFromCursor(messages, cursor, activity);
			Log.d("DATABASE", "Messages added to list");

		} finally {
			// Always close the subjectsData
			dbHelper.close();
		}
		Log.i("DATABASE", messages.size() + " messages from db");
		// Add a message to represent the message refresh
		if (!messages.isEmpty()) {
			Log.i("DATABSE", "Added refresh message");
			messages
					.add(new Message(MessageList.IDREFRESH, "Refresh", "", "",
							new ArrayList<Tag>(), new Date(), false, false, 0,
							0, 0, 0));
		}
	}
	
	private static void addMessagesFromCursor(List<Message> messages, Cursor cursor, 
			Activity activity) {
		while (cursor.moveToNext()) {
			// Format the date
			Date date;
			try {
				date = new SimpleDateFormat(DATE_FORMAT).parse(cursor
						.getString(4));
			} catch (ParseException e) {
				Log.e("DATABASE", "Error parsing date");
				date = new Date();
			}
			// Add the message
			List<Tag> tags = getMessageTags(activity, cursor.getInt(0));
			messages.add(new Message(cursor.getInt(0), cursor.getString(1),
					cursor.getString(2), cursor.getString(3), tags, date,
					1 == cursor.getInt(5), 1 == cursor.getInt(6), cursor
							.getInt(7), cursor.getInt(8), cursor
							.getDouble(9), cursor.getLong(10)));
		}
	}

	public static Message getMessage(Activity activity, int idMessage) {
		// Get the helper
		MessagesDBHelper dbHelper = new MessagesDBHelper(activity);

		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d("DATABASE", "Database obtained");

			// Execute the query
			Cursor cursor = db.query(MessagesDBHelper.MESSAGES_TABLE, null,
					MessagesDBHelper.IDMESSAGE + " = " + idMessage, null, null,
					null, null);
			activity.startManagingCursor(cursor);
			Log.d("DATABASE", "Query for messages executed");

			// Extract the results
			if (cursor.moveToNext()) {
				// Format the date
				Date date;
				try {
					date = new SimpleDateFormat(DATE_FORMAT).parse(cursor
							.getString(4));
				} catch (ParseException e) {
					Log.e("DATABASE", "Error parsing date");
					date = new Date();
				}
				// Add the message
				return new Message(cursor.getInt(0), cursor.getString(1),
						cursor.getString(2), cursor.getString(3),
						getMessageTags(activity, cursor.getInt(0)), date,
						1 == cursor.getInt(5), 1 == cursor.getInt(6), cursor
								.getInt(7), cursor.getInt(8), cursor
								.getDouble(9), cursor.getLong(10));
			}
			Log.d("DATABASE", "Messages returned");

		} finally {
			// Always close the subjectsData
			dbHelper.close();
		}
		return null;
	}

	/**
	 * Saves the logged user into the android shared preferences
	 */
	public static void saveLoggedUser(String loggedUserStr, Context context) {
		context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
				.edit().putString(LOGGED_USER, loggedUserStr).commit();
	}

	/**
	 * Gets the logged user from the android shared preferences
	 */
	public static User getLoggedUser(Context context) {
		String userStr = context.getSharedPreferences(PREFERENCES_FILE,
				Context.MODE_PRIVATE).getString(LOGGED_USER, null);

		if (userStr == null) {
			return null;
		} else {
			try {
				return JSONAdapter.JSONToUser(userStr);
			} catch (JSONException e) {
				Log.e(TAG_LOGGER, "Error parsing the logged user");
			}
		}
		return null;
	}

	/**
	 * Deletes the logged user from the android shared preferences
	 */
	public static void removeLoggedUser(Context context) {
		context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE)
				.edit().remove(LOGGED_USER).commit();
	}

	public static void saveTags(Context context, List<Tag> tags) {
		MessagesDBHelper dbHelper = new MessagesDBHelper(context);
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			Log.i(TAG_LOGGER, "tag count: " + tags.size());
			for (Tag tag : tags) {
				// Container for the values
				ContentValues tagValues = new ContentValues();

				// Values into content: messages table
				tagValues.put(MessagesDBHelper.IDTAG, tag.getId());
				tagValues.put(MessagesDBHelper.TAG, tag.getTagName());

				// Save the message
				db.insertOrThrow(MessagesDBHelper.TAGS_TABLE, null, tagValues);

				Log.i("DATABASE", "Inserted into db");
			}
		} finally {
			// Always close the dbHelper
			dbHelper.close();
		}
	}

	public static List<Tag> getTags(Activity activity) {
		// Get the helper
		MessagesDBHelper dbHelper = new MessagesDBHelper(activity);
		// Create the list
		List<Tag> tags = new ArrayList<Tag>();
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d("DATABASE", "Database obtained");

			// Execute the query
			Cursor cursor = db.query(MessagesDBHelper.TAGS_TABLE, null, null,
					null, null, null, MessagesDBHelper.IDTAG);
			activity.startManagingCursor(cursor);
			Log.d("DATABASE", "Query for messages executed");

			// Extract the results
			while (cursor.moveToNext()) {
				// Add the message
				tags.add(new Tag(cursor.getInt(0), cursor.getString(1)));
			}
			Log.d("DATABASE", "Tags added to list");

		} finally {
			// Always close the helper
			dbHelper.close();
		}
		Log.i("DATABASE", tags.size() + " tags from db");
		return tags;
	}

	public static List<Tag> getMessageTags(Activity activity, int idMessage) {
		// Get the helper
		MessagesDBHelper dbHelper = new MessagesDBHelper(activity);
		// Create the list
		List<Tag> tags = new ArrayList<Tag>();
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d("DATABASE", "Database obtained");

			// Execute the query
			Cursor cursor = db
					.rawQuery(
							"SELECT t.* FROM tags AS t, messages_tags AS mt WHERE t.idtag = mt.idtag AND mt.idmessage = "
									+ idMessage, null);
			activity.startManagingCursor(cursor);
			Log.d("DATABASE", "Query for tags executed");

			// Extract the results
			while (cursor.moveToNext()) {
				// Add the message
				tags.add(new Tag(cursor.getInt(0), cursor.getString(1)));
			}
			Log.d("DATABASE", "Tags added to list");

		} finally {
			// Always close the helper
			dbHelper.close();
		}
		Log.i("DATABASE", tags.size() + " tags from db");
		return tags;
	}

	/**
	 * Gets the the id of the last message saved in database
	 */
	public static Message getLastMessage(Activity activity) {
		// Messages are ordered by its id
		List<Message> messages = new ArrayList<Message>();
		addMessages(activity, messages, ALL, MessagesDBHelper.IDMESSAGE);
		if(!messages.isEmpty()) {
			return messages.get(0);
		}
		return Message.getZeroMessage();
	}

	/**
	 * Gets the the id of the last tag saved in database
	 */
	public static int getLastIdTag(Activity activity) {
		// Tags are ordered by its id
		List<Tag> tags = getTags(activity);
		if (tags.isEmpty()) {
			return 0;
		}
		return tags.get(tags.size() - 1).getId();
	}

	/**
	 * Gets the the id of the first following message saved in database
	 */
	public static Message getFirstMessageNotFollowing(Activity activity) {
		List<Message> messages = new ArrayList<Message>();
		addMessages(activity, messages, ALL, MessagesDBHelper.DATE);
		User user = getLoggedUser(activity);

		for (int i = messages.size() - 2; i >= 0; i--) {
			Message m = messages.get(i);
			if (m.getAffinity() > 0.0) {
				continue;
			}
			boolean following = false;
			secondFor: for (Tag t1 : m.getTags()) {
				for (Tag t2 : user.getTagsFollowing()) {
					if (t1.equals(t2)) {
						following = true;
						break secondFor;
					}
				}
			}
			if (!following) {
				return m;
			}
		}
		return Message.getZeroMessage();
	}

	/**
	 * Gets the the id of the first message saved in database
	 */
	public static Message getFirstMessageFollowing(Activity activity) {
		MessagesDBHelper dbHelper = new MessagesDBHelper(activity);
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d("DATABASE", "Database obtained");

			String query = "SELECT DISTINCT * FROM messages AS m, messages_tags AS mt WHERE "
					+ MessagesDBHelper.AFFINITY
					+ " = 0.0 AND mt.idtag IN ("
					+ getUserTagsAsStringList(activity)
					+ ") ORDER BY "
					+ MessagesDBHelper.DATE
					+ " ASC, "
					+ MessagesDBHelper.IDMESSAGE + " ASC LIMIT 1";

			Cursor cursor = db.rawQuery(query, null);
			activity.startManagingCursor(cursor);
			Log.d("DATABASE", "Query for messages executed");

			List<Message> messages = new ArrayList<Message>();
			addMessagesFromCursor(messages, cursor, activity);
			if(!messages.isEmpty()) {
				return messages.get(0);
			}
			return Message.getZeroMessage();
		} finally {
			// Always close the subjectsData
			dbHelper.close();
		}
	}

	/**
	 * Gets the the id of the first message saved in database
	 */
	public static Message getFirstMessageRecommended(Activity activity) {
		MessagesDBHelper dbHelper = new MessagesDBHelper(activity);
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d("DATABASE", "Database obtained");

			String query = "SELECT * FROM messages WHERE "
					+ MessagesDBHelper.AFFINITY + " > 0.0 ORDER BY "
					+ MessagesDBHelper.DATE + " ASC, "
					+ MessagesDBHelper.IDMESSAGE + " ASC LIMIT 1";

			Cursor cursor = db.rawQuery(query, null);
			activity.startManagingCursor(cursor);
			Log.d("DATABASE", "Query for messages executed");

			List<Message> messages = new ArrayList<Message>();
			addMessagesFromCursor(messages, cursor, activity);
			if(!messages.isEmpty()) {
				return messages.get(0);
			}
			return Message.getZeroMessage();
		} finally {
			// Always close the subjectsData
			dbHelper.close();
		}
	}

	public static void updateRecommendations(String content, Activity activity) {

		HashMap<Long, Float> recommendations = new HashMap<Long, Float>();
		try {
			JSONAdapter.JSONToRecommendations(content, recommendations);
			Iterator<Long> recomendationsIterator = recommendations.keySet()
					.iterator();
			while (recomendationsIterator.hasNext()) {
				Long idMessage = recomendationsIterator.next();
				Float ratingForIdMessage = recommendations.get(idMessage);
				Message message = getMessage(activity, idMessage.intValue());
				if (message != null) {
					message.setAffinity(ratingForIdMessage);
					updateMessage(activity, message);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
