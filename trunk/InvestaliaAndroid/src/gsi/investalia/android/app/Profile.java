package gsi.investalia.android.app;

import java.util.ArrayList;
import java.util.List;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.android.jade.JadeAdapter;
import gsi.investalia.domain.Tag;
import gsi.investalia.domain.User;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;

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

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.profile);

		loggedUser = SQLiteInterface.getLoggedUser(this);
		tags = SQLiteInterface.getTags(this);
		tagsCharsequence = new CharSequence[tags.size()];
		for(int i = 0; i < tags.size(); i++) {
			tagsCharsequence[i] = tags.get(i).getTagName();
		}
		
		// Obtener del xml de preferences los objetos de la vista
		user_name = (EditTextPreference) findPreference("user_name");
		user_nick_name = (EditTextPreference) findPreference("user_nick_name");
		user_email = (EditTextPreference) findPreference("user_email");
		user_location = (EditTextPreference) findPreference("user_location");
		user_password = (EditTextPreference) findPreference("user_password");

		Preference user_tags = (Preference) findPreference("user_tags");
		Preference user_save = (Preference) findPreference("user_save");
		Preference user_cancel = (Preference) findPreference("user_cancel");

		loadData(); // Obtiene los datos
		refreshData(); // Actualiza la vista con los datos

		// Añadir un listener onClick para las opciones especiales
		user_tags.setOnPreferenceClickListener(this);
		user_save.setOnPreferenceClickListener(this);
		user_cancel.setOnPreferenceClickListener(this);

		OnPreferenceChangeListener changeListener = new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				preference.setSummary((String) newValue);
				return true;
			}
		};

		user_name.setOnPreferenceChangeListener(changeListener);
		user_nick_name.setOnPreferenceChangeListener(changeListener);
		user_email.setOnPreferenceChangeListener(changeListener);
		user_location.setOnPreferenceChangeListener(changeListener);

	}

	/**
	 * Crea el dialog para selccionar tags
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
	 * Obtiene los datos guardados para este usuario
	 */
	private void loadData() {

		selected = new boolean[tagsCharsequence.length]; // Crea un array de booleans
												// (false) que indicará que tag
												// está seleccionada

		for (Tag tag: loggedUser.getTagsFollowing()) {
			selected[tag.getId()] = true; // Marca las tags suscritas como seleccionadas
		}

		user_name.setText(loggedUser.getName());
		user_nick_name.setText(loggedUser.getUserName());
		user_email.setText(loggedUser.getEmail());
		user_location.setText(loggedUser.getLocation());
		user_password.setText(loggedUser.getPassword());
	}

	// Actualizar los 'summary' (texto debajo del título de cada opción)
	private void refreshData() {
		user_name.setSummary(user_name.getText());
		user_nick_name.setSummary(user_nick_name.getText());
		user_email.setSummary(user_email.getText());
		user_location.setSummary(user_location.getText());
	}

	private boolean saveData() {

		// Create a new user object with the modified attributes
		List<Tag> selectedTags = new ArrayList<Tag>();
		for (int i = 0; i < selected.length; i++) {
			if (selected[i] == true) { // TODO is it okay?
				selectedTags.add(new Tag(i, ""));
			}
		}
		User modifiedUser = new User(loggedUser.getId(), user_nick_name.getText(), 
				user_password.getText(), user_name.getText(),
				user_location.getText(), user_email.getText(), null, 0);
		
		// Saves the user profile
		return JadeAdapter.setUserProfile(modifiedUser);
	}

	@Override
	public boolean onPreferenceClick(Preference preference) {
		String key = preference.getKey();

		if (key.equals("user_tags")) {
			showDialog(0);
			return true;

		} else if (key.equals("user_save")) {
			if (saveData()) {
				Toast.makeText(getBaseContext(), R.string.profile_save_ok,
						Toast.LENGTH_SHORT).show();
			}
			loadData();
			refreshData();
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
}
