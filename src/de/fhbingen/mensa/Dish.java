package de.fhbingen.mensa;

public class Dish {

	public Dish(
		int id_dishes,
		String date,
		String text,
		double priceStudent,
		double priceOfficial)
	{
		this.id_dishes = id_dishes;
		this.date = date;
		this.text = text;
		this.priceStudent = priceStudent;
		this.priceOfficial = priceOfficial;
		this.avgRating = 3.33;
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

	private int id_dishes;
	private String date;
	private String text;
	private double priceStudent;
	private double priceOfficial;
	
	private double avgRating;
}
