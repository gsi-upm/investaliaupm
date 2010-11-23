package gsi.investalia.android.app;

import gsi.investalia.android.db.MessagesDBHelper;
import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.android.jade.JadeAdapter;
import gsi.investalia.domain.Message;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MessageList extends Activity implements OnItemClickListener {

	// App
	private static final String TAG_LOGGER = "Messages activity";
	private static final String DATE_FORMAT_SHOW = "dd/MM/yyyy";
	private String orderingBy;
	private String whichMessages;
	private ArrayAdapter<Message> arrayAdapter;
	private int selectedIndex;
	public static final int IDREFRESH = -2; // Id of the unreal message to ask
											// for the previous

	// Jade
	private JadeAdapter jadeAdapter;

	// Domain
	private List<Message> messages;

	// Broadcasting
	private MessageListBroadcastReceiver broadcastReceiver;
	private IntentFilter intentFilter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_list);

		// Set orderingBy by default
		orderingBy = MessagesDBHelper.DATE;
		whichMessages = SQLiteInterface.FOLLOWING;

		// List of messages
		messages = new ArrayList<Message>();
		selectedIndex = -1;

		// ListView
		ListView listView = (ListView) findViewById(R.id.message_list);
		listView.setOnItemClickListener(this);

		// Inflater
		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// Adapter
		arrayAdapter = new ArrayAdapter<Message>(this, R.layout.message_item,
				messages) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View itemView;

				// Get the message
				Message m = getItem(position);

				// Get the views
				if (m.getId() == IDREFRESH) {
					itemView = inflater.inflate(R.layout.refresh_item, null);
				} else {
					itemView = inflater.inflate(R.layout.message_item, null);
				}

				if (m.getId() != IDREFRESH) {
					TextView userView = (TextView) itemView
							.findViewById(R.id.user);
					TextView colorView = (TextView) itemView
							.findViewById(R.id.colorView);
					TextView titleView = (TextView) itemView
							.findViewById(R.id.title);
					TextView dateView = (TextView) itemView
							.findViewById(R.id.date);
					TextView tagsView = (TextView) itemView
							.findViewById(R.id.tags);
					ImageView imageView = (ImageView) itemView
							.findViewById(R.id.user_image);

					// Inflate the views
					if (m.getAffinity() > 0) {
						colorView.setBackgroundResource(R.color.blue);
					} else if (!m.isRead()) {
						colorView.setBackgroundResource(R.color.green);
					} else if (m.isLiked()) {
						colorView.setBackgroundResource(R.color.yellow);
					} else {
						// If read and not liked, remove the background
						colorView.setBackgroundResource(0);
					}

					userView.setText(m.getUserName());
					titleView.setText(m.getTitle());
					String dateStr = new SimpleDateFormat(DATE_FORMAT_SHOW)
							.format(m.getDate());
					dateView.setText(dateStr);

					// Tags
					String tagsStr = "";
					for (int i = 0; i < m.getTags().size(); i++) {
						tagsStr += m.getTags().get(i).getTagName();
						if (i < m.getTags().size() - 1) {
							tagsStr += ", ";
						}
					}
					tagsStr.toUpperCase();
					tagsView.setText(tagsStr);
				}

				// Return the view
				return itemView;
			}
		};
		listView.setAdapter(arrayAdapter);

		// Create the JadeAdapter
		jadeAdapter = ((Main) getParent()).getJadeAdapter();

		// Set the broadcast receiver
		this.broadcastReceiver = new MessageListBroadcastReceiver();
		this.intentFilter = new IntentFilter();
		this.intentFilter.addAction(JadeAdapter.MESSAGES_DOWNLOADED);
	}

	@Override
	public void onResume() {
		super.onResume();

		// Add messages from database
		SQLiteInterface.addMessages(this, messages, whichMessages, orderingBy);
		arrayAdapter.notifyDataSetChanged();

		// Update the last message read (if any)
		if (selectedIndex != -1) {
			jadeAdapter.updateMessage(messages.get(selectedIndex));
			selectedIndex = -1;
		}

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
	 * Called when an item is clicked
	 */
	@Override
	public void onItemClick(AdapterView av, View v, int index, long arg3) {
		Message selectedMessage = messages.get(index);
		if(selectedMessage.getId() == IDREFRESH) {
			jadeAdapter.donwloadOldMessages();
		}
		else {
			// Send the message id
			Bundle bundle = new Bundle();
			selectedIndex = index;
			bundle.putInt("message_id", selectedMessage.getId());
			Intent intent = new Intent(this, ReadMessage.class);
			intent.putExtras(bundle);
			startActivity(intent);
		}
	}

	/**
	 * Creates the menu from the xml
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	/**
	 * Called when a menu item is selected
	 */
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		super.onMenuItemSelected(featureId, item);

		if (item.getItemId() == R.id.menu_refresh) {
			Log.i(TAG_LOGGER, "Message list: ask for new messages");
			jadeAdapter.donwloadNewMessages();
		} else if (item.getItemId() == R.id.menu_show) {
			// TODO
		} else {
			item.setChecked(true);
			String toastStr = "";
			if (item.getItemId() == R.id.show_opt2_following_affinity) {
				toastStr = getString(R.string.show_opt2_following_affinity);
				whichMessages = SQLiteInterface.FOLLOWING;
				orderingBy = MessagesDBHelper.AFFINITY;
			} else if (item.getItemId() == R.id.show_opt1_following_date) {
				toastStr = getString(R.string.show_opt1_following_date);
				whichMessages = SQLiteInterface.FOLLOWING;
				orderingBy = MessagesDBHelper.DATE;
			} else if (item.getItemId() == R.id.show_opt4_all_affinity) {
				toastStr = getString(R.string.show_opt4_all_affinity);
				whichMessages = SQLiteInterface.ALL;
				orderingBy = MessagesDBHelper.AFFINITY;
			} else if (item.getItemId() == R.id.show_opt3_all_date) {
				toastStr = getString(R.string.show_opt3_all_date);
				whichMessages = SQLiteInterface.ALL;
				orderingBy = MessagesDBHelper.DATE;
			}
			Toast.makeText(getBaseContext(), toastStr, Toast.LENGTH_SHORT)
					.show();
			SQLiteInterface.addMessages(this, messages, whichMessages,
					orderingBy);
			arrayAdapter.notifyDataSetChanged();
		}
		return true;
	}

	/**
	 * Receiver to listen to updates
	 */
	private class MessageListBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(JadeAdapter.MESSAGES_DOWNLOADED)) {
				Log.i(TAG_LOGGER, "Messages downloaded broadcast receipt: "
						+ this.toString());
				// Add messages from database
				SQLiteInterface.addMessages(MessageList.this, messages,
						whichMessages, orderingBy);
				arrayAdapter.notifyDataSetChanged();
				// Notify as a toast
				Toast.makeText(getBaseContext(), R.string.refresh_complete,
						Toast.LENGTH_SHORT).show();
			}
		}
	}
}