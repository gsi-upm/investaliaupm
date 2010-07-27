package gsi.investalia.android.jade;

import org.json.JSONException;

import gsi.investalia.domain.User;
import gsi.investalia.json.JSONAdapter;
import android.content.Context;
import android.util.Log;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.SequentialBehaviour;
import jade.lang.acl.ACLMessage;
import jade.wrapper.gateway.GatewayAgent;

public class AndroidAgent extends GatewayAgent {

	private JadeListener listener;
	private static final String TAG_LOGGER = "AGENT";

	@Override
	protected void setup() {
		super.setup();
		addBehaviour(new MessageReceiverBehaviour());
	}

	@Override
	protected void processCommand(final Object command) {
		final Object args[] = getArguments();
		if (command instanceof Context && listener == null) {
			Log.v("LISTENER","Listener recibidoNOnull");
			listener = new JadeListener((Context) command);
		}

		if (command instanceof Behaviour) {
			Log.v(TAG_LOGGER, "Adding a new behaviour");
			SequentialBehaviour sb = new SequentialBehaviour(this);
			sb.addSubBehaviour((Behaviour) command);
			sb.addSubBehaviour(new OneShotBehaviour(this) {
				public void action() {
					AndroidAgent.this.releaseCommand(command);
				}
			});
			addBehaviour(sb);

		} else if (command instanceof ACLMessageListener) {
			// Launches the agent
			Log.v(TAG_LOGGER, "Sending login user");
			SequentialBehaviour sb = new SequentialBehaviour(this);
			sb.addSubBehaviour(new OneShotBehaviour(this) {
				public void action() {
					ACLMessage msg = new ACLMessage(ACLMessage.CFP);
					try {
						String content = args[0].toString();
						msg.setContent(content);


					} catch (Exception e) {
						Log.e(TAG_LOGGER, "Error parsing JSON");
					}

					AID login = new AID("login", AID.ISLOCALNAME);
					msg.addReceiver(login);
					send(msg);
					Log.v(TAG_LOGGER, "Login message sent");
				}
			});
			sb.addSubBehaviour(new OneShotBehaviour(this) {
				public void action() {
					Log.v(TAG_LOGGER, "Releasing command from behaviour");
					AndroidAgent.this.releaseCommand(command);
				}
			});
			addBehaviour(sb);
			releaseCommand(command);
			Log.v(TAG_LOGGER, "Releasing command outside behaviour");
		} else {
			Log.v(TAG_LOGGER, "Null comand");
			releaseCommand(command);
		}
	}	

	private class MessageReceiverBehaviour extends CyclicBehaviour {

		public void action() {
			ACLMessage msg = myAgent.receive();
			Log.v("LISTENER","Listener recibido1");
			// if a message is available and a listener is available
			if(listener==null){
				Log.v("LISTENER","Listener recibidonull");
				listener = new JadeListener(null);
			}
			if (msg != null && listener != null) {
				// Calls interface updater
				Log.v("LISTENER","Listener recibidorweteg");
				listener.onMessageReceived(msg);
			} else {
				block();
			}
		}
	}
}
