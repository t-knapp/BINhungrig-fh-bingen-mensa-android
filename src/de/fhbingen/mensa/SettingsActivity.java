package de.fhbingen.mensa;

import android.os.Bundle;
import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class SettingsActivity extends Activity implements OnItemSelectedListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		Spinner spinner = (Spinner) findViewById(R.id.spinnerUser);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.users_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);
		
		//Set current selection
		SharedPreferences settings = getSharedPreferences(Mensa.PREF_USER, 0);

		int currentPos = settings.getInt("userRole", Mensa.UserRole.STUDENT.ordinal());
		roleIndex = currentPos;
		spinner.setSelection(currentPos);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.settings, menu);
		return true;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int pos,
			long id) {
		// We need an Editor object to make preference changes.
		// All objects are from android.context.Context
		SharedPreferences settings = getSharedPreferences(Mensa.PREF_USER, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt("userRole", pos);
		
		// Commit the edits!
		editor.commit();
		
		// Commit to Mensa
        Mensa.userRole = Mensa.UserRole.values()[settings.getInt("userRole", Mensa.UserRole.STUDENT.ordinal())];
		
		if(roleIndex != pos){
			MainActivity.roleChanged = true;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// Do nothing =)
	}

	private int roleIndex;
}
