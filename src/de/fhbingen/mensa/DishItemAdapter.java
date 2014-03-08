package de.fhbingen.mensa;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class DishItemAdapter extends ArrayAdapter<Dish> {

	public DishItemAdapter(Context context, List<Dish> dishes) {
		super(context, R.layout.dish_list_item, dishes);
		this.context = context;

        dishIdMap = new HashMap<Dish, Integer>();
        for (int i=0; i < dishes.size(); i++){
            dishIdMap.put(dishes.get(i), i);
        }
	}

    public long getItemId(int position){
        Dish item = getItem(position);
        return dishIdMap.get(item);
    }

	public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(
                Context.LAYOUT_INFLATER_SERVICE
        );
        Dish dish = getItem(position);

        View rowView = inflater.inflate(
                R.layout.dish_list_item,
                parent,
                false
        );

        // Getting the layout elements of the listitem View
        TextView title = (TextView) rowView.findViewById(R.id.textView_title);
        TextView price = (TextView) rowView.findViewById(R.id.textView_price);

		// Setting the values to the layout elements
        title.setText(dish.getText());

        // Formatting price
		price.setText(
			String.format(
			    Locale.GERMAN,
				"%.2f €",
				(Mensa.userRole == Mensa.UserRole.STUDENT)
					? dish.getPriceStudent()
					: dish.getPriceOfficial()
			)
		);
        
		// Set avg. rating
		TextView rating = (TextView) rowView.findViewById(R.id.textView_rating);
		double avgRating = dish.getAvgRating();
		if(avgRating != -1){
			rating.setText(String.format(Locale.GERMAN, "%.1f", dish.getAvgRating()));
		} else {
			rating.setVisibility(View.GONE);
		}
		
		// Insert thumbnail if available
		byte[] encodedString = dish.getThumb();
		if(encodedString.length > 0){
			ImageView iv = (ImageView) rowView.findViewById(R.id.imageView_dish);
			byte[] decodedString = Base64.decode(encodedString, Base64.DEFAULT);
			Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length); 
			iv.setImageBitmap(decodedByte);
		}
		
		return rowView;
	}

    private Context context;
    private HashMap<Dish, Integer> dishIdMap;
}
