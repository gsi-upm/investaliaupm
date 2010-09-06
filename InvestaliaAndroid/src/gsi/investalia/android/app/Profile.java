package gsi.investalia.android.app;

import java.util.ArrayList;
import java.util.List;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.android.jade.JadeAdapter;
import gsi.investalia.domain.Tag;
import gsi.investalia.domain.User;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.util.Log;
import android.widget.Toast;

public class Profile extends PreferenceActivity implements
		OnPreferenceClickListener {

	private static final String TAG_LOGGER = "Profile";

	private User loggedUser;
	private List<Tag> tags;
	private CharSequence[] tagsCharsequence;
	private boolean[] selected;

	private EditTextPreference user_name;
	private EditTextPreference user_nick_name;
	private EditTextPreference user_email;
	private EditTextPreference user_location;
	private EditTextPreference user_password;

	// Jade
	private JadeAdapter jadeAdapter;

	// Broadcasting
	private ProfileBroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.profile);

		// Get the views from the xml
		user_name = (EditTextPreference) findPreference("user_name");
		user_nick_name = (EditTextPreference) findPreference("user_nick_name");
		user_email = (EditTextPreference) findPreference("user_email");
		user_location = (EditTextPreference) findPreference("user_location");
		user_password = (EditTextPreference) findPreference("user_password");

		Preference user_tags = (Preference) findPreference("user_tags");
		Preference user_save = (Preference) findPreference("user_save");
		Preference user_cancel = (Preference) findPreference("user_cancel");

		// Get the jadeAdapter from the main activity
		jadeAdapter = ((Main) getParent()).getJadeAdapter();

		// Set the preference CLICK listener for the special fields
		user_tags.setOnPreferenceClickListener(this);
		user_save.setOnPreferenceClickListener(this);
		user_cancel.setOnPreferenceClickListener(this);

		// Create the preference listener
		OnPreferenceChangeListener changeListener = new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				preference.setSummary((String) newValue);
				return true;
			}
		};

		// Set the listener to the views
		user_name.setOnPreferenceChangeListener(changeListener);
		user_nick_name.setOnPreferenceChangeListener(changeListener);
		user_email.setOnPreferenceChangeListener(changeListener);
		user_location.setOnPreferenceChangeListener(changeListener);

		// Set the broadcast receiver
		this.broadcastReceiver = new ProfileBroadcastReceiver();
		this.intentFilter = new IntentFilter();
		this.intentFilter.addAction(JadeAdapter.USER_UPDATED);
		this.intentFilter.addAction(JadeAdapter.USER_NOT_UPDATED);
	}

	@Override
	public void onResume() {
		super.onResume();

		// Gets the data each time the tab is resumed
		loadData();
		// Refreshes the views
		refreshData();
		// Start listening
		registerReceiver(this.broadcastReceiver, this.intentFilter);
	}

	@Override
	public void onPause() {
		super.onPause();

		// Stop listening
		unregisterReceiver(this.broadcastReceiver);
	}

	/**
	 * Creates the dialog to select the tags
	 */
	protected Dialog onCreateDialog(int id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.topic_title);
		builder.setMultiChoiceItems(tagsCharsequence, selected,
				new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						selected[which] = isChecked;
					}
				});

		AlertDialog alert = builder.create();
		return alert;
	}

	/**
	 * Gets the data saved
	 */
	private void loadData() {	
		Log.i(TAG_LOGGER, "Loading data"); 
		
		// Get the logged user
		loggedUser = SQLiteInterface.getLoggedUser(this);
		
		// Get the tags
		tags = SQLiteInterface.getTags(this);
		tagsCharsequence = new CharSequence[tags.size()];
		for (int i = 0; i < tags.size(); i++) {
			tagsCharsequence[i] = tags.get(i).getTagName();
		}
		
		// Creates a boolean array to indicate which ones are selected
		selected = new boolean[tagsCharsequence.length];

		// Check the following tags
		if (!tags.isEmpty()) { 
			for (Tag tag : loggedUser.getTagsFollowing()) {
				selected[tag.getId() - 1] = true;
			}
		}
	}

	private void refreshData() {
		// Refresh the options
		user_name.setText(loggedUser.getName());
		user_nick_name.setText(loggedUser.getUserName());
		user_email.setText(loggedUser.getEmail());
		user_location.setText(loggedUser.getLocation());
		user_password.setText(loggedUser.getPassword());

		// Refresh the 'summary' (text below each option)
		user_name.setSummary(user_name.getText());
		user_nick_name.setSummary(user_nick_name.getText());
		user_email.setSummary(user_email.getText());
		user_location.setSummary(user_location.getText());
	}

	private void saveData() {
		// Create a new user object with the modified attributes
		List<Tag> selectedTags = new ArrayList<Tag>();
		for (int i = 0; i < selected.length; i++) {
			if (selected[i]) { // TODO is it okay?
				selectedTags.add(new Tag(i + 1, ""));
			}
		}
		User modifiedUser = new User(loggedUser.getId(), user_nick_name
				.getText(), user_password.getText(), user_name.getText(),
				user_location.getText(), user_email.getText(), selectedTags, 0);

		// Saves the user profile
		jadeAdapter.updateUser(modifiedUser);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();

		if (key.equals("user_tags")) {
			showDialog(0);
			return true;

		} else if (key.equals("user_save")) {
			saveData();
			return true;

		} else if (key.equals("user_cancel")) {
			removeDialog(0);
			loadData();
			refreshData();
			Toast.makeText(getBaseContext(), R.string.profile_save_cancel,
					Toast.LENGTH_SHORT).show();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Receiver to listen to updates
	 */
	private class ProfileBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(JadeAdapter.USER_UPDATED)) {
				Log.i(TAG_LOGGER, "User updated correctly");
				loadData();
				refreshData();
				Toast.makeText(getBaseContext(), R.string.profile_save_ok,
						Toast.LENGTH_SHORT).show();
			} else if (intent.getAction().equals(JadeAdapter.USER_NOT_UPDATED)) {
				Log.i(TAG_LOGGER, "User not updated");
				Toast.makeText(getBaseContext(), R.string.user_error,
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}
