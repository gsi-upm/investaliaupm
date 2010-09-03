package gsi.investalia.android.jade;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.domain.Message;
import gsi.investalia.domain.User;
import gsi.investalia.json.JSONAdapter;
import jade.android.ConnectionListener;
import jade.android.JadeGateway;
import jade.core.Profile;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.util.leap.Properties;
import java.net.ConnectException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
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

	// Agent actions
	public static final String CHECK_LOGIN = "check_login";
	public static final String SAVE_MESSAGE = "save_message";
	public static final String NEW_USER = "new_user";
	public static final String UPDATE_USER = "update_user";
	public static final String DOWNLOAD_MESSAGES = "download_messages";

	public JadeAdapter(Activity activity) {
		this.activity = activity;
	}

	public void checkLogin(User user) {
		Log.v("ANDROID", "Try to log, username: " + user.getUserName());
		try {
			// We pass the action and the user
			String[] args = { CHECK_LOGIN,
					JSONAdapter.userToJSON(user).toString() };
			jadeConnect(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void saveNewMessage(User user, Message message) {

		Log.v(TAG_LOGGER, "User name: " + user.getName() + " Titulo: "
				+ message.getTitle() + " Texto: " + message.getText()
				+ " Fecha: " + message.getDate().toLocaleString()
				+ " Topics primer ID: ");
		// + message.getTags().get(0).getTagName());

		try {
			Log.v(TAG_LOGGER, "enviando mensaje de " + user.getName());
			// We pass the action, the user and the message
			String[] args = { SAVE_MESSAGE,
					JSONAdapter.messageToJSON(message).toString() };
			jadeConnect(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Cuando un usuario cierra un mensaje se llama a este método para indicar
	 * si le ha gustado el mensaje y actualizar el número de veces que se ha
	 * leido
	 * 
	 * @param userId
	 *            ID del usuario que lee el mensaje
	 * @param message
	 *            mensaje que está leyendo
	 */
	public static void updateMessageData(int userId, Message message) {
		// TODO Auto-generated method stub
		// Cada vez que se llama a este método tiene que incrementarse un
		// contador con las veces que se ha leido este mensaje
		// de forma global, o solo las veces que este usuario ha leido este
		// mensaje

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
			// TODO
			Log.i(TAG_LOGGER, "content: " + content);
		} catch (JSONException e1) {
			Log.e(TAG_LOGGER, "Error parsing JSON to ask for updates");
			return;
		}
		String[] args = { DOWNLOAD_MESSAGES, content };
		
		// Send the message
		try {
			jadeConnect(args);
		} catch (Exception e) {
			Log.e("ANDROID", "Error connecting to JADE");
		}
	}

	
	public void setUserProfile(User user) {
		String jsonStr;
		try {
			jsonStr = JSONAdapter.userToJSON(user).toString();
		} catch (JSONException e) {
			Log.e(TAG_LOGGER, "JSON Exception parsing user to update");
			return;
		}
		Log.i(TAG_LOGGER, "Update user: " + jsonStr);
		String[] args = { UPDATE_USER, jsonStr };
		// TODO
		Log.i(TAG_LOGGER, "agent action jadeadapter: " + args[0]);
		jadeConnect(args);
	}

	/**
	 * Método para registrar un nuevo usuario
	 * 
	 * @param user
	 *            nuevo usuario
	 * @return true si el usuario se crea correctamente, false si se produce
	 *         algun error
	 */
	public void newUser(User user) {
		try {
			Log.i("ANDROID", "Try to create new user: " + user.getUserName());
			// We pass the action and the user
			String[] args = { NEW_USER, JSONAdapter.userToJSON(user).toString() };
			jadeConnect(args);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void jadeConnect(String[] args) {
		Log.v("LOGIN", "Starting Jade connection");
		Log.i(TAG_LOGGER, "zzz Agent action: " + args[0]);

		// Create the listener
		listener = new JadeListener(activity);

		// Create JADE properties class
		Properties props = new Properties();
		User user = new User("void", "void");
		
		if(args[0].equals(CHECK_LOGIN)) {
			try {
				// Get the user
				JSONObject jsonObj = new JSONObject(args[1]);
				user = JSONAdapter.JSONToUser(jsonObj.toString());
				Log.v(TAG_JADE, "Agent name: " + user.getUserName());

			} catch (JSONException e1) {
				Log.v("JSON ERROR", e1.getMessage());
			}
		}
		else {
			user = SQLiteInterface.getLoggedUser(activity);
		}
		
		// TODO: IMPORTANTE: añadir el host y puerto en el strings.xml
		props.setProperty(Profile.MAIN_HOST, IP);
		props.setProperty(Profile.MAIN_PORT, PORT);
		props.setProperty(JICPProtocol.MSISDN_KEY, user.getUserName());

		try {
			JadeGateway.connect(AndroidAgent.class.getName(), args, props,
					activity, this);
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
			if (activity == null)
				Log.v("CONTEXT", "null");
			gateway.execute(activity);
			gateway.execute(listener);
			Log.v(TAG_JADE, "Agent created succesfully");

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

		// // First we restart secretary agent workflow with a new android
		// // behaviour
		// try {
		// gateway.execute(new RestartBehaviour());
		// Log.v(TAG_JADE, "Workflow restarted");
		// } catch (Exception e) {
		// Log.e(TAG_JADE, "Error restarting secretary workflow");
		// // TODO We should finish the workflow and start a new one
		// }

		try {
			gateway.shutdownJADE();
		} catch (ConnectException e) {
			Log.w(TAG_JADE, "Error disconnecting Jade");
			Log.e("jade.android", e.getMessage(), e);
		}

		gateway.disconnect(activity);
		Log.v(TAG_JADE, "Disconnection successfull");
	}

	@Override
	public void onDisconnected() {
		Log.v(TAG_JADE, "Disconnected to Jade");
	}
}
