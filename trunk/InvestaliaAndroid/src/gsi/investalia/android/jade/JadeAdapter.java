package gsi.investalia.android.jade;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.domain.Message;
import gsi.investalia.domain.Tag;
import gsi.investalia.domain.User;
import jade.android.ConnectionListener;
import jade.android.JadeGateway;
import jade.core.Profile;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.util.leap.Properties;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
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
	private JadeGateway gateway;
		
	// App
	private static final String IP = "10.0.2.2";
	private static final String PORT = "1099";
	private Context context;
	private static final String TAG_LOGGER = "Adapter";
	private static final String TAG_JADE = "JADE";

	
	// Broadcast actions
	public static final String LOGGED_IN = "logged_in";
	public static final String WRONG_LOGIN = "wrong_login";

	public JadeAdapter() {		
	}

	public void checkLogin(String username, String password, Context context) {

		try {
			User user = new User(username, password);
			gateway.execute(user);
						
		}  catch (Exception e) {
			e.printStackTrace();
		}

				
		// TEST		
		// TODO this should be done by the JADE Receiver
		
		List<Tag> tags = new ArrayList<Tag>();
		tags.add(new Tag(1, "Finanzas"));
		tags.add(new Tag(2, "Banca"));
		User user2 = new User(1, "user", "pw", "John Locke", "The Island",
				"john@lost.com", tags, 0);
		SQLiteInterface.saveLoggedUser(user2, context);
		//Solo ejecutar una vez en el terminal
		//SQLiteInterface.saveExampleMessages(context);
		context.sendBroadcast(new Intent(JadeAdapter.LOGGED_IN));
		//	END TEST	
		
	}

	/**
	 * Recibe los datos enviados desde la pantalla de escribir mensajes
	 * 
	 * @param userId
	 *            Id del usuario que ha enviado el mensaje
	 * @param message
	 *            mensaje
	 * @return true si el mensaje se ha guardado correctamente, false en caso de
	 *         error
	 */
	public static boolean saveNewMessage(int userId, Message message) {
		// TODO Auto-generated method stub
		Log.v(TAG_LOGGER, "User_id: " + userId + " Titulo: "
						+ message.getTitle() + " Texto: " + message.getText()
						+ " Topics primer ID: "
						+ message.getTags().get(0).getTagName());

		return true;
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

	/**
	 * Guarda los nuevos valores del perfil
	 * 
	 * @param user
	 * @return false en caso de error, true si los datos se han guardado
	 *         correctamente
	 */
	public static boolean setUserProfile(User user) {

		// TODO
		Log.v(TAG_LOGGER, user.getName() + " " + user.getUserName() + " "
				+ user.getEmail() + " " + user.getLocation() + " "
				+ user.getPassword());

		return true;
	}

	/**
	 * Método para registrar un nuevo usuario
	 * 
	 * @param user
	 *            nuevo usuario
	 * @return true si el usuario se crea correctamente, false si se produce
	 *         algun error
	 */
	public static boolean newUser(User user) {
		// TODO Auto-generated method stub
		return true;
	}
	
	
	

	// // JADE
	public void jadeConnect(String containerId, Context context) {
		Log.v(TAG_JADE, "Starting Jade connection");

		// Save the context
		this.context = context;
		
		// Create JADE properties class
		Properties props = new Properties();

		// TODO: IMPORTANTE: añadir el host y puerto en el strings.xml
		props.setProperty(Profile.MAIN_HOST, IP);
		props.setProperty(Profile.MAIN_PORT, PORT);
		props.setProperty(JICPProtocol.MSISDN_KEY, containerId);

		try {
			// agentClassName, agentArgs, jadeProfile, context, listener 
			JadeGateway.connect(AndroidAgent.class.getName(), null, props, context, this);
			Log.v(TAG_JADE, "Connection sucessfull");
		} catch (Exception e) {
			Log.w(TAG_JADE, "Error connecting");
			Log.e("jade.android", e.getMessage(), e);
		}
	}

	@Override
	public void onConnected(JadeGateway gw) {
		Log.v(TAG_JADE, "Calling onConnected method");
		gateway = gw;
		
		try {
			// First, we create the MessageListener
		
			gateway.execute(context); 
			Log.v(TAG_JADE, "Agent created succesfully");
			
			} catch (Exception e) {
			Log.w(TAG_JADE, "Error creating agent");
			Log.e("jade.android", e.getMessage(), e);
		}
	}
	
	

	public void jadeDisconnect(Activity activity) {
		Log.v(TAG_JADE, "Starting Jade disconnection");

		if (gateway == null) {
			Log.v(TAG_JADE, "The connection wasn't established.");
			return;
		}

		// First we restart secretary agent workflow with a new android
		// behaviour	
		try {
			gateway.execute(new RestartBehaviour());
			Log.v(TAG_JADE, "Workflow restarted");
		} catch (Exception e) {
			Log.e(TAG_JADE, "Error restarting secretary workflow");
			// TODO We should finish the workflow and start a new one
		}

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

	// /END OF JADE
	
}



