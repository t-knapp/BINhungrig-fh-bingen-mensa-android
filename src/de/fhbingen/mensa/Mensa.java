package de.fhbingen.mensa;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.util.Log;

public class Mensa extends Application {

	public final static String APIURL = "http://nuke.volans.uberspace.de/fh-bingen/mensa/dev/API.php?";
	public final static String GALLERYURL = "http://nuke.volans.uberspace.de/fh-bingen/mensa/dev/gallery.php?";
	
	private final static String TAG = Mensa.class.getName();
	
	public final static String PREF_USER = "UserSettings";
	
	public static UserRole userRole;
	
	public enum UserRole {
		STUDENT, OFFICIAL
	}

	public Mensa() {
		dayMap = new HashMap<String, List<Dish>>();
	}

    /**
     *
     * @param result JSON-formatted dish-array
     * @param append if true, the result String data will be appended
     */
	public void loadWeek(String result, boolean append) {
		
		if(!dayMap.isEmpty() && !append){
			return;
		}
		try {
			// Filling the data in an array
			final JSONArray jsonArray = new JSONArray(result);

			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);

				String objDate = jsonObject.getString("date");

				// Log.d(TAG, "Date " + objDate);

				if (!dayMap.containsKey(objDate)) {
					dayMap.put(objDate, new LinkedList<Dish>());
				}

				dayMap.get(objDate).add(
					new Dish(
						jsonObject.getInt("id_dishes"),
						objDate,
						jsonObject.getString("text"),
						jsonObject.getDouble("priceStudent"),
						jsonObject.getDouble("priceOfficial"),
						jsonObject.getDouble("ravg"),
						jsonObject.getInt("pid"),
						jsonObject.getString("thumb")
					)
				);

			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void setDishPicture(String dateQuery, int id_dishes, byte[] decodedData){
		
		if(dateQuery == null) return;
		if(dateQuery.isEmpty()) return;
		if(decodedData == null) return;
		if(decodedData.length == 0) return;
		
		List<Dish> dayList = dayMap.get(dateQuery);
		
		for (Dish dish : dayList) {
			if(dish.getId_dishes() == id_dishes){
				dish.setPicture(decodedData);
			}
		}
		
	}
	
	public void setAvgRating(String dateQuery, int id_dishes, double avg){
		if(dateQuery == null) return;
		if(dateQuery.isEmpty()) return;
		
		List<Dish> dayList = dayMap.get(dateQuery);
		
		for (Dish dish : dayList) {
			if(dish.getId_dishes() == id_dishes){
				dish.setAvgRating(avg);
			}
		}
	}

	
	public List<Dish> getDay(String cal) {
		return dayMap.get(cal);
	}

	@Override
	public String toString() {
		return "Mensa Application";
	}

    public int daysTillSunday(){
        //NOW -> Sunday
        final Calendar rightNow = Calendar.getInstance();
        final Calendar sunday = Calendar.getInstance();
        sunday.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);

        final long diff = sunday.getTimeInMillis() - rightNow.getTimeInMillis();

        return (int)(diff / (24 * 60 * 60 * 1000) + 1);
    }

	/**
	 * Returns current week of year formatted YYYYWW
	 * @return String YYYYWW
	 */
	public static String getCurrentWeek(){
		final Calendar rightNow = Calendar.getInstance();
		return String.format(
				Locale.GERMAN,
				"%d%02d",
				rightNow.get(Calendar.YEAR),
				rightNow.get(Calendar.WEEK_OF_YEAR)
		);
	}

    /**
     * Returns next week of year formatted YYYYWW
     * @return String YYYYWW
     */
	public static String getNextWeek(){
        final Calendar rightNow = Calendar.getInstance();
        rightNow.add(Calendar.WEEK_OF_YEAR, 1);
        return String.format(
                Locale.GERMAN,
                "%d%02d",
                rightNow.get(Calendar.YEAR),
                rightNow.get(Calendar.WEEK_OF_YEAR)
        );
    }

    public boolean getNextWeekLoaded(){
        return  this.nextWeekLoaded;
    }

    public void setNextWeekLoaded(boolean nextWeekLoaded){
        this.nextWeekLoaded = nextWeekLoaded;
    }

	// private Database db;
	private HashMap<String, List<Dish>> dayMap;
	private boolean nextWeekLoaded = false;
	/*
	 _   _ _   _ _     
	| | | | | (_) |    
	| | | | |_ _| |___ 
	| | | | __| | / __|
	| |_| | |_| | \__ \
	 \___/ \__|_|_|___/
	 */

	public static String toYYYYMMDD(Calendar calendar){
		//Log.d("UTILS", calendar.toString());
		return String.format(
				Locale.GERMAN,
                "%d-%02d-%02d",
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.DAY_OF_MONTH)
        );
	}
	
	public static String toDDMMYYYY(Calendar calendar){
		//Log.d("UTILS", calendar.toString());
		return String.format(
				Locale.GERMAN,
                "%d.%02d.%d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR)
        );
	}

}
