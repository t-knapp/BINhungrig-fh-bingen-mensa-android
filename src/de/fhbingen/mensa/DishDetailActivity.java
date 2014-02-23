package de.fhbingen.mensa;

import java.util.Arrays;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RadialGradient;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class DishDetailActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dish_detail);
		
		final Mensa mensa = (Mensa) this.getApplication();
		
		final Dish dish = (Dish) getIntent().getExtras().getSerializable("data");
		
		final TextView labelRatings = (TextView) findViewById(R.id.textView_headingDoRating);
		
		TextView tv;
		
		//Set dish-text
		tv = (TextView) findViewById(R.id.dish_text);
		tv.setText(dish.getText());
		
		//Set dish-price
		tv = (TextView) findViewById(R.id.dish_price);
		tv.setText(
			String.format(
			    Locale.GERMAN,
				"%.2f €",
				(Mensa.userRole == Mensa.UserRole.STUDENT)
					? dish.getPriceStudent()
				    : dish.getPriceOfficial()
			)
		);
		
		//Set average rating
		tv = (TextView) findViewById(R.id.textView_avgRating);
		tv.setText(String.format(Locale.GERMAN, "%.1f", dish.getAvgRating()));
				
		//Set ratings
		int[] ratings  = mensa.loadRating(dish.getId_dishes());


		
		CustomBar cb;
		int max = 0;
		int numberRatings = 0;
		for (int i : ratings) {
			max = i > max ? i : max;
			numberRatings += i;
		}

		for (int i = 0; i < ratings.length; i++){
			cb = (CustomBar) findViewById(CUSTOMBARS[i]);
			cb.setData(i, max, ratings[i]);
		}

		//Set nubmer of rating
		tv = (TextView) findViewById(R.id.textView_numberRatings);
		tv.setText(String.format("%d Bewertungen", numberRatings));

		
		//Load and set dish-picture
		// if thumb.length > 0
		//     if picdata.length > 0
		//         show large image
		//         and add listener for gallery
		//     else
		//         show thumb image
		//         and add listener for download large one
		// else
		//     show default image
		//     add listener for picture intent
		final ImageView iv = (ImageView) findViewById(R.id.dish_picture);
		
		byte[] thumbBytes = dish.getThumb();
		
		if(thumbBytes.length > 0){
			
			byte[] pictureBytes = dish.getPicture();
			if(pictureBytes != null){
				//Show large image

				//decodedString = mensa.loadPicture(dish.getId_dishes(), dish.getId_pictures());

				Bitmap decodedByte = BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.length); 
				iv.setImageBitmap(decodedByte);
				
				iv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Log.d("DDA", "startWebView");
					}
				});
								
			} else {
				// Show thumbnail
				byte[] decodedString = Base64.decode(thumbBytes, Base64.DEFAULT);
				Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
				
				iv.setImageBitmap(decodedByte);
				
				iv.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Log.d("DDA", "downloadLargePicture");
						byte[] decodedString = mensa.loadPicture(dish.getId_dishes(), dish.getId_pictures());

						Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
						
						iv.setImageBitmap(decodedByte);
						iv.setOnClickListener(null);
						
						iv.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								Log.d("DDA", "startWebView");
							}
						});
					}
				});
			}
			
			iv.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					Log.d("DDA", "startPhotoIntent");
					return true;
				}
			});
			
		} else {
			iv.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Log.d("DDA", "startPhotoIntent");
				}
			});
		}
		/*
		iv.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				Log.d("DDA", "startPhotoIntent");
				return true;
			}
		});
		*/
				
		//Rating
		final RatingBar bar = (RatingBar) findViewById(R.id.ratingBarDish);
		bar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
			
			@Override
			public void onRatingChanged(RatingBar ratingBar, float rating,
					boolean fromUser) {
				ratingBar.setRating(rating>0?rating:1);				
			}
		});
		
		final Button btn = (Button) findViewById(R.id.button1);
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				ContentTask ct = new ContentTask();
				ct.execute(
					Mensa.APIURL
					+ "insertDishRating=" 
					+ dish.getId_dishes() 
					+ ";" + (int)bar.getRating());
				try {
					String result = ct.get();
					if(result.equals("true")){
						Log.d("DDA", "Rating submitted.");
						//TODO: Hide Button and Bar, or disable them, store in DB
						//TODO: Update Ratings
						btn.setVisibility(View.GONE);
						bar.setIsIndicator(true);
						labelRatings.setText("Deine Bewertung");
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
			
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.dish_detail, menu);
		return true;
	}
	
	private final int[] CUSTOMBARS = {R.id.customBar1, R.id.customBar2, R.id.customBar3, R.id.customBar4, R.id.customBar5};
}
