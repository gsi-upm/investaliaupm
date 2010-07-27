package gsi.investalia.android.app;

import gsi.investalia.android.db.MessagesDBHelper;
import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.domain.Message;
import gsi.investalia.domain.User;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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

	private static final String TAG_LOGGER = "Messages activity";
	private List<Message> messages;
	private User loggedUser;
	private ArrayAdapter<Message> arrayAdapter;
	private static final String DATE_FORMAT_SHOW = "dd/MM/yyyy";
	private int orderingBy;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_list);
		
		// Logged user
		loggedUser = SQLiteInterface.getLoggedUser(this);
		
		// Set orderingBy by default
		orderingBy = R.id.show_opt1_date;

		// List of messages
		messages = new ArrayList<Message>();
		SQLiteInterface.addMessages(this, messages, null);
		
		// ListView
		ListView listView = (ListView) findViewById(R.id.message_list);
		listView.setOnItemClickListener(this);

		// Inflater
		final LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		// Adapter
		arrayAdapter = new ArrayAdapter<Message>(this, R.layout.message_item, messages) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View itemView;

				// Get the views
				if (convertView == null) {
					itemView = inflater.inflate(R.layout.message_item, null);
				} else {
					itemView = convertView;
				}
				
				TextView userView = (TextView) itemView
						.findViewById(R.id.user);
				TextView titleView = (TextView) itemView
						.findViewById(R.id.title);
				TextView dateView = (TextView) itemView
						.findViewById(R.id.date);
				ImageView imageView = (ImageView) itemView
						.findViewById(R.id.user_image);

				// Inflate the views
				Message m = getItem(position);
				userView.setText(m.getUserName());
				titleView.setText(m.getTitle());
				String dateStr = new SimpleDateFormat(DATE_FORMAT_SHOW).format(m.getDate());
				dateView.setText(dateStr);

				// Return the view
				return itemView;
			}
		};	
		listView.setAdapter(arrayAdapter);
	}

	/**
	 * Called when an item is clicked
	 */
	@Override
	public void onItemClick(AdapterView av, View v, int index, long arg3) {		
		// Para enviar parametros a las nuevas actividades
		Bundle bun = new Bundle();
		bun.putInt("message_id", messages.get(index).getId());
		Intent read_message = new Intent(this, ReadMessage.class);
		read_message.putExtras(bun);
		startActivity(read_message);
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

		switch (item.getItemId()) {
		case R.id.menu_refresh:
			// TODO ask JADE to get the messages
			Toast.makeText(getBaseContext(), R.string.refresh_complete,
					Toast.LENGTH_SHORT).show();
			break;
		case R.id.menu_show:
			break;
			
		// Order by date
		case R.id.show_opt1_date: 
		case R.id.show_opt3_all_date: // TODO
			item.setChecked(true);			
			Toast.makeText(getBaseContext(), R.string.show_opt1_date,
					Toast.LENGTH_SHORT).show();
			SQLiteInterface.addMessages(this, messages, MessagesDBHelper.DATE);
			arrayAdapter.notifyDataSetChanged();
			break;

			// Order by rating
		case R.id.show_opt2_rating:
		case R.id.show_opt4_all_rating: // TODO
			item.setChecked(true);
			Toast.makeText(getBaseContext(), R.string.show_opt2_rating,
					Toast.LENGTH_SHORT).show();
			SQLiteInterface.addMessages(this, messages, MessagesDBHelper.RATING);
			arrayAdapter.notifyDataSetChanged();
			break;
		}
		return true;
	}
}