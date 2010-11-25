package gsi.investalia.android.jade;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import jade.lang.acl.ACLMessage;

import android.util.Log;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.domain.Message;
import gsi.investalia.domain.Tag;
import gsi.investalia.json.JSONAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

/**
 * This class implements the ACLMessageListener interface and it is called by
 * the agent to process the jade messages and send the broadcast messages
 * to the activities to refresh
 */

public class JadeListener implements ACLMessageListener {

	private Context context; // Necessary for broadcasting
	private Activity activity;
	private final static String TAG_LOGGER = "JADE LISTENER";

	public JadeListener(Activity activity) {
		this.context = activity;
		this.activity = activity;
	}

	public void onMessageReceived(ACLMessage message) {

		// What you should do when you receive the message
		Log.i(TAG_LOGGER,
				"onMessageReceived(): JadeListener has received message");
		Log.v(TAG_LOGGER, "message content: " + message.getContent());

		// Wrong login message
		if (message.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
			Log.i(TAG_LOGGER, "Failure. Wrong user/password. Logging out");
			context.sendBroadcast(new Intent(JadeAdapter.WRONG_LOGIN));

			// Correct login message
		} else if (message.getPerformative() == ACLMessage.INFORM) {
			Log.i(TAG_LOGGER, "Accept proposal. Logged in");

			// Save the user
			SQLiteInterface.saveLoggedUser(message.getContent(), context);

			// Decide all messages
			SQLiteInterface.deleteAllMessages(context);

			// Correct parsing: logged user
			context.sendBroadcast(new Intent(JadeAdapter.LOGGED_IN));
			
			// Correct message sent
		} else if (message.getPerformative() == ACLMessage.CONFIRM) {
			Log.i(TAG_LOGGER, "Confirm. Message sent");
			context.sendBroadcast(new Intent(JadeAdapter.MESSAGE_OK));

			// Correct message received
		} else if (message.getPerformative() == ACLMessage.PROPOSE) {
			Log.i(TAG_LOGGER, "Propose. Messages downloaded");
			try {
				// Save the messages and tags to db
				Log.i(TAG_LOGGER, "json message list: " + message.getContent());
				List<Message> messages = new ArrayList<Message>();
				List<Tag> tags = new ArrayList<Tag>();
				JSONAdapter.JSONToMessageListAndTagList(message.getContent(),
						messages, tags);
				SQLiteInterface.saveMessages(context, activity, messages);
				SQLiteInterface.saveTags(context, tags);

				// Broadcast reception
				context.sendBroadcast(new Intent(
						JadeAdapter.MESSAGES_DOWNLOADED));
				Log.i(TAG_LOGGER, "Messages downloaded broadcast sent");
			} catch (JSONException e) {
				Log.e(TAG_LOGGER, "Error parsing JSON");
			}
		
			// User created
		} else if (message.getPerformative() == ACLMessage.AGREE) {
			Log.i(TAG_LOGGER, "User created");
			context.sendBroadcast(new Intent(JadeAdapter.USER_CREATED));
			
			// User non created
		} else if (message.getPerformative() == ACLMessage.DISCONFIRM) {
			Log.i(TAG_LOGGER, "Wrong new user");
			context.sendBroadcast(new Intent(JadeAdapter.WRONG_NEW_USER));
		
			// Profile user modified
		} else if (message.getPerformative() == ACLMessage.INFORM_REF) {
			Log.i(TAG_LOGGER, "User modified");
			// Save the user
			SQLiteInterface.saveLoggedUser(message.getContent(), context);
			// Delete old messages
			SQLiteInterface.deleteAllMessages(context);
			context.sendBroadcast(new Intent(JadeAdapter.USER_UPDATED));
			
			// Recommendations updated 
		} else if (message.getPerformative() == ACLMessage.PROPAGATE) {
			Log.i(TAG_LOGGER, "Recommendations updated");
			SQLiteInterface.updateRecommendations(message.getContent(), activity);
		}
	}
}