package de.fhbingen.mensa;

import android.app.ListFragment;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.support.v4.app.NavUtils;
import android.widget.CheckedTextView;
import com.actionbarsherlock.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;

public class SettingsActivity extends SherlockActivity implements OnItemSelectedListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
        getSupportActionBar().setHomeButtonEnabled(true);

        CheckedTextView autoDownload = (CheckedTextView) findViewById(R.id.auto_download_large_pictures);
        autoDownload.setChecked(SettingsHelper.isAutoDownloadLargePicturesEnabled());

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
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
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
			de.fhbingen.mensa.Fragments.ListFragment.roleChanged = true;
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// Do nothing =)
	}

	private int roleIndex;

    public void onAutoDownloadLargePicturesCheckboxClicked(View view) {
        CheckedTextView autoDownload = (CheckedTextView) view;
        autoDownload.toggle();
        SettingsHelper.setIsAutoDownloadLargePicturesEnabled(autoDownload.isChecked());
    }
}
