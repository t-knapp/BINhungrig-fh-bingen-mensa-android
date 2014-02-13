package de.fhbingen.mensa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        Log.i(LOG, "ContentView is setted");

		Mensa mensa = (Mensa) this.getApplication();

        Log.i(LOG, "Mensa: " + mensa.toString());
		Toast.makeText(this, mensa.toString(), Toast.LENGTH_LONG).show();

		
		// Setup the list view
        final ListView newsEntryListView = (ListView) findViewById(R.id.listView1);
	
		String readTwitterFeed = "";
		try {
			readTwitterFeed = mensa.getDishes(2014,03);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			Log.e(LOG, "mensa.getDishes - InterruptedException" + e1.toString());
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			Log.e(LOG, "mensa.getDishes - ExecutionException" + e1.toString());
		}

		
	    try {
          // Filling the data in an array
	      JSONArray jsonArray = new JSONArray(readTwitterFeed);
	      Log.i(LOG,
	          "Number of entries " + jsonArray.length());
          final List<Dish> list = new ArrayList<Dish>();
	      for (int i = 0; i < jsonArray.length(); i++) {
	        JSONObject jsonObject = jsonArray.getJSONObject(i);
	        Log.i(LOG, jsonObject.getString("text") );
            list.add(new Dish(
                    jsonObject.getInt("id_dishes"),
                    jsonObject.getString("date"),
                    jsonObject.getString("text"),
                    jsonObject.getDouble("priceStudent"),
                    jsonObject.getDouble("priceOfficial")
            ));
	      }

            // Filling the Adapter with the generated values
            final DishItemAdapter adapter = new DishItemAdapter(
                    this,
                    list
            );

            // Connection between ListView and Adapter
            newsEntryListView.setAdapter(adapter);

	      
	    } catch (Exception e) {
	      Log.e(LOG, e.getMessage());
	    }
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    private final static String LOG = MainActivity.class.getName();
}
