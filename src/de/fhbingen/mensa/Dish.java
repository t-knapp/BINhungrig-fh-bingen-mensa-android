package de.fhbingen.mensa;

import android.content.Context;
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

    public void setPicture(byte[] picture, int id_pictures) {
        this.picture = picture;
        this.id_pictures = id_pictures;
    }

    public boolean hasPicture() {
        return getId_pictures() != -1;
    }



    public int getId_pictures() {
		return id_pictures;
	}

    /**
     * Method that determines if the dish is served today.
     * @return true if dish is served today.
     * @throws ParseException
     */
    public boolean isServedToday() throws ParseException {
        return getDate().equals(DATE_FORMAT.format(new Date()));
    }

    @Override
    public String toString(){
        return "Dish (id_dishes: " + id_dishes + "; date: " + date + "; text: " + text + "; id_pictures: "
                + id_pictures + "; thumb: " + thumb + "; picture: " + picture + "; avgRating: " + avgRating
                + "; priceStd: " + priceStudent + "; priceOff: " + priceOfficial;
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
