package gsi.investalia.android.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper class for working with the database. When the Android app tries to
 * access to the db, this class creates the database if its not already created,
 * and upgrades the version if a new one is available.
 */
public class MessagesDBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "messages.db";
	private static final int DATABASE_VERSION = 7;

	// Messages table
	public static final String MESSAGES_TABLE = "messages";
	public static final String IDMESSAGE = "idmessage";
	public static final String USERNAME = "username";
	public static final String TITLE = "title";
	public static final String TEXT = "text";
	public static final String DATE = "date";
	public static final String LIKED = "liked";
	public static final String READ = "read";
	public static final String RATING = "rating";
	public static final String TIMES_READ = "times_read";

	// Tags table
	public static final String TAGS_TABLE = "tags";
	public static final String IDTAG = "idtag";
	public static final String TAG = "tag";
	
	// Messages_tags table
	public static final String MESSAGES_TAGS_TABLE = "messages_tags";
	public static final String IDMESSAGE_TAG = "idmessage_tag";
	

	/** Create a helper object for the Messages database */
	public MessagesDBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + MESSAGES_TABLE + " (" + IDMESSAGE
				+ " INTEGER PRIMARY KEY, " + USERNAME + " TEXT," + TITLE
				+ " TEXT," + TEXT + " TEXT," + DATE + " DATE," + LIKED
				+ " BOOLEAN," + READ + " BOOLEAN," + RATING + " INTEGER,"
				+ TIMES_READ + " INTEGER);");
		db.execSQL("CREATE TABLE " + TAGS_TABLE + " (" + IDTAG
				+ " INTEGER PRIMARY KEY, " + TAG + " TEXT);");
		db.execSQL("CREATE TABLE " + MESSAGES_TAGS_TABLE + " (" + IDMESSAGE_TAG
				+ " INTEGER PRIMARY KEY, " + IDMESSAGE + " INT," + IDTAG
				+ " INT);");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + TAGS_TABLE);
		db.execSQL("DROP TABLE IF EXISTS " + MESSAGES_TAGS_TABLE);
		onCreate(db);
	}
}