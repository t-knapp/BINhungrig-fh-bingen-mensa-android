package de.fhbingen.mensa;

import android.animation.LayoutTransition;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

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
        // TODO: Formatierung der Ausgabe, sodass immer 2 Nachkommastellen
        price.setText(Double.toString(dish.getPriceStudent()));
		
		return rowView;
	}

    private Context context;
    private HashMap<Dish, Integer> dishIdMap;
}
