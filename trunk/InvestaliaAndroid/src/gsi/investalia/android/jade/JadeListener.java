package gsi.investalia.android.jade;

import org.json.JSONException;

import jade.lang.acl.ACLMessage;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;
import gsi.investalia.android.app.Login;
import gsi.investalia.android.app.Main;
import gsi.investalia.android.app.R;
import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.domain.User;
import gsi.investalia.json.JSONAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;


/**
 * This class implements the ACLMessageListener interface and it is called by
 * the agent to update the GUI when a message is received
 * 
 * @author Stefano Semeria Reply Cluster
 * @author Tiziana Trucco Telecomitalia
 */

public class JadeListener implements ACLMessageListener {

	private Handler handl;
	private Activity act;
	private Context context; // Necessary for broadcasting
	private final static String TAG_LOGGER = "JADE LISTENER";

	public JadeListener(Context context) {
		this.context = context;
	}


	public void onMessageReceived(ACLMessage message) {
		Log.v("ANDROID", "onMessageReceived(): GuiUpdater has received message");
		// What you should do when you receive the message

		Log.v(TAG_LOGGER, "onMessageReceived(): JadeListener has received message");

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
		} else if (message.getPerformative() == ACLMessage.CONFIRM){			
			Log.v(TAG_LOGGER, "Accept proposal. Logged in");	
			//TODO
			context.sendBroadcast(new Intent(JadeAdapter.MESSAGE_OK));
			
			
		}
	}

	public Activity getActivity() {
		return act;
	}


}