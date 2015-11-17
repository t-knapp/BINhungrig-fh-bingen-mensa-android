package de.fhbingen.mensa;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

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
        ViewHolder vh = new ViewHolder();
        vh.tvTitle  = (TextView) view.findViewById(R.id.textView_dish_name);
        vh.tvRating = (TextView) view.findViewById(R.id.textView_price);

        final String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
        final float price = cursor.getFloat(cursor.getColumnIndexOrThrow("priceStd"));

        vh.tvTitle.setText(title);
        vh.tvRating.setText(String.format("%.2f", price));
    }

    private static class ViewHolder {
        TextView tvTitle;
        TextView tvRating;
    }
}
