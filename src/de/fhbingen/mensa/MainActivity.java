package de.fhbingen.mensa;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        Log.i(TAG, "ContentView is setted");

		Mensa mensa = (Mensa) this.getApplication();

        Log.i(TAG, "Mensa: " + mensa.toString());
		Toast.makeText(this, mensa.toString(), Toast.LENGTH_LONG).show();

		String readTwitterFeed = "";
		try {
			readTwitterFeed = mensa.getDishes(2014,03);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			Log.e(TAG, "mensa.getDishes - InterruptedException" + e1.toString());
		} catch (ExecutionException e1) {
			// TODO Auto-generated catch block
			Log.e(TAG, "mensa.getDishes - ExecutionException" + e1.toString());
		}

		
	    try {
          // Filling the data in an array
	      JSONArray jsonArray = new JSONArray(readTwitterFeed);
	      Log.i(TAG,
	          "Number of entries " + jsonArray.length());
          final List<Dish> list = new ArrayList<Dish>();
	      for (int i = 0; i < jsonArray.length(); i++) {
	        JSONObject jsonObject = jsonArray.getJSONObject(i);
	        //Log.i(TAG, jsonObject.getString("text") );
            Log.d(TAG, "Date " + jsonObject.getString("date"));

            list.add(new Dish(
                    jsonObject.getInt("id_dishes"),
                    jsonObject.getString("date"),
                    jsonObject.getString("text"),
                    jsonObject.getDouble("priceStudent"),
                    jsonObject.getDouble("priceOfficial"),
                    MainActivity.this
            ));
	      }

            final List<Dish> listMonday = new ArrayList<Dish>();

            Log.d(TAG, "Before foreach for for Monday");
            for(Dish d: list){
                try{
                    String day = d.getDayOfWeek();
                     Log.d(TAG, day);

                    if (day.equals("Montag"))
                        listMonday.add(d);
                }
                catch(Exception e){
                    Log.e(TAG, "Message: " + e.getMessage() + "\nCause: " + e.getCause() );
                }
            }

            // Filling the Adapter with the generated values
            final DishItemAdapter adapter = new DishItemAdapter(
                    this,
                    listMonday
            );

            // Connection between ListView and Adapter
            setListAdapter(adapter);

	      
	    } catch (Exception e) {
	      Log.e(TAG, "Exception cause: " + e.getCause() + "\nException message" +e.getMessage() + "\nException toStr" + e.toString());
	    }

	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    private final static String TAG = MainActivity.class.getName();

}
