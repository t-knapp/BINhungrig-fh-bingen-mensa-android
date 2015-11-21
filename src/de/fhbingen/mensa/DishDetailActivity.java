package de.fhbingen.mensa;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.activeandroid.query.Select;

import java.util.List;
import java.util.Locale;

import de.fhbingen.mensa.data.orm.Dish;
import de.fhbingen.mensa.data.orm.Rating;

public class DishDetailActivity extends Activity {

    private static final String TAG = DishDetailActivity.class.getSimpleName();

    private Dish dish;
    private ViewHolder vh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_detail);

        // Extract dishId
        final int dishId = getIntent().getExtras().getInt("dishId");
        this.dish = Dish.findByDishId(dishId);

        // Init ViewHolder
        this.initViewHolder();

        // Populate View
        this.populateView();
    }

    private void initViewHolder(){
        if(this.vh == null){
            this.vh = new ViewHolder();
            vh.dishText = (TextView) findViewById(R.id.dish_text);
            vh.dishPrice = (TextView) findViewById(R.id.dish_price);
            vh.avgRating = (TextView) findViewById(R.id.textView_avgRating);
        }
    }

    private void populateView(){
        this.vh.dishText.setText(this.dish.getTitle());
        //TODO: Fix data type chaos in SettingFragment
        final int userRole = Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getString(SettingsFragment.REF_KEY_USERROLE, "0")
        );

        this.vh.dishPrice.setText(
                String.format(Locale.GERMAN, "%.2f €", (userRole == 0) ? dish.getPriceStd() : dish.getPriceNonStd())
        );

        this.vh.avgRating.setText(
                String.format(Locale.GERMAN, "%.1f", dish.getAvgRating())
        );
    }

    private static class ViewHolder {
        TextView dishText, dishPrice, avgRating;
    }

}
