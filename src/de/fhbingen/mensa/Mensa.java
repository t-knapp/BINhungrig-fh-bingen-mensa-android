package de.fhbingen.mensa;

import java.util.*;

import android.content.Context;
import android.content.pm.PackageManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Application;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class Mensa extends Application {

	public final static String APIURL = "http://nuke.volans.uberspace.de/fh-bingen/mensa/dev/API.php?";
	public final static String GALLERYURL = "http://nuke.volans.uberspace.de/fh-bingen/mensa/dev/gallery.php?";
	
	private final static String TAG = Mensa.class.getName();
	
	public final static String PREF_USER = "UserSettings";
	
	public static UserRole userRole;

    private static Mensa MENSA;

	public enum UserRole {
		STUDENT, OFFICIAL
	}

	public Mensa() {
        MENSA = this;
	}


    public boolean isConnected(){
        // Initial determination of internet state
        ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }




	@Override
	public String toString() {
		return "Mensa Application";
	}

    public int daysTillSunday(){
        /*
        Sun -> 1
        Mon -> 7
        Tue -> 6
        Wed -> 5
        Thu -> 4
        Fri -> 3
        Sat -> 2
         */
        final Calendar rightNow = Calendar.getInstance();

        final Calendar sunday = (Calendar) rightNow.clone();
        sunday.set(Calendar.DAY_OF_WEEK,Calendar.SUNDAY);

        final long diff = sunday.getTimeInMillis() - rightNow.getTimeInMillis();

        return (int) Math.ceil(diff / (24 * 60 * 60 * 1000f)) + 1;
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
                "%02d.%02d.%d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR)
        );
	}

    public static Mensa getMensa() {
        return MENSA;
    }

    /**
     * Determines if mensa has already closed NOW.
     * Mensa never opens on weekends, therefore <b>on weekends returns false</b>
     * @return true if closed, false if not
     */
    public static boolean isAlreadyClosed(){
        final Calendar rightNow = Calendar.getInstance(Locale.GERMAN);

        final int DAY_OF_WEEK = rightNow.get(Calendar.DAY_OF_WEEK);
        if(DAY_OF_WEEK == Calendar.SATURDAY || DAY_OF_WEEK == Calendar.SUNDAY){
            return false;
        }

        final Calendar closesAt = Calendar.getInstance(Locale.GERMAN);
        closesAt.set(Calendar.HOUR_OF_DAY, 14); /* HOUR_OF_DAY means 24h format, HOUR 12h */
        closesAt.set(Calendar.MINUTE, 30); /* half an hour tolerance */
        closesAt.set(Calendar.SECOND, 0);

        return closesAt.before(rightNow);
    }

    public static boolean isStillClosed(){
        final Calendar rightNow = Calendar.getInstance(Locale.GERMAN);

        final int DAY_OF_WEEK = rightNow.get(Calendar.DAY_OF_WEEK);
        if(DAY_OF_WEEK == Calendar.SATURDAY || DAY_OF_WEEK == Calendar.SUNDAY){
            return false;
        }

        final Calendar opensAt = Calendar.getInstance(Locale.GERMAN);
        opensAt.set(Calendar.HOUR_OF_DAY, 11); /* HOUR_OF_DAY means 24h format, HOUR 12h */
        opensAt.set(Calendar.MINUTE, 0); /* half an hour tolerance */
        opensAt.set(Calendar.SECOND, 0);

        return opensAt.after(rightNow);
    }

    //<issue 11> https://github.com/t-knapp/BINhungrig-fh-bingen-mensa-android/issues/11
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        Mensa.context = getApplicationContext();
    }

    public static final String getUserAgentString(){
        try {
            return context.getString(R.string.user_agent_prefix)
                    + context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "getUserAgentString() : PackageManager.NameNotFoundException");
            return context.getString(R.string.user_agent_prefix) + "UNAVAILABLE";
        }
    }
    //</issue 11>
}
