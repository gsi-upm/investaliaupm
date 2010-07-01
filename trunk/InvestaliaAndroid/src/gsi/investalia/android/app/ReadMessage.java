package gsi.investalia.android.app;

import java.text.SimpleDateFormat;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.android.jade.JadeAdapter;
import gsi.investalia.domain.Message;
import gsi.investalia.domain.User;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;

public class ReadMessage extends Activity {

	private static final String TAG_LOGGER = "ReadMessage";
	private CheckBox liked;
	private Message message;
	private User loggedUser;
	private static final String DATE_FORMAT_SHOW = "dd/MM/yyyy";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read_message);

		// Get the user
		loggedUser = SQLiteInterface.getLoggedUser(this);

		// Get the message
		Bundle bun = getIntent().getExtras();
		message = SQLiteInterface.getMessage(this, bun.getInt("message_id"));

		// Get the views
		TextView user = (TextView) findViewById(R.id.read_user);
		TextView date = (TextView) findViewById(R.id.read_date);
		TextView title = (TextView) findViewById(R.id.read_title);
		TextView text = (TextView) findViewById(R.id.read_text);
		TextView text_score = (TextView) findViewById(R.id.read_score);
		RatingBar stars = (RatingBar) findViewById(R.id.read_RatingBar);
		liked = (CheckBox) findViewById(R.id.read_liked);

		user.setText(message.getUserName());
		String dateStr = new SimpleDateFormat(DATE_FORMAT_SHOW).format(message
				.getDate());
		date.setText(dateStr);
		title.setText(message.getTitle());
		text.setText(message.getText());

		// TODO ¿how to handle score?
		float score = (float) 4.20;
		text_score.setText(getString(R.string.score) + ": " + score + "/5.0");
		stars.setRating(score);
	}

	/*
	 * Al cerrar la actividad se actualiza el contador de cuantas veces se ha
	 * leido el mensaje y si se ha marcado la casilla de Me gusta.
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	public void onDestroy() {
		super.onDestroy();
		message.setRead(true);
		message.setLiked(liked.isChecked());
		JadeAdapter.updateMessageData(loggedUser.getId(), message);
		Log.v(TAG_LOGGER, "Me gusta: " + liked.isChecked());
		Log.v(TAG_LOGGER, "User: " + loggedUser.getId() + " Mess ID: "
				+ message.getId());
	}
}
