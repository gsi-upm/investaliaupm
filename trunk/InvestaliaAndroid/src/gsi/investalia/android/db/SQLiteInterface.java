package gsi.investalia.android.db;

import gsi.investalia.domain.Message;
import gsi.investalia.domain.Tag;
import gsi.investalia.domain.User;
import gsi.investalia.json.JSONAdapter;

import java.util.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
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

	private static final String DATE_FORMAT = "yyyy-MM-dd";
	private static final String PREFERENCES_FILE = "main";
	private static final String LOGGED_USER = "logged_user";
	private static final String TAG_LOGGER = "Database";

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
		} finally {
			// Always close the dbHelper
			dbHelper.close();
		}
	}

	public static void addMessages(Activity activity, List<Message> messages,
			String orderBy) {
		// Get the helper
		MessagesDBHelper dbHelper = new MessagesDBHelper(activity);
		// Clear the list
		messages.clear();
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getReadableDatabase();
			Log.d("DATABASE", "Database obtained");

			// Execute the query
			Cursor cursor = db.query(MessagesDBHelper.MESSAGES_TABLE, null,
					null, null, null, null, orderBy);
			activity.startManagingCursor(cursor);
			Log.d("DATABASE", "Query for messages executed");

			// Extract the results
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
				messages.add(new Message(cursor.getInt(0), cursor.getString(1),
						cursor.getString(2), cursor.getString(3), null, date,
						1 == cursor.getInt(5), 1 == cursor.getInt(6), cursor
								.getInt(7), cursor.getInt(8)));
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
						cursor.getString(2), cursor.getString(3), null, date,
						1 == cursor.getInt(5), 1 == cursor.getInt(6), cursor
								.getInt(7), cursor.getInt(8));
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
	public static void saveLoggedUser(User loggedUser, Context context) {
		try {
			context
					.getSharedPreferences(PREFERENCES_FILE,
							Context.MODE_PRIVATE).edit().putString(LOGGED_USER,
							JSONAdapter.userToJSON(loggedUser).toString())
					.commit();
		} catch (JSONException e) {
			Log.e(TAG_LOGGER, "Error saving the logged user");
		}
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

	// Text for example messages
	private static final String text = "La señal de alarma que ayer encendió en Europa la rebaja de la calidad de la deuda griega al nivel de bono basura y la llamada de atención sobre Portugal sigue sonando en los principales mercados: Bolsas, euro y deuda. En este último, hoy está teniendo un especial protagonismo España, cuya prima de riesgo ha marcado un máximo desde que entró en la eurozona en 2000 por la hipótesis de que será la próxima en ver recortada su calificación. La incertidumbre sobre el futuro del país mediterráneo, la demostración de que la crisis ha cruzado ya sus fronteras y se dirige hacia Portugal y el posible contagio al resto de países con una situación fiscal delicada como España, Irlanda y, en menor medida, Italia, está pesando demasiado en el ánimo de los inversores como para dar por finiquitado el ajuste con el varapalo sufrido en la jornada anterior.";

	public static void saveExampleMessages(Context context) {
		List<Message> messages = new ArrayList<Message>();
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

		try {
			messages.add(new Message(1, "Locke",
					"Nuevos planes de pensiones del santander", text,
					new ArrayList<Tag>(), dateFormat.parse("2010-04-10"), true,
					true, 7, 5));
			messages.add(new Message(2, "Locke",
					"Google presenta resultados y no convencen", text,
					new ArrayList<Tag>(), dateFormat.parse("2010-04-11"), true,
					true, 7, 5));
			messages.add(new Message(3, "Locke",
					"BBVA y los mensajes contradictorios", text,
					new ArrayList<Tag>(), dateFormat.parse("2010-04-10"), true,
					true, 7, 5));
			messages.add(new Message(4, "Locke",
					"El IPC se situa en el 1,4% en el mes de marzo", text,
					new ArrayList<Tag>(), dateFormat.parse("2010-04-09"), true,
					true, 7, 5));
			messages.add(new Message(5, "Locke",
					"¿Ha sido Saab la marca peor gestionada?", text,
					new ArrayList<Tag>(), dateFormat.parse("2010-04-12"), true,
					true, 7, 5));
			messages.add(new Message(6, "Locke",
					"Nuevos planes de pensiones del santander", text,
					new ArrayList<Tag>(), dateFormat.parse("2010-04-10"), true,
					true, 7, 5));
			messages.add(new Message(7, "Locke",
					"BBVA y los mensajes contradictorios", text,
					new ArrayList<Tag>(), dateFormat.parse("2010-04-9"), true,
					true, 7, 5));
		} catch (ParseException e) {
			Log.e("DATABASE", "Error parsing the date in example messages");
		}
		saveMessages(context, messages);
	}

	private static void saveExampleTags(Context context) {
		List<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag(1, "Ibex 35"));
		tags.add(new Tag(2, "Mercados internacionales"));
		tags.add(new Tag(3, "Divisas"));
		tags.add(new Tag(4, "Hipotecas"));
		tags.add(new Tag(5, "Bancos y cajas"));
		tags.add(new Tag(6, "Empresas"));
		tags.add(new Tag(7, "Inversiones"));
		tags.add(new Tag(8, "Laboral y empleo"));
		tags.add(new Tag(9, "Off-Topic"));
		
		saveTags(context, tags);
	}
	
	public static void saveTags(Context context, List<Tag> tags) {
		MessagesDBHelper dbHelper = new MessagesDBHelper(context);
		try {
			// Get the database
			SQLiteDatabase db = dbHelper.getWritableDatabase();

			for (Tag tag : tags) {
				// Container for the values
				ContentValues tagValues = new ContentValues();

				// Values into content: messages table
				tagValues.put(MessagesDBHelper.IDTAG, tag.getId());
				tagValues.put(MessagesDBHelper.TAG, tag.getTagName());				

				// Save the message
				db.insertOrThrow(MessagesDBHelper.TAGS_TABLE, null,
						tagValues);
			
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
			Cursor cursor = db.query(MessagesDBHelper.TAGS_TABLE, null,
					null, null, null, null, null);
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

	

	/**
	 * TODO delete
	 * IDUSER(posicion dentro del array), USERNAME, PASSWORD, NAME, LOCATION,
	 * EMAIL
	 */
	private static final String[][] USERS = {
			{ "Jack", "1234", "Jack", "The island", "jack@email.com" },
			{ "Desmond", "1234", "Desmond", "The island", "des@email.com" },
			{ "Kate", "1234", "Kate Austen", "The island", "kate@email.com" },
			{ "Locke", "1234", "John Locke", "The island", "locke@email.com" },
			{ "Sawyer", "1234", "James Ford", "The island", "sawyer@email.com" }, };
}
