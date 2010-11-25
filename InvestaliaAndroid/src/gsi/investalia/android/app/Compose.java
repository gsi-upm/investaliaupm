package gsi.investalia.android.app;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.android.jade.JadeAdapter;
import gsi.investalia.domain.Message;
import gsi.investalia.domain.Tag;
import gsi.investalia.domain.User;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Compose extends Activity implements OnClickListener {

	private static final String TAG_LOGGER = "Compose";

	private TextView selectedTopicsText;
	private boolean tagEmpty = true;
	private Button sendButton;
	private EditText title;
	private EditText text;
	private List<Tag> tags;
	private CharSequence[] tagsCharSequence;
	private boolean[] selectedTags;
	private int selectedTagsNum;
	private User loggedUser;
	private int dialogCount;
	
	// Jade
	private JadeAdapter jadeAdapter;

	// Broadcasting
	private ComposeBroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.compose);
		
		// Create the JadeAdapter
		jadeAdapter = ((Main) getParent()).getJadeAdapter();
		
		this.broadcastReceiver = new ComposeBroadcastReceiver();
		this.intentFilter = new IntentFilter();
		this.intentFilter.addAction(JadeAdapter.MESSAGE_OK);
		this.intentFilter.addAction(JadeAdapter.MESSAGE_FAIL);
		registerReceiver(this.broadcastReceiver, this.intentFilter);		

		// Buttons
		Button topicButton = (Button) findViewById(R.id.compose_topic_button);
		selectedTopicsText = (TextView) findViewById(R.id.compose_selected_topics_text);
		title = (EditText) findViewById(R.id.compose_title);
		text = (EditText) findViewById(R.id.compose_text);
		sendButton = (Button) findViewById(R.id.compose_send_button);
		sendButton.setEnabled(false);

		topicButton.setOnClickListener(this);
		sendButton.setOnClickListener(this);

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
		}
		
		// Check if there is enough information to activate the send button
		TextWatcher watcher = new TextWatcher() { 
			public void afterTextChanged(Editable s) {
				enableSendButton();
			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {}

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {}
		};
		title.addTextChangedListener(watcher);
		text.addTextChangedListener(watcher);
		
		// A number to pass in showDialog. This allows simply
		// erase the selected tags
		this.dialogCount = 1;	
	}

	public void onResume() {
		super.onResume();
		
		loggedUser = SQLiteInterface.getLoggedUser(this);
		enableSendButton();
		
		// Start listening
		registerReceiver(this.broadcastReceiver, this.intentFilter);
	}
	
	@Override
	public void onPause() {
		super.onPause();

		// Stop listening
		unregisterReceiver(this.broadcastReceiver);
	}
		
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.compose_topic_button:
			showDialog(dialogCount);
			break;

		case R.id.compose_send_button:
			sendData();
			break;
		}
	}
	
	/**
	 * Clean the information presented on the buttons 
	 */
	public void clearFields() {
		title.setText("");
		text.setText("");
		for (int i = 0; i < selectedTags.length; i++) {
			selectedTags[i] = false;
		}
		refreshSelectedTags();
		this.dialogCount++;
		enableSendButton();
	}

	/**
	 * Read the information related to the message to send 
	 */
	private void sendData() {

		String titleStr = title.getText().toString();
		String textStr = text.getText().toString();
		List<Tag> tags_selected = new ArrayList<Tag>();
		for (int i = 0; i < selectedTags.length; i++) {
			if (selectedTags[i]) {
				tags_selected.add(tags.get(i));
			}
		}
		
		// Only fill the necessary attributes
		// new Date() gets the current date
		Message message = new Message(-1, loggedUser.getUserName(), titleStr, textStr,
		tags_selected, new Date(), false, false, 0, 0, -1, 0);
		
		// Hide the keyboard
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(title.getWindowToken(), 0);
		
		// Send the message
		jadeAdapter.saveNewMessage(message);	
	}

	/**
	 * Create the dialog to select tags
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Log.i(TAG_LOGGER, "onCreateDialog");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.topic_title);
		builder.setMultiChoiceItems(tagsCharSequence, selectedTags,
				new DialogInterface.OnMultiChoiceClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which,
							boolean isChecked) {
						selectedTags[which] = isChecked;
						refreshSelectedTags();
						enableSendButton();
					}
				});

		AlertDialog alert = builder.create();
		return alert;
	}
	
	/**
	 * Update selected tags before sending a message
	 */
	private void refreshSelectedTags() {

		tagEmpty = true;
		selectedTagsNum = 0;
		String tags = "Tags: ";

		for (int i = 0; i < selectedTags.length; i++) {
			if (selectedTags[i] == true) {
				tagEmpty = false;
				tags = tags + "\n" + tagsCharSequence[i];
				selectedTagsNum++;
			}
		}
		selectedTopicsText.setText(tags);

		if (tagEmpty) {
			selectedTopicsText.setText(R.string.topic_none);
		}
	}

	/**
	 * Update the send button 
	 */
	private void enableSendButton() {
		if (tagEmpty || title.length() == 0 || text.length() == 0) {
			sendButton.setEnabled(false);
		} else {
			sendButton.setEnabled(true);
		}
	}
	
	/**
	 * Receiver to listen to updates
	 */
	private class ComposeBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(JadeAdapter.MESSAGE_OK)) {
				Toast.makeText(getBaseContext(), R.string.compose_sent,
						Toast.LENGTH_SHORT).show();
				clearFields();
			}
			else if (intent.getAction().equals(JadeAdapter.MESSAGE_FAIL)) {
				Toast.makeText(getBaseContext(), R.string.compose_error,
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}