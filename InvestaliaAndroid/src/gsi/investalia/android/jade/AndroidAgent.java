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

		if (command instanceof Context && listener == null) {
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

		} else if (command instanceof User) {
			final User user = (User) command;

			// Launches the agent
			Log.v(TAG_LOGGER, "Sending login user");

			SequentialBehaviour sb = new SequentialBehaviour(this);
			sb.addSubBehaviour(new OneShotBehaviour(this) {
				public void action() {
					ACLMessage msg = new ACLMessage(ACLMessage.CFP);
					try {
						msg.setContent(JSONAdapter.userToJSON(user).toString());
					} catch (JSONException e) {
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

			// if a message is available and a listener is available
			if (msg != null && listener != null) {
				// Calls interface updater
				listener.onMessageReceived(msg);
			} else {
				block();
			}
		}
	}
}
