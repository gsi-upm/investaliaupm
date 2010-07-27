package gsi.investalia.android.app;

import java.util.ArrayList;
import java.util.List;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.android.jade.JadeAdapter;
import gsi.investalia.domain.Message;
import gsi.investalia.domain.Tag;
import gsi.investalia.domain.User;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Compose extends Activity implements OnClickListener {

	private static final String TAG_LOGGER = "Compose";

	private TextView selected_topics_text;
	private boolean tag_empty = true;
	private Button send_button;
	private EditText title;
	private EditText text;
	private List<Tag> tags;
	private CharSequence[] tagsCharSequence;
	private boolean[] selectedTags;
	private int selected_tags;
	private User loggedUser;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose);

		loggedUser = SQLiteInterface.getLoggedUser(this);

		Button topic_button = (Button) findViewById(R.id.compose_topic_button);
		selected_topics_text = (TextView) findViewById(R.id.compose_selected_topics_text);
		title = (EditText) findViewById(R.id.compose_title);
		text = (EditText) findViewById(R.id.compose_text);
		send_button = (Button) findViewById(R.id.compose_send_button);
		send_button.setEnabled(false);

		topic_button.setOnClickListener(this);
		send_button.setOnClickListener(this);

		// List with all the tags
		tags = SQLiteInterface.getTags(this); 
		// List with all the tag names
		tagsCharSequence = new CharSequence[tags.size()];
		// List with the selected tags: 
		// selectedTags[i] = true means tags.get(i) is selected
		selectedTags = new boolean[tags.size()];
		// Fill the lists
		for(int i = 0; i < tags.size(); i++) {
			Tag tag = tags.get(i);
			tagsCharSequence[i] = tag.getTagName();
			selectedTags[i] = false;
			// No se activan las tags que sigue el autor para enviar un mensaje
//			for(Tag userTag: loggedUser.getTagsFollowing()) {
//				if(userTag.getId() == tag.getId()) {
//					selectedTags[i] = true;
//					break;
//				}
//			}
			
		}
		
		
		// Watcher para controlar cuando se introduce texto para activar el botón de enviar
		TextWatcher watcher = new TextWatcher() { 
			public void afterTextChanged(Editable s) {
				enableSendButton();
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}
		};
		title.addTextChangedListener(watcher);
		text.addTextChangedListener(watcher);
	}

	public void onResume() {
		super.onResume();
		enableSendButton();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.compose_topic_button:
			showDialog(0);
			break;

		case R.id.compose_send_button:
			sendData();
			break;
		}
	}

	/**
	 * Método que lee de los cuadros de texto y de la lista de tag seleccionadas
	 * para enviar los datos al Adapter
	 */
	private void sendData() {

		String titleStr = title.getText().toString();
		String textStr = text.getText().toString();
		List<Tag> tags = new ArrayList<Tag>();
		for (int i = 0; i < selectedTags.length; i++) {
			if (selectedTags[i]) {
				tags.add(tags.get(i));
			}
		}
		
		// Only fill the necessary attributes
		Message message = new Message(-1, "", titleStr, textStr,
		tags, null, false, false, 0, 0);

		if (JadeAdapter.saveNewMessage(loggedUser.getId(), message)) {
			Toast.makeText(getBaseContext(), R.string.compose_sent,
					Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getBaseContext(), R.string.compose_error,
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Crea el dialog para selccionar tags
	 */
	protected Dialog onCreateDialog(int id) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.topic_title);
		builder.setMultiChoiceItems(tagsCharSequence, selectedTags,
				new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						selectedTags[which] = isChecked;
						refreshSelectedTags();
					}
				});

		AlertDialog alert = builder.create();
		return alert;
	}

	/**
	 * Actualiza la lista de tags seleccionadas en la vista Actualiza el boton
	 * de enviar
	 */
	private void refreshSelectedTags() {

		tag_empty = true;
		selected_tags = 0;
		String tags = "Tags: ";

		for (int i = 0; i < selectedTags.length; i++) {
			if (selectedTags[i] == true) {
				tag_empty = false;
				tags = tags + "\n" + tagsCharSequence[i];
				selected_tags++;
			}
		}
		selected_topics_text.setText(tags);

		if (tag_empty) {
			selected_topics_text.setText(R.string.topic_none);
		}
		enableSendButton();
	}

	/*
	 * Actualiza el estado del botón enviar
	 */
	private void enableSendButton() {
		if (tag_empty || title.length() == 0 || text.length() == 0) {
			send_button.setEnabled(false);
		} else {
			send_button.setEnabled(true);
		}
	}
}
