package de.fhbingen.mensa;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        Log.i(TAG, "ContentView is setted");

		Mensa mensa = (Mensa) this.getApplication();
		
		// Load userrole from preferences
		SharedPreferences settings = getSharedPreferences(Mensa.PREF_USER, 0);
        Mensa.userRole = Mensa.UserRole.values()[settings.getInt("userRole", Mensa.UserRole.STUDENT.ordinal())];

		mensa.loadWeek();
		
		dlist = mensa.getDay("2014-01-13");
		
	    try {
            // Filling the Adapter with the generated values
            adapter = new DishItemAdapter(
                    this,
                    dlist
            );

            // Connection between ListView and Adapter
            setListAdapter(adapter);
      
	    } catch (Exception e) {
	      Log.e(TAG, "Exception cause: " + e.getCause() + "\nException message" +e.getMessage() + "\nException toStr" + e.toString());
	    }
	    
	    // Set date
	    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
	    SimpleDateFormat dayFormat   = new SimpleDateFormat("EEEE", Locale.GERMAN);
	    SimpleDateFormat monthFormat = new SimpleDateFormat("MMMMMMMMM", Locale.GERMAN);
	    try {
			Date date = inputFormat.parse("2014-01-13");

			Calendar cal = new GregorianCalendar();
			cal.setTime(date);
			
			TextView tv = (TextView) findViewById(R.id.textView_date);
			tv.setText(
				String.format(
					"%s, %2d. %s %d",
					dayFormat.format(date),
					cal.get(Calendar.DAY_OF_MONTH),
					monthFormat.format(date),
					cal.get(Calendar.YEAR)
				)
			);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent detail = new Intent(this, DishDetailActivity.class);
		detail.putExtra("data", (Dish)getListView().getItemAtPosition(position));
		startActivity(detail);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Intent settings = new Intent(this, SettingsActivity.class);
			//startActivity(settings);
					
			startActivityForResult(settings, 1337);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == 1337 && resultCode == 0 && roleChanged){
			
			
			//TODO: Update view
			
			roleChanged = false;
		}
	}
		
    private final static String TAG = MainActivity.class.getName();
    
    public static boolean roleChanged;
    
    private List<Dish> dlist;
    private DishItemAdapter adapter;
}
