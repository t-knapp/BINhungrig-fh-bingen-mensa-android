package de.fhbingen.mensa;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

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
		
		Mensa mensa = (Mensa) this.getApplication();
		
		Toast.makeText(this, mensa.toString(), Toast.LENGTH_LONG).show();

		
		// Setup the list view
        final ListView newsEntryListView = (ListView) findViewById(R.id.listView1);
        final DishItemAdapter newsEntryAdapter = new DishItemAdapter(this, R.layout.dish_list_item);
        newsEntryListView.setAdapter(newsEntryAdapter);
        
	
	
		String readTwitterFeed = "";
		try {
			readTwitterFeed = mensa.getDishes(2014,03);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			
		//textview.setText(readTwitterFeed);

		//Toast.makeText(this, newsEntryAdapter.toString(), Toast.LENGTH_LONG).show();
		

		newsEntryAdapter.add(
        		new Dish(-1,
        				 "heute",
        				 "Text text text",
        				 3.25,
        				 4.00
				 )
    		);

		
	    try {
	      JSONArray jsonArray = new JSONArray(readTwitterFeed);
	      Log.i(MainActivity.class.getName(),
	          "Number of entries " + jsonArray.length());
	      
	      for (int i = 0; i < jsonArray.length(); i++) {
	        JSONObject jsonObject = jsonArray.getJSONObject(i);
	        Log.i(MainActivity.class.getName(), jsonObject.getString("text") );
	        /*
	        newsEntryAdapter.add(
        		new Dish(jsonObject.getInt("id_dishes"),
        				 jsonObject.getString("date"),
        				 jsonObject.getString("text"),
        				 jsonObject.getDouble("priceStudent"),
        				 jsonObject.getDouble("priceOfficial")
				 )
    		);
    		*/
	        
	      }
	      
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
