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

	public String getDishes() throws InterruptedException, ExecutionException {
		Calendar rightNow = Calendar.getInstance();
		return this.getDishes(rightNow.get(Calendar.YEAR),
				rightNow.get(Calendar.WEEK_OF_YEAR));
	}

	public String getDishes(int year, int week) throws InterruptedException,
			ExecutionException {
		return this.getDishes(Integer.toString(year),
				String.format("%02d", week));
	}

	public String getDishes(String year, String weekOfYear)
			throws InterruptedException, ExecutionException {
		ContentTask ct = new ContentTask();
		ct.execute(APIURL + "getWeek=" + year + weekOfYear);
		return ct.get();
	}

	public void loadWeek(String result) {
		
		if(!dayMap.isEmpty()){
			return;
		}
		
		try {
			// Filling the data in an array
			JSONArray jsonArray = new JSONArray(result);

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

				// Calendar cal = new GregorianCalendar();
				// SimpleDateFormat dateFormat = new
				// SimpleDateFormat("yyyy-mm-dd");
				// cal.setTime(dateFormat.parse(objDate));

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
	/*
	public byte[] loadPicture(int id_dishes, int id_pictures){
		ContentTask ct = new ContentTask();
		ct.execute(APIURL + "getDishPhotoData=" + id_pictures);
		
		try {
			String result = ct.get();
			if(result.length() > 0 && !result.equals("false")){
			
				JSONObject jsonObj = new JSONObject(result);
								
				Calendar rightNow = Calendar.getInstance();
				
				String query = "2014-01-13";
				
				//TODO: Set to live mode
				
				//query = String.format(
				//	"%d-%02d-%02d",
				//	rightNow.get(Calendar.YEAR),
				//	rightNow.get(Calendar.MONTH),
				//	rightNow.get(Calendar.DAY_OF_MONTH)
				//);
				
				
				Log.d(TAG, "query : " + query);
				
				List<Dish> dayList = dayMap.get(
					query
				);
				
				for (Dish dish : dayList) {
					if(dish.getId_dishes() == id_dishes){
						String pictureData = jsonObj.getString("pictureData");
						if(pictureData.length() > 0){
							byte[] decodedString = Base64.decode(pictureData.getBytes(), Base64.DEFAULT);
							dish.setPicture(decodedString);
							return decodedString;
						} else {
							return null;
						}
					}
				}
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return null;
	}
	*/
	
	/*public int[] loadRating(int id_dishes){
		ContentTask ct = new ContentTask();
		ct.execute(APIURL + "getRatings=" + id_dishes );
		
		int[] retVal = new int[5];
		
		try {
			String result = ct.get();
			
			if(!result.isEmpty()){
				JSONArray jsonArray = new JSONArray(result);
				for(int i = 0; i < jsonArray.length(); i++){
					retVal[i] = jsonArray.getInt(i);
				}
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return retVal;
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return retVal;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return retVal;
		}
		return retVal;
	}
	*/
	
	public List<Dish> getDay(String cal) {
		return dayMap.get(cal);
	}

	@Override
	public String toString() {
		return "Mensa Application";
	}
	
	/**
	 * Returns current week of year formatted YYYYWW
	 * @return String YYYYWW
	 */
	public static String getCurrentWeek(){
		Calendar rightNow = Calendar.getInstance();
		return String.format(
				Locale.GERMAN,
				"%d%02d",
				rightNow.get(Calendar.YEAR),
				rightNow.get(Calendar.WEEK_OF_YEAR)
		);
	}
	
	
	
	// private Database db;
	private HashMap<String, List<Dish>> dayMap;
	
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
