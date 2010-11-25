package gsi.investalia.android.jade;

import android.util.Log;

import jade.core.AID;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class RestartBehaviour extends OneShotBehaviour {

	@Override
	public void action() {
		ACLMessage msg = new ACLMessage(ACLMessage.PROPOSE);
		msg.setContent("DEAD");
		AID login = new AID("login", AID.ISLOCALNAME);
		msg.addReceiver(login); 
		myAgent.send(msg);
		Log.v("ANDROID", "Worflow restart message sent");
	}

}
