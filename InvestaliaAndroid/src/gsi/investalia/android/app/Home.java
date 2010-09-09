package gsi.investalia.android.app;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.android.jade.JadeAdapter;
import gsi.investalia.domain.User;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class Home extends Activity {

	// Jade
	private JadeAdapter jadeAdapter;
	
	// Broadcasting
	private HomeBroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;
	
	// Log
	private static final String TAG_LOGGER = "HOME";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		// Get the user from the shared preferences
		User loggedUser = SQLiteInterface.getLoggedUser(this);

		// Set the views
		TextView username = (TextView) findViewById(R.id.home_TextView02);
		username.setText(getString(R.string.welcome) + " "
				+ loggedUser.getName());

		// Create the JadeAdapter
		jadeAdapter = new JadeAdapter(this.getParent());
		Main parent = (Main) getParent();
		parent.setJadeAdapter(jadeAdapter);
		
		// Set the broadcast receiver
		this.broadcastReceiver = new HomeBroadcastReceiver();
		this.intentFilter = new IntentFilter();
		this.intentFilter.addAction(JadeAdapter.JADE_CONNECTED);
		/* If the user has logged out, the activity could still be
		   connected to Main. If so, it will connect to jade when the
		   disconnection is successful */
		if(JadeAdapter.isConnected()) {
			this.intentFilter.addAction(JadeAdapter.JADE_DISCONNECTED);
		// Otherwise, connect directly
		} else {
			jadeAdapter.jadeConnect();
		}
		registerReceiver(this.broadcastReceiver, this.intentFilter);
	}
	
	@Override 
	public void onDestroy() {
		super.onDestroy();
		jadeAdapter.jadeDisconnect();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Creates the menu from the xml
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.home, menu);
		return true;
	}

	public void logOut() {
		SQLiteInterface.removeLoggedUser(this);
		startActivity(new Intent(this, Login.class));
		finish();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		super.onMenuItemSelected(featureId, item);
		// There is only one option: log out
		
		logOut();	
		return true;
	}

	/**
	 * Receiver to listen to updates
	 */
	private class HomeBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Disconnected to login: can connect to Main
			if (intent.getAction().equals(JadeAdapter.JADE_DISCONNECTED)) {
				Log.i(TAG_LOGGER, "Disconnected to login, connect to main");
				jadeAdapter.jadeConnect();
				Main parent = (Main) getParent();
				parent.setJadeAdapter(jadeAdapter);
			}
			// Connected to main: can download the messages
			if (intent.getAction().equals(JadeAdapter.JADE_CONNECTED)) {
				Log.i(TAG_LOGGER, "Download messages");
				unregisterReceiver(broadcastReceiver);
				jadeAdapter.donwloadNewMessages();
			}
		}
	}
}
