package de.fhbingen.mensa;

import android.content.Context;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.util.Locale;

import de.fhbingen.mensa.data.orm.Dish;
import de.fhbingen.mensa.data.orm.Rating;

/**
 * Created by tknapp on 10.11.15.
 */
public class DishCursorAdapter extends CursorAdapter {

    public DishCursorAdapter(Context context, Cursor cursor){
        super(context, cursor, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        //return LayoutInflater.from(context).inflate(R.layout.list_item_dish, parent, false);
        return LayoutInflater.from(context).inflate(R.layout.dish_list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Get user role for prices
        final int userRole = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
                .getString(SettingsFragment.REF_KEY_USERROLE, "0"));

        ViewHolder vh = new ViewHolder();
        vh.tvTitle  = (TextView) view.findViewById(R.id.textView_dish_name);
        vh.tvPrice = (TextView) view.findViewById(R.id.textView_price);
        vh.tvRating = (TextView) view.findViewById(R.id.textView_rating);

        final String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        final float price = cursor.getFloat(
                cursor.getColumnIndexOrThrow(
                        (userRole == 0) ? Dish.COL_PRICE_STD : Dish.COL_PRICE_NON_STD
                )
        );

        vh.tvTitle.setText(title);
        vh.tvPrice.setText(String.format("%.2f", price));

        // Ratings
        vh.tvRating.setText(
                String.format(
                        Locale.GERMAN, "★ %.1f"
                        , Rating.getAvgRating(
                                cursor.getInt(cursor.getColumnIndexOrThrow("dishId")))
                )
        );
    }

    private static class ViewHolder {
        TextView tvTitle;
        TextView tvPrice, tvRating;
    }
}
