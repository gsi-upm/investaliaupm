package gsi.investalia.android.jade;

import org.json.JSONException;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.domain.User;
import gsi.investalia.json.JSONAdapter;
import jade.lang.acl.ACLMessage;

import android.util.Log;
import android.content.Context;
import android.content.Intent;

/**
 * This class is called by the agent to send notifications to the UI
 */
public class JadeListener {

	private Context context; // Necessary for broadcasting
	private final static String TAG_LOGGER = "JADE LISTENER";

	public JadeListener(Context context) {
		this.context = context;
	}

	public void onMessageReceived(ACLMessage message) {
		Log.v(TAG_LOGGER,
				"onMessageReceived(): JadeListener has received message");

		if (message.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
			Log.v(TAG_LOGGER, "Failure. Wrong user/password combination. Logging out");
			context.sendBroadcast(new Intent(JadeAdapter.WRONG_LOGIN));
		} else if (message.getPerformative() == ACLMessage.INFORM) {
			Log.v(TAG_LOGGER, "Accept proposal. Logged in");	
			try {
				User loggedUser = JSONAdapter.JSONToUser(message.getContent());
				SQLiteInterface.saveLoggedUser(loggedUser, context);
			} catch (JSONException e) {
				Log.e(TAG_LOGGER, "Error parsing JSON");
			}
			context.sendBroadcast(new Intent(JadeAdapter.LOGGED_IN));
		}
	}
}
