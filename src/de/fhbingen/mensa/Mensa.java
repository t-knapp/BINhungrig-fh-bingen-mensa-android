package de.fhbingen.mensa;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;

import android.app.Application;
import android.text.format.Time;

public class Mensa extends Application {

	private final static String APIURL = "http://nuke.volans.uberspace.de/fh-bingen/mensa/dev/API.php?";
	
	public Mensa() {
		// TODO Auto-generated constructor stub
	}
	
	public String getDishes() throws InterruptedException, ExecutionException{
		Calendar rightNow = Calendar.getInstance();
		return this.getDishes(
				rightNow.get(Calendar.YEAR), rightNow.get(Calendar.WEEK_OF_YEAR));
	}
	
	public String getDishes(int year, int week) throws InterruptedException, ExecutionException{
		return this.getDishes(Integer.toString(year), String.format("%02d", week));
	}
	
	public String getDishes(String year, String weekOfYear) throws InterruptedException, ExecutionException{
		ContentTask ct = new ContentTask();
		ct.execute(APIURL + "getWeek=" + year + weekOfYear);
		return ct.get();
	}
	
	
	@Override
	public String toString() {
		return "Mensa Application";
	}
	
	private Database db;

}
