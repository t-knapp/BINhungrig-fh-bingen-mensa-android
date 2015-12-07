package de.fhbingen.mensa;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.activeandroid.query.Select;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import asynctask.DownloadFullPhotoTask;
import de.fhbingen.mensa.data.orm.Building;
import de.fhbingen.mensa.data.orm.Dish;
import de.fhbingen.mensa.data.orm.Ingredient;
import de.fhbingen.mensa.data.orm.LocalRating;
import de.fhbingen.mensa.data.orm.Photo;
import de.fhbingen.mensa.data.orm.Rating;
import de.fhbingen.mensa.service.UpdateContentService;

public class DishDetailActivity extends Activity implements DownloadFullPhotoTask.IDownloadComplete {

    private static final String TAG = DishDetailActivity.class.getSimpleName();

    private Dish dish;
    private ViewHolder vh;

    // Current date
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);
    final String strDate = sdf.format(Calendar.getInstance().getTime());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_detail);

        // Extract dishId
        final int dishId = getIntent().getExtras().getInt(Dish.COL_DISHID);
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
            vh.ingredients = (TextView) findViewById(R.id.textView_Ingredients);
            vh.dishPrice = (TextView) findViewById(R.id.dish_price);
            vh.avgRating = (TextView) findViewById(R.id.textView_avgRating);
            vh.numOfRatings = (TextView) findViewById(R.id.textView_numberRatings);

            //Bars
            vh.customBars[0] = (CustomBar) findViewById(R.id.customBar1);
            vh.customBars[1] = (CustomBar) findViewById(R.id.customBar2);
            vh.customBars[2] = (CustomBar) findViewById(R.id.customBar3);
            vh.customBars[3] = (CustomBar) findViewById(R.id.customBar4);
            vh.customBars[4] = (CustomBar) findViewById(R.id.customBar5);

            //Ratingsbar
            vh.ratingBar = (RatingBar) findViewById(R.id.ratingBarDish);
            vh.headingRating = (TextView) findViewById(R.id.textView_headingDoRating);
            vh.sendRatingButton = (Button) findViewById(R.id.button_sendRating);

            //Photo
            vh.ivDish = (ImageView) findViewById(R.id.dish_picture);
            vh.ivDownloadPhoto = (ImageView) findViewById(R.id.iv_download_photo);
            vh.pbDownload = (ProgressBar) findViewById(R.id.progressBar1);
        }
    }

    //TODO: Register to EventBus onResume
    //TODO: Unregister from Eventbus onPause

    private void populateView(){
        this.vh.dishText.setText(this.dish.getTitle());
        //TODO: Fix data type chaos in SettingFragment
        final int userRole = Integer.parseInt(
                PreferenceManager.getDefaultSharedPreferences(this)
                        .getString(SettingsFragment.REF_KEY_USERROLE, "0")
        );

        //Ingredients
        final String[] ingredients = Ingredient.loopUpIngredientKeys(dish.getIngredients());
        if(ingredients.length > 0) {
            vh.ingredients.setText("Enhält: " + TextUtils.join(", ", ingredients));
        } else {
            ((ViewGroup) vh.ingredients.getParent()).removeView(vh.ingredients);
        }

        this.vh.dishPrice.setText(
                String.format(
                        Locale.GERMAN, "%.2f €"
                        , (userRole == 0) ? dish.getPriceStd() : dish.getPriceNonStd()
                )
        );

        this.populateRatingView();

        // User Rating Controls
        //TODO: Enable in production
        //this.enableRatingControls(this.isBuildingOpenNow(dish.getBuildingId()));
        this.setOwnRatingBar();

        // TODO: Only if possible
        // Button onClickListener
        this.vh.sendRatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Rating newRating = new Rating();
                newRating.setDishId(dish.getDishId());
                newRating.setValue((int) vh.ratingBar.getRating());
                newRating.setDate(strDate);

                // Post rating on server in async task
                new AsyncTask<Rating, Void, Rating>(){

                    private final String TAG = "AsyncTaskRating";

                    @Override
                    protected Rating doInBackground(Rating... params) {
                        final RestTemplate restTemplate = new RestTemplate();
                        restTemplate.getMessageConverters().add(
                                new MappingJackson2HttpMessageConverter()
                        );
                        return restTemplate.postForObject(
                                UpdateContentService.UrlBuilder.RATINGS, params[0], Rating.class
                        );
                    }

                    @Override
                    protected void onPostExecute(Rating rating) {
                        super.onPostExecute(rating);
                        Log.v(TAG, "postExecute: " + rating.toString());
                        // Save rating to db
                        rating.save();

                        // Save rating to localRatings to prevent multiple requests
                        LocalRating.fromRating(rating).save();

                        // Disable controls and set values
                        setOwnRatingBar();
                        enableRatingControls(false);
                        vh.headingRating.setText(R.string.own_rating);
                        populateRatingView();
                    }

                }.execute(newRating);
            }
        });

        // Photo stuff;
        final DownloadFullPhotoTask.IDownloadComplete callback = this;
        final Photo dbRandomPhoto = Photo.selectRandomByDishId(dish.getDishId());
        if(dbRandomPhoto != null) {
            if (dbRandomPhoto.hasFull()) {
                setDishPhoto(dbRandomPhoto.getFull());
            } else if (dbRandomPhoto.hasThumb()) {
                setDishPhoto(dbRandomPhoto.getThumb());

                // Show download ui element
                vh.ivDownloadPhoto.setVisibility(View.VISIBLE);

                //Setup onClick
                vh.ivDownloadPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        vh.ivDownloadPhoto.setVisibility(View.GONE);
                        vh.pbDownload.setVisibility(View.VISIBLE);

                        //Download Full Photo Async
                        new DownloadFullPhotoTask(callback).execute(dbRandomPhoto);
                    }
                });
            }
        }
    }

    // "Callback" for onDownloadComplete event


    @Override
    public void onDownloadComplete(byte[] bytes) {
        vh.pbDownload.setVisibility(View.GONE);

        setDishPhoto(bytes);
    }

    private void setDishPhoto(final byte[] data){
        vh.ivDish.setImageDrawable(
                new BitmapDrawable(
                        BitmapFactory.decodeByteArray(
                                data
                                , 0
                                , data.length
                        )
                )
        );
    }

    private void setOwnRatingBar(){

        final LocalRating dbRating = LocalRating.findByDishIdAndDate(dish.getDishId(), strDate);
        if(dbRating != null){
            // User rating exists for this dish and date
            vh.headingRating.setText(R.string.own_rating);
            vh.ratingBar.setRating(dbRating.getValue());

            this.enableRatingControls(false);
        }
    }

    private void populateRatingView(){
        this.vh.avgRating.setText(
                String.format(Locale.GERMAN, "%.1f", dish.getAvgRating())
        );

        final int[] ratings = dish.getRatings();
        Log.v(TAG, "dish.getRatings(): " + Arrays.toString(ratings));
        this.setRatingBars(ratings);

        this.vh.numOfRatings.setText(String.format("%d Bewertungen", ratings[6]));
    }

    private void enableRatingControls(final boolean enable){
        vh.ratingBar.setEnabled(enable);
        vh.sendRatingButton.setEnabled(enable);
    }

    private boolean isBuildingOpenNow(final long buildingId) {
        return Building.isOpenNow(buildingId);
    }

    private void setRatingBars(final int[] ratings){
        // Index 5 holds maximum of occurences
        for(int i = 0; i < 5; i++){
            vh.customBars[i].setData(i, ratings[5], ratings[i]);
        }
    }

    private static class ViewHolder {
        TextView dishText, dishPrice, avgRating, numOfRatings, headingRating, ingredients;

        CustomBar[] customBars = new CustomBar[5];

        RatingBar ratingBar;

        Button sendRatingButton;

        ImageView ivDish, ivDownloadPhoto;

        ProgressBar pbDownload;
    }

}
