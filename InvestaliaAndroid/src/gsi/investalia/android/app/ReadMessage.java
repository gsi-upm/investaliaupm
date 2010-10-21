package gsi.investalia.android.app;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.domain.Message;
import gsi.investalia.domain.User;
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
		if(!message.isRead()) {
			message.setTimesRead(message.getTimesRead() + 1);
			message.setRead(true);
		}
		
		// Save that is read
		SQLiteInterface.updateMessage(ReadMessage.this, message);

		// Get the views
		TextView user = (TextView) findViewById(R.id.read_user);
		TextView date = (TextView) findViewById(R.id.read_date);
		TextView title = (TextView) findViewById(R.id.read_title);
		TextView text = (TextView) findViewById(R.id.read_text);
		
		liked = (CheckBox) findViewById(R.id.read_liked);
		liked.setChecked(message.isLiked());
		User loggedUser = SQLiteInterface.getLoggedUser(this);
		liked.setEnabled(!message.getUserName().equalsIgnoreCase(loggedUser.getUserName()));
		liked.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/* Saves into db 'read' and 'liked'. 
				 * MessageList will send it to server
				 * Done here instead of in the onDestroy to simply
				 * avoid problems of synchronizing (if its done in the
				 * onDestroy the jade connection may start before the query
				 * is ready
				 */
				message.setLiked(liked.isChecked());
				if(message.isLiked()) {
					message.setRating(message.getRating() + 1);
				} else {
					message.setRating(message.getRating() - 1);
				}
				setScore();
				SQLiteInterface.updateMessage(ReadMessage.this, message);
				Log.v(TAG_LOGGER, "Liked message: " + liked.isChecked());		
			}
		});

		user.setText("@" + message.getUserName());
		String dateStr = new SimpleDateFormat(DATE_FORMAT_SHOW).format(message
				.getDate());
		date.setText(dateStr);
		title.setText(message.getTitle());
		text.setText(message.getText());

		// Set the score
		setScore();
	}
	
	public void setScore() {
		// Get the views 
		TextView textScore = (TextView) findViewById(R.id.read_score);
		RatingBar stars = (RatingBar) findViewById(R.id.read_RatingBar);
		
		// Calculate the score
		float score = (float) 5.0 * message.getRating() / message.getTimesRead();
		
		// Set the values
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		textScore.setText(getString(R.string.score) + ": " + nf.format(score) + "/5.0 (leído " + message.getTimesRead() + " veces)");
		stars.setRating(score);
	}
}
