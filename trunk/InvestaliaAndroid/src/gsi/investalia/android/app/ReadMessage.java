package gsi.investalia.android.app;

import java.text.SimpleDateFormat;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.domain.Message;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;

public class ReadMessage extends Activity {

	private static final String TAG_LOGGER = "ReadMessage";
	private CheckBox liked;
	private Message message;
	private static final String DATE_FORMAT_SHOW = "dd/MM/yyyy";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.read_message);

		// Get the message
		Bundle bun = getIntent().getExtras();
		message = SQLiteInterface.getMessage(this, bun.getInt("message_id"));
		message.setRead(true);
		
		// Save that is read
		SQLiteInterface.updateMessage(ReadMessage.this, message);

		// Get the views
		TextView user = (TextView) findViewById(R.id.read_user);
		TextView date = (TextView) findViewById(R.id.read_date);
		TextView title = (TextView) findViewById(R.id.read_title);
		TextView text = (TextView) findViewById(R.id.read_text);
		TextView text_score = (TextView) findViewById(R.id.read_score);
		RatingBar stars = (RatingBar) findViewById(R.id.read_RatingBar);
		liked = (CheckBox) findViewById(R.id.read_liked);
		liked.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/* Saves into db 'read' and 'liked'. 
				 * MessageList will send it to server
				 * Done here instead of in the onDestroy to simply
				 * avoid problems of synchronizing
				 */
				message.setLiked(liked.isChecked());
				SQLiteInterface.updateMessage(ReadMessage.this, message);
				Log.v(TAG_LOGGER, "Liked message: " + liked.isChecked());		
			}
		});

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
}
