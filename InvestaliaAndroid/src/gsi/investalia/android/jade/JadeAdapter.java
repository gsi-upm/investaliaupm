package gsi.investalia.android.jade;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.domain.Message;
import gsi.investalia.domain.Tag;
import gsi.investalia.domain.User;
import gsi.investalia.json.JSONAdapter;
import jade.android.ConnectionListener;
import jade.android.JadeGateway;
import jade.core.Profile;
import jade.imtp.leap.JICP.JICPProtocol;
import jade.util.leap.Properties;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

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

	private JadeListener updater;
	// Broadcast actions
	public static final String LOGGED_IN = "logged_in";
	public static final String WRONG_LOGIN = "wrong_login";
	public static final String MESSAGE_OK = "message_ok";
	public static final String MESSAGE_FAIL = "message_fail";

	public JadeAdapter() {		
	}

	public void checkLogin(String username, String password,Context cnt,Activity act) {
		
		//SQLiteInterface.saveExampleMessages(context); //Solo ejecutar una vez en el terminal para introducir datos de prueba
		
		try {
			User user = new User(username, password);
			Log.v("ANDROID", "user creado"+ user.getUserName());
			String [] args = {"checkLogin", JSONAdapter.userToJSON(user).toString()};
			jadeConnect(args,  act, cnt);			

		}  catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Recibe los datos enviados desde la pantalla de escribir mensajes
	 * 
	 * @param user
	 *            el usuario logeado que está enviando el mensaje
	 * @param message
	 *            mensaje
	 * @return true si el mensaje se ha guardado correctamente, false en caso de
	 *         error
	 */
	public void saveNewMessage(User user, Message message,Context cnt,Activity act) {
		
		Log.v(TAG_LOGGER, "User name: " + user.getName() + " Titulo: "
						+ message.getTitle() + " Texto: " + message.getText()+" Fecha: "+message.getDate().toLocaleString()
						+ " Topics primer ID: ");
						//+ message.getTags().get(0).getTagName());
		
		try {
			Log.v(TAG_LOGGER, "enviando mensaje de "+ user.getName());
			//Pasamos como parametros el método donde estamos, el usuario logeado y el mensaje.
			String [] args = {"saveNewMessage",JSONAdapter.userToJSON(user).toString(), 
					JSONAdapter.messageToJSON(message).toString()};
			jadeConnect(args,  act, cnt);
			


		}  catch (Exception e) {
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

	
	
	public void jadeConnect(String[] args, Activity act, Context context) {
		Log.v("LOGIN", "Starting Jade connection");

		// Save the context
		this.context = context;
		// CREATE THE UI UPDATER
		updater = new JadeListener(context);

		// Create JADE properties class
		Properties props = new Properties();
		String user = "error";
		try {
			//Para obtener el usuario que envia el mensaje
			JSONObject json = new JSONObject(args[1]);
			user = json.getString(JSONAdapter.USER_NAME);
			Log.v(TAG_JADE,"Nombre del agente en jadeConnect: "+user);

		} catch (JSONException e1) {
			Log.v("JSON ERROR",e1.getMessage());
		}
		//TODO: IMPORTANTE: añadir el host y puerto en el strings.xml
		props.setProperty(Profile.MAIN_HOST, IP);
		props.setProperty(Profile.MAIN_PORT, PORT);
		props.setProperty(JICPProtocol.MSISDN_KEY,user);

		try {
			JadeGateway.connect(AndroidAgent.class.getName(), args, props,
					context,this);


		}catch (Exception e) {
			Log.w("LOGIN", "Error connecting");
			Log.e("jade.android", e.getMessage(), e);
		}

	}



	// // JADE
//	public void jadeConnect(String containerId, Context context) {
//		Log.v(TAG_JADE, "Starting Jade connection");
//
//		// Save the context
//		this.context = context;
//
//		// Create JADE properties class
//		Properties props = new Properties();
//
//		// TODO: IMPORTANTE: añadir el host y puerto en el strings.xml
//		props.setProperty(Profile.MAIN_HOST, IP);
//		props.setProperty(Profile.MAIN_PORT, PORT);
//		props.setProperty(JICPProtocol.MSISDN_KEY, containerId);
//
//		try {
//			// agentClassName, agentArgs, jadeProfile, context, listener 
//			JadeGateway.connect(AndroidAgent.class.getName(), null, props, context, this);
//			Log.v(TAG_JADE, "Connection sucessfull");
//		} catch (Exception e) {
//			Log.w(TAG_JADE, "Error connecting");
//			Log.e("jade.android", e.getMessage(), e);
//		}
//	}

	@Override
	public void onConnected(JadeGateway gw) {
		Log.v(TAG_JADE, "Calling onConnected method");
		gateway = gw;

		try {
			// First, we creat0e the MessageListener
			if (context == null) Log.v("CONTEXT", "null");
			gateway.execute(context);
			gateway.execute(updater); 
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

//		// First we restart secretary agent workflow with a new android
//		// behaviour	
//		try {
//			gateway.execute(new RestartBehaviour());
//			Log.v(TAG_JADE, "Workflow restarted");
//		} catch (Exception e) {
//			Log.e(TAG_JADE, "Error restarting secretary workflow");
//			// TODO We should finish the workflow and start a new one
//		}

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


