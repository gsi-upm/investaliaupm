package gsi.investalia.android.jade;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.domain.Message;
import gsi.investalia.domain.User;
import gsi.investalia.json.JSONAdapter;
import jade.android.ConnectionListener;
import jade.android.JadeGateway;
import jade.core.AID;
import jade.core.Profile;
import jade.core.behaviours.OneShotBehaviour;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.lang.acl.ACLMessage;
import jade.util.leap.Properties;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;

import java.net.ConnectException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

/**
 * Clase que actúa de interfaz de la interacción con el agente JADE. Contiene
 * los métodos necesarios para la correcta conexión y para la comunicación con
 * el agente.
 * 
 */
public class JadeAdapter implements ConnectionListener {

	// Jade
	private static final String TAG_JADE = "JADE";
	private static final String IP = "10.0.2.2";
	private static final String PORT = "1099";
	private JadeGateway gateway;
	private JadeListener listener;
	private static boolean isConnected = false;

	// App
	private static final String TAG_LOGGER = "Adapter";
	private Activity activity;

	// Broadcast actions
	public static final String LOGGED_IN = "logged_in";
	public static final String WRONG_LOGIN = "wrong_login";
	public static final String MESSAGE_OK = "message_ok";
	public static final String MESSAGE_FAIL = "message_fail";
	public static final String MESSAGES_DOWNLOADED = "messages_donwloaded";
	public static final String USER_CREATED = "user_created";
	public static final String WRONG_NEW_USER = "wrong_new_user";
	public static final String USER_UPDATED = "user_created";
	public static final String USER_NOT_UPDATED = "user_not_updated";
	public static final String JADE_CONNECTED = "jade_connected";
	public static final String JADE_DISCONNECTED = "jade_disconnected";

	// Server agent names
	public static final String LOGIN_AGENT = "login";
	public static final String POSTING_AGENT = "posting";
	public static final String DOWNLOAD_MESSAGES_AGENT = "downloadMessages";
	public static final String NEW_USER_AGENT = "newUser";
	public static final String UPDATE_USER_AGENT = "updateUser";
	public static final String UPDATE_MESSAGE_AGENT = "updateMessage";

	// Android agent name (used when logging)
	public static final String ANDROID_LOGIN_AGENT = "login_android";

	public JadeAdapter(Activity activity) {
		this.activity = activity;
	}

	public void checkLogin(User user) {
		Log.v("ANDROID", "Try to log, username: " + user.getUserName());
		try {
			// Send the message
			gateway.execute(new SenderBehaviour(LOGIN_AGENT, JSONAdapter
					.userToJSON(user).toString()));
		} catch (Exception e) {
			Log.e("ANDROID", "Error connecting to JADE (login)");
		}
	}

	public void saveNewMessage(Message message) {
		Log.i(TAG_LOGGER, "Save new message");
		// Send the message
		try {
			gateway.execute(new SenderBehaviour(POSTING_AGENT, JSONAdapter
					.messageToJSON(message).toString()));
		} catch (Exception e) {
			Log.e("ANDROID", "Error connecting to JADE (save message)");
		}
	}

	/**
	 * Refreshes times read and liked after the message is read
	 */
	public void updateMessage(Message message) {
		Log.i(TAG_LOGGER, "Update message");
		// Send the message
		try {
			gateway.execute(new SenderBehaviour(UPDATE_MESSAGE_AGENT,
					JSONAdapter.messageToJSON(message).toString()));
		} catch (Exception e) {
			Log.e("ANDROID", "Error connecting to JADE (update message)");
			e.printStackTrace();
		}
	}

	public void donwloadNewMessages() {
		Log.i("ANDROID", "Ask for new messages");
		// Get the loggedUser
		User loggedUser = SQLiteInterface.getLoggedUser(activity);

		// Pass the last update and last tag in the content
		String content;
		try {
			content = JSONAdapter.updatesToJSON(loggedUser.getLastUpdate(),
					SQLiteInterface.getLastIdTag(activity)).toString();
		} catch (JSONException e1) {
			Log.e(TAG_LOGGER, "Error parsing JSON (download messages)");
			return;
		}

		// Send the message
		try {
			gateway.execute(new SenderBehaviour(DOWNLOAD_MESSAGES_AGENT, content));
		} catch (Exception e) {
			Log.e("ANDROID", "Error connecting to JADE (download messages)");
		}
	}

