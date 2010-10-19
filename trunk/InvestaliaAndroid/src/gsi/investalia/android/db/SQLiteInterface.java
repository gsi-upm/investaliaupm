package gsi.investalia.android.db;

import gsi.investalia.domain.Message;
import gsi.investalia.domain.Tag;
import gsi.investalia.domain.User;
import gsi.investalia.json.JSONAdapter;

import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
	public static final String RECOMMENDATIONS = "recomendations";

	public static void saveMessages(Context context, List<Message> messages) {
		MessagesDBHelper dbHelper = new MessagesDBHelper(context);
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			for (Message m : messages) {
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

		int idLoggedUser = getLoggedUser(activity).getId();

		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d("DATABASE", "Database obtained");

			String query = "";
			// If ordering by date, show the newer ones before
			if (which.equals(ALL)) {
				if (orderBy.equals(MessagesDBHelper.IDMESSAGE)) {
					// To determine the last idMessage
					query = "SELECT * FROM messages ORDER BY IDMESSAGE DESC LIMIT 1";
				} else if (orderBy.equals(MessagesDBHelper.DATE)) {
					query = "SELECT * FROM messages ORDER BY DATE DESC";
				}
			} else if (which.equals(FOLLOWING)) {
				query = "SELECT DISTINCT m.* FROM messages AS m, messages_tags AS mt "
						+ "WHERE m.idmessage = mt.idmessage AND idtag IN ("
						+ getUserTagsAsStringList(activity)
						+ ") ORDER BY DATE DESC";
			} else if (which.equals(RECOMMENDATIONS)) {
				query = "SELECT m.* FROM messages AS m, users_recommendations AS ur "
						+ " WHERE m.idMessage = ur.idMessage AND ur.idUser = "
						+ idLoggedUser;
				if (orderBy.equals(MessagesDBHelper.DATE)) {
					query += " ORDER BY DATE DESC";
				} else if (orderBy.equals(MessagesDBHelper.AFFINITY)) {
					query += " ORDER BY ur.user_affinity DESC";
				}
			}

			Cursor cursor = db.rawQuery(query, null);
			activity.startManagingCursor(cursor);
			Log.d("DATABASE", "Query for messages executed");

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
								.getInt(7), cursor.getInt(8), idLoggedUser));
			}
			Log.d("DATABASE", "Messages added to list");

		} finally {
			// Always close the subjectsData
			dbHelper.close();
		}
		Log.i("DATABASE", messages.size() + " messages from db");
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
								.getInt(7), cursor.getInt(8), getLoggedUser(
								activity).getId());
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

			// TODO
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
	public static int getLastIdMessage(Activity activity) {
		// Messages are ordered by its id
		List<Message> messages = new ArrayList<Message>();
		addMessages(activity, messages, ALL, MessagesDBHelper.IDMESSAGE);
		if (messages.isEmpty()) {
			return 0;
		}
		return messages.get(0).getId();
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

	public static void saveUserRecommendations(Context context,
			HashMap<Long, Float> recommendations) {
		MessagesDBHelper dbHelper = new MessagesDBHelper(context);
		try {
			int idUser = getLoggedUser(context).getId();
			// Get the database
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			db.delete(MessagesDBHelper.RECOMMENDATIONS_TABLE,
					MessagesDBHelper.IDUSER + "=" + idUser, null);

			Iterator<Long> recomendationsIterator = recommendations.keySet()
					.iterator();
			while (recomendationsIterator.hasNext()) {

				ContentValues recommendationValues = new ContentValues();

				Long idMessage = recomendationsIterator.next();

				// Values into content: recommendations table
				recommendationValues.put(MessagesDBHelper.IDMESSAGE, idMessage);
				recommendationValues.put(MessagesDBHelper.IDUSER, idUser);
				recommendationValues.put(MessagesDBHelper.AFFINITY,
						recommendations.get(idMessage));

				// Save the recommendation
				db.insertOrThrow(MessagesDBHelper.RECOMMENDATIONS_TABLE, null,
						recommendationValues);

				Log.i("DATABASE", "Inserted into db");
			}
		} finally {
			// Always close the dbHelper
			dbHelper.close();
		}
	}

	public static HashMap<Long, Float> getUserRecommendations(
			Activity activity, int idUser) {
		// Get the helper
		MessagesDBHelper dbHelper = new MessagesDBHelper(activity);

		HashMap<Long, Float> recommendations = new HashMap<Long, Float>();

		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d("DATABASE", "Database obtained");

			// Execute the query
			Cursor cursor = db.query(MessagesDBHelper.RECOMMENDATIONS_TABLE,
					null, MessagesDBHelper.IDUSER
							+ " = "
							+ getLoggedUser(activity.getApplicationContext())
									.getId(), null, null, null, null);
			activity.startManagingCursor(cursor);
			Log.d("DATABASE", "Query for messages executed");

			// Extract the results
			while (cursor.moveToNext()) {
				// Add the message
				recommendations.put(new Long(cursor.getInt(0)), cursor
						.getFloat(1));
			}

		} finally {
			// Always close the helper
			dbHelper.close();
		}

		return recommendations;
	}
}
