package de.fhbingen.mensa;

import android.content.Context;
import android.util.Log;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Pattern;

import de.fhbingen.mensa.Exceptions.NotExcepectedServerAnswer;

public class Dish implements Serializable {

    /**
     * 
     */
	private static final long serialVersionUID = 3377898359032737970L;

    /**
     * Day format used by the api and saved in the dish.
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /** Constructor of dish
     *
     * @param id_dishes
     * @param date
     * @param text
     * @param priceStudent
     * @param priceOfficial
     */
	public Dish(
		int id_dishes,
		String date,
		String text,
		double priceStudent,
		double priceOfficial,
		double avgRating,
		int id_pictures,
		String thumb/*,
        Context context*/)
	{
		this.id_dishes = id_dishes;
		this.date = date;
		this.text = text;
		this.priceStudent = priceStudent;
		this.priceOfficial = priceOfficial;
		this.avgRating = avgRating;
		this.id_pictures = id_pictures;
		this.thumb = thumb.getBytes();
        //this.context = context;
        //try{
		//    this.day_of_week = computeDayOfWeek();
		//}
		//catch (NotExcepectedServerAnswer e){
        //    this.day_of_week = "Montag";
        //}
	}
	
	
	public int getId_dishes() {
		return id_dishes;
	}

	public String getDate() {
		return date;
	}

	public String getText() {
		return text;
	}

	public double getPriceStudent() {
		return priceStudent;
	}

	public double getPriceOfficial() {
		return priceOfficial;
	}

	public double getAvgRating() {
		return avgRating;
	}
	
	public void setAvgRating(double r){
		avgRating = r;
	}

    public String getDayOfWeek(){
        return day_of_week;
    }

    public byte[] getThumb(){
    	return thumb;
    }
    
    public byte[] getPicture() {
		return picture;
    }

	public void setPicture(byte[] picture) {
		this.picture = picture;
	}

	public int getId_pictures() {
		return id_pictures;
	}
	

	/** Transforms the String of Date in an valid Calendar object.
     *  Calculates the responding day of the week with the calendar object.
     * @return a String with the weekday e.g. Monday
     * @throws NotExcepectedServerAnswer if the String of date doesn't match to yyyy-mm-dd
     */
    private String computeDayOfWeek() throws NotExcepectedServerAnswer{
        boolean matches = Pattern.matches("\\d{4}-\\d{2}-\\d{2}", getDate());
        assert(matches);
        Calendar cal = Calendar.getInstance();
        if (matches){
           //converting String into calendar object
            try{
                cal.setTime(DATE_FORMAT.parse(getDate()));
                Log.d(TAG, "Calendarvalues changed to:\n" + cal.toString());
            }
            catch (ParseException pe){
                Log.e(TAG, "ParseException in Dish - computeDayOfWeek");
                throw new NotExcepectedServerAnswer("Error while parsing");
            }
        }
        else {
            throw new NotExcepectedServerAnswer("Dateformat doesn't match to the expected result yyyy-mm-dd* ");
        }
        String[] DAYS = null;
        try{
            DAYS = context.getResources().getStringArray(R.array.dayName_array);
        }
        catch (NullPointerException e){
            Log.e(TAG, "NullPointer bei days Array abrufen");
        }
        catch (Exception e){
            Log.e(TAG, e.toString() +"\n" + e.getCause());
        }
        String retValue = DAYS[cal.get(Calendar.DAY_OF_WEEK) -1]; // Sonday = -1
        Log.d(TAG, "returnValue of getDay " + retValue);
        return retValue;
    }

    /**
     * Method that determines if the dish is served today.
     * @return true if dish is served today.
     * @throws ParseException
     */
    public boolean isServedToday() throws ParseException {
        Log.d(TAG, "dish.date : " + getDate());
        Log.d(TAG, "now.date  : " + DATE_FORMAT.format(new Date()));
        return getDate().equals(DATE_FORMAT.format(new Date()));
    }

	private int id_dishes;
	private String date;
	private String text;
	private double priceStudent;
	private double priceOfficial;
	
	private int id_pictures;
	private byte[] thumb;
	private byte[] picture;
	
	private double avgRating;

	private String day_of_week;
	
    private Context context;

    private final static String TAG = Dish.class.getName();
}
