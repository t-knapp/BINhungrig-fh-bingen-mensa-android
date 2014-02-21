package de.fhbingen.mensa;

import java.util.Arrays;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RadialGradient;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class DishDetailActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dish_detail);
		
		Mensa mensa = (Mensa) this.getApplication();
		
		Dish dish = (Dish) getIntent().getExtras().getSerializable("data");
		
		TextView tv;
		
		//Set dish-text
		tv = (TextView) findViewById(R.id.dish_text);
		tv.setText(dish.getText());
		
		//Set dish-price
		tv = (TextView) findViewById(R.id.dish_price);
		tv.setText(
			String.format(
				"%.2f €",
				(Mensa.userRole == Mensa.UserRole.STUDENT)
					? dish.getPriceStudent()
				    : dish.getPriceOfficial()
			)
		);
		
		//Set avg. rating
		int[] ratings  = mensa.loadRating(dish.getId_dishes());

		
		//Load and set dish-picture
		if(dish.getId_pictures() != -1){ //Server sets -1 if no picture available
			byte[] decodedString;
			if(dish.getPicture() == null){
				decodedString = mensa.loadPicture(dish.getId_dishes(), dish.getId_pictures());
			} else {
				decodedString = dish.getPicture();
			}
			if(decodedString != null){
				ImageView iv = (ImageView) findViewById(R.id.dish_picture);
				Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
				iv.setImageBitmap(decodedByte);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.dish_detail, menu);
		return true;
	}

}
