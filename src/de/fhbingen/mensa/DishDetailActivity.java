package de.fhbingen.mensa;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;

import com.activeandroid.query.Select;

import org.w3c.dom.Text;

import java.util.Arrays;
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
            vh.numOfRatings = (TextView) findViewById(R.id.textView_numberRatings);

            //Bars
            vh.customBars[0] = (CustomBar) findViewById(R.id.customBar1);
            vh.customBars[1] = (CustomBar) findViewById(R.id.customBar2);
            vh.customBars[2] = (CustomBar) findViewById(R.id.customBar3);
            vh.customBars[3] = (CustomBar) findViewById(R.id.customBar4);
            vh.customBars[4] = (CustomBar) findViewById(R.id.customBar5);
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

        final int[] ratings = dish.getRatings();
        Log.v(TAG, "dish.getRatings(): " + Arrays.toString(ratings));
        this.setRatingBars(ratings);

        this.vh.numOfRatings.setText(String.format("%d Bewertungen", ratings[6]));
    }

    private void setRatingBars(final int[] ratings){
        // Index 5 holds maximum of occurences
        for(int i = 0; i < 5; i++){
            vh.customBars[i].setData(i, ratings[5], ratings[i]);
        }
    }

    private static class ViewHolder {
        TextView dishText, dishPrice, avgRating, numOfRatings;

        CustomBar[] customBars = new CustomBar[5];
    }

}