	public void updateUser(User user) {
		String jsonStr;
		try {
			jsonStr = JSONAdapter.userToJSON(user).toString();
		} catch (JSONException e) {
			Log.e(TAG_LOGGER, "Error parsing JSON (update user)");
			return;
		}
		Log.i(TAG_LOGGER, "Update user: " + jsonStr);
		try {
			gateway.execute(new SenderBehaviour(UPDATE_USER_AGENT, jsonStr));
		} catch (Exception e) {
			Log.e("ANDROID", "Error connecting to JADE (update user)");
		}
	}

	public void newUser(User user) {
		Log.i("ANDROID", "Try to create new user: " + user.getUserName());
		try {
			gateway.execute(new SenderBehaviour(NEW_USER_AGENT, JSONAdapter
					.userToJSON(user).toString()));
		} catch (Exception e) {
			Log.e("ANDROID", "Error connecting to JADE (new user)");
		}
	}

	public static boolean isConnected() {
		return isConnected;
	}

	public void jadeConnect() {
		Log.v("LOGIN", "Starting Jade connection");

		// Get the agent name
		String agentName;
		User loggedUser = SQLiteInterface.getLoggedUser(activity);
		if (loggedUser == null) {
			// When logging, the agent name is irrelevant
			agentName = ANDROID_LOGIN_AGENT;
		} else {
			agentName = loggedUser.getUserName();
		}
		Log.i(TAG_LOGGER, "Agent name: " + agentName);

		// Create the listener
		listener = new JadeListener(activity);

		// Create JADE properties class
		Properties props = new Properties();

		// TODO: IMPORTANTE: añadir el host y puerto en el strings.xml
		props.setProperty(Profile.MAIN_HOST, IP);
		props.setProperty(Profile.MAIN_PORT, PORT);
		props.setProperty(JICPProtocol.MSISDN_KEY, agentName);

		try {
			JadeGateway.connect(AndroidAgent.class.getName(), props, activity,
					this);
			isConnected = true;
		} catch (Exception e) {
			Log.w("LOGIN", "Error connecting");
			Log.e("jade.android", e.getMessage(), e);
		}
	}

	@Override
	public void onConnected(JadeGateway gw) {
		Log.v(TAG_JADE, "Calling onConnected method");
		gateway = gw;

		try {
			if (activity == null) {
				Log.v("CONTEXT", "null");
			}
			// Pass the listener
			gateway.execute(listener);
			Log.v(TAG_JADE, "Agent created succesfully");
			activity.sendBroadcast(new Intent(JADE_CONNECTED));

		} catch (Exception e) {
			Log.w(TAG_JADE, "Error creating agent");
			Log.e("jade.android", e.getMessage(), e);
		}
	}

	public void jadeDisconnect() {
		Log.v(TAG_JADE, "Starting Jade disconnection");

		if (gateway == null) {
			Log.v(TAG_JADE, "The connection wasn't established.");
			return;
		}

		// Shutdown
		try {
			gateway.shutdownJADE();
		} catch (ConnectException e) {
			Log.w(TAG_JADE, "Error disconnecting Jade");
			Log.e("jade.android", e.getMessage(), e);
		}

		// Disconnect to service
		gateway.disconnect(activity);
		isConnected = false;
		activity.sendBroadcast(new Intent(JADE_DISCONNECTED));

		Log.v(TAG_JADE, "Disconnection successfull");
	}

	public void jadeRestart() {
		jadeDisconnect();
		jadeConnect();
	}

	@Override
	public void onDisconnected() {
		Log.v(TAG_JADE, "Disconnected to Jade");
		// TODO it's not called!! why???
	}

	private class SenderBehaviour extends OneShotBehaviour {

		/** ACLMessage to be sent */
		private ACLMessage message;

		public SenderBehaviour(String receiver, String content) {
			message = new ACLMessage(ACLMessage.CFP);
			message.setContent(content);
			message.addReceiver(new AID(receiver, AID.ISLOCALNAME));
		}

		/** Sends the message. Executed by JADE agent. */
		public void action() {
			Log.i(TAG_LOGGER, "Sending message " + message.toString());
			myAgent.send(message);
		}
	}
}
