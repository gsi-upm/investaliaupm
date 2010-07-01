package gsi.investalia.android.app;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.Toast;
import gsi.investalia.android.app.R;
import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.android.jade.JadeAdapter;
import gsi.investalia.domain.Tag;
import gsi.investalia.domain.User;

public class Login extends Activity implements OnClickListener {

	// Wiews
	private EditText user_text;
	private EditText user_pass;
	private View stateLayout;
	
	// Jade
	private JadeAdapter jadeAdapter;
	
	// Broadcasting
	private LoginBroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);

		// If it's already logged, we send it directly to main
		//if (SQLiteInterface.getLoggedUser(this) != null) {
			//finish();
			//TODO goToMain();
		//}

		// Get the views
		user_text = (EditText) findViewById(R.id.user_text);
		user_pass = (EditText) findViewById(R.id.user_pass);
		stateLayout = findViewById(R.id.state_layout);

		// Set the listeners
		findViewById(R.id.login_button).setOnClickListener(this);
		findViewById(R.id.new_user_button).setOnClickListener(this);

		// Connect to JADE
		jadeAdapter = new JadeAdapter();
		jadeAdapter.jadeConnect("luis", this);
		
		// Set the broadcast receiver
		this.broadcastReceiver = new LoginBroadcastReceiver();
		this.intentFilter = new IntentFilter();
		this.intentFilter.addAction(JadeAdapter.LOGGED_IN);
		this.intentFilter.addAction(JadeAdapter.WRONG_LOGIN);
	}

	public void onResume() {
		super.onResume();

		// Start listening
		registerReceiver(this.broadcastReceiver, this.intentFilter);
	}

	public void onPause() {
		super.onPause();
		// Cuando la actividad se queda en segundo plano se oculta el mensaje de
		// "cargando..."
		stateLayout.setVisibility(View.INVISIBLE);

		// Stop listening
		registerReceiver(this.broadcastReceiver, this.intentFilter);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.v("LOGIN", "Calling onDestroy method");
		//jadeAdapter.jadeDisconnect(this);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.new_user_button:
			showDialog(0);
			break;

		case R.id.login_button:
			// Comprobamos que los cuadros de texto no están vacios
			if (user_text.length() == 0 || user_pass.length() == 0) {
				Toast.makeText(getBaseContext(), R.string.empty_user_pass,
						Toast.LENGTH_SHORT).show();

			} else {
				String username = user_text.getText().toString();
				String password = user_pass.getText().toString();
				//TODO JadeAdapter.checkLogin(username, password, this);
				JadeAdapter.checkLogin(username, password, this);
			}
		}
	}

	private void wrongLogin() {
		Animation shake = AnimationUtils.loadAnimation(this, R.anim.shake);
		user_pass.startAnimation(shake);
		Toast.makeText(getBaseContext(), R.string.wrong_pass,
				Toast.LENGTH_SHORT).show();
	}

	private void goToMain() {
		// Start the main activity
		startActivity(new Intent(this, Main.class));
		// Close the login activity
		finish();
	}

	protected Dialog onCreateDialog(int id) {

		LayoutInflater factory = LayoutInflater.from(this);
		final View textEntryView = factory.inflate(R.layout.new_user, null);

		AlertDialog.Builder dialog = new AlertDialog.Builder(this);
		dialog.setView(textEntryView);
		dialog.setTitle(R.string.new_user_button);
		dialog.setPositiveButton(R.string.dialog_ok,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {

						EditText userName = (EditText) textEntryView
								.findViewById(R.id.new_user_nick_name);
						EditText password = (EditText) textEntryView
								.findViewById(R.id.new_user_password);
						EditText name = (EditText) textEntryView
								.findViewById(R.id.new_user_name);
						EditText email = (EditText) textEntryView
								.findViewById(R.id.new_user_email);
						EditText location = (EditText) textEntryView
								.findViewById(R.id.new_user_location);

						if (userName.length() == 0 || password.length() == 0
								|| name.length() == 0 || email.length() == 0
								|| location.length() == 0) {
							// Si alguno de los campos se encuentran en blanco
							Toast.makeText(getBaseContext(),
									R.string.empty_field, Toast.LENGTH_SHORT)
									.show();

						} else {
							User user = new User(-1, userName.toString(),
									password.toString(), name.toString(),
									location.toString(), email.toString(),
									new ArrayList<Tag>(), 0);
							// Si da error el método del adapter devolverá un
							// false

							if (JadeAdapter.newUser(user)) {
								Toast.makeText(getBaseContext(),
										R.string.user_created,
										Toast.LENGTH_SHORT).show();
							} else {
								Toast
										.makeText(getBaseContext(),
												R.string.user_error,
												Toast.LENGTH_SHORT).show();
							}
						}

					}
				});

		dialog.setNegativeButton(R.string.dialog_cancel,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						removeDialog(0);
					}
				});

		AlertDialog new_user_dialog = dialog.create();

		return new_user_dialog;

	}

	/**
	 * Receiver to listen to updates
	 */
	private class LoginBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(JadeAdapter.LOGGED_IN)) {
				goToMain();
			}
			else if (intent.getAction().equals(JadeAdapter.WRONG_LOGIN)) {
				wrongLogin();
			}
		}
	}
}