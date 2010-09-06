package gsi.investalia.android.jade;

import android.util.Log;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
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

		Log.i(TAG_LOGGER, "zzz received: " + command.toString());
		// Listener received
		if (command instanceof JadeListener) {
			Log.v("LISTENER", "zzz Listener received");
			listener = (JadeListener) command;
		}

		// Behaviour received
		else if (command instanceof Behaviour) {
			Log.v(TAG_LOGGER, "zzz Adding a new behaviour");
			addBehaviour((Behaviour) command);
		}

		releaseCommand(command);
	}

	private class MessageReceiverBehaviour extends CyclicBehaviour {

		public void action() {
			ACLMessage msg = myAgent.receive();
			Log.v("LISTENER", "MessageReceiverBehaviour added");

			// if a message is available and a listener is available
			if (msg != null && listener != null) {
				// Calls interface updater
				Log.v("LISTENER", "Pass message to the listener");
				listener.onMessageReceived(msg);
			} else {
				block();
			}
		}
	}
}
