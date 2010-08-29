package gsi.investalia.android.jade;

import java.util.List;

import org.json.JSONException;

import jade.lang.acl.ACLMessage;

import android.util.Log;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.domain.Message;
import gsi.investalia.domain.User;
import gsi.investalia.json.JSONAdapter;

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

	private Context context; // Necessary for broadcasting
	private final static String TAG_LOGGER = "JADE LISTENER";

	public JadeListener(Context context) {
		this.context = context;
	}


	public void onMessageReceived(ACLMessage message) {
		
		// What you should do when you receive the message
		Log.i(TAG_LOGGER, "onMessageReceived(): JadeListener has received message");

		// Wrong login message
		if (message.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
			Log.i(TAG_LOGGER, "Failure. Wrong user/password. Logging out");
			context.sendBroadcast(new Intent(JadeAdapter.WRONG_LOGIN));
			
		// Correct login message	
		} else if (message.getPerformative() == ACLMessage.INFORM) {
			Log.i(TAG_LOGGER, "Accept proposal. Logged in");	
			try {
				// Save the logged user in the phone memory
				User loggedUser = JSONAdapter.JSONToUser(message.getContent());
				SQLiteInterface.saveLoggedUser(loggedUser, context);
				
				// TODO Decide if deleting or not
				SQLiteInterface.deleteAllMessages(context);
				
				// Correct parsing: logged user
				context.sendBroadcast(new Intent(JadeAdapter.LOGGED_IN));

			} catch (JSONException e) {
				Log.e(TAG_LOGGER, "Error parsing JSON");
				
				// Error parsing: not logged user
				context.sendBroadcast(new Intent(JadeAdapter.WRONG_LOGIN));
			}
			
		} else if (message.getPerformative() == ACLMessage.CONFIRM){			
			Log.i(TAG_LOGGER, "Accept proposal. Logged in");	
			//TODO
			context.sendBroadcast(new Intent(JadeAdapter.MESSAGE_OK));	
			
		}else if (message.getPerformative() == ACLMessage.PROPOSE) {
			Log.i(TAG_LOGGER, "Propose. Messages downloaded");	
			try {		
				// Save the messages to db
				Log.i(TAG_LOGGER, "json message list: " + message.getContent());
				List<Message> messages = JSONAdapter.JSONToMessageList(message.getContent());
				SQLiteInterface.saveMessages(context, messages); 
				
				// Save new lastUpdate
				if (!messages.isEmpty()) {
					int newLastUpdate = messages.get(messages.size() - 1).getId();
					User loggedUser = SQLiteInterface.getLoggedUser(context);
					loggedUser.setLastUpdate(newLastUpdate);
					SQLiteInterface.saveLoggedUser(loggedUser, context);
				}
				
				// Broadcast reception
				context.sendBroadcast(new Intent(JadeAdapter.MESSAGES_DOWNLOADED));
				Log.i(TAG_LOGGER, "Messages downloaded broadcast sent");
			} catch (JSONException e) {
				Log.e(TAG_LOGGER, "Error parsing JSON");			
			}	
		} 	
	}
}