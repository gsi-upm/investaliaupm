package gsi.investalia.android.app;

import gsi.investalia.android.db.SQLiteInterface;
import gsi.investalia.domain.User;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class Home extends Activity {

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        // Get the user from the shared preferences
    	User loggedUser = SQLiteInterface.getLoggedUser(this);
               
    	// Set the views
        TextView username = (TextView) findViewById(R.id.home_TextView02);
        username.setText(getString(R.string.welcome) + " " + loggedUser.getName());   
    }
	
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
	
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		super.onMenuItemSelected(featureId, item);
		// There is only one option: log out
		logOut();
		return true;
	}
}
