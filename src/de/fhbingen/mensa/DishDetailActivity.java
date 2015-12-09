package de.fhbingen.mensa;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.fhbingen.mensa.Exceptions.PictureFileEmptyException;
import de.fhbingen.mensa.asynctask.DownloadFullPhotoTask;
import de.fhbingen.mensa.data.orm.Building;
import de.fhbingen.mensa.data.orm.Dish;
import de.fhbingen.mensa.data.orm.Ingredient;
import de.fhbingen.mensa.data.orm.LocalRating;
import de.fhbingen.mensa.data.orm.Photo;
import de.fhbingen.mensa.data.orm.Rating;
import de.fhbingen.mensa.service.UpdateContentService;

public class DishDetailActivity extends Activity implements DownloadFullPhotoTask.IDownloadComplete {

    private static final String TAG = DishDetailActivity.class.getSimpleName();

    public static final int DISABLED_ALPHA = 50;
    public static final int ENABLED_ALPHA = 255;

    private Dish dish;
    private ViewHolder vh;

    // Current date
    final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);
    final String strDate = sdf.format(Calendar.getInstance().getTime());

    // Given in Extra
    private String dateOfTab;

    private boolean isCurrentDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dish_detail);

        // Extract dishId
        final int dishId = getIntent().getExtras().getInt(Dish.COL_DISHID);
        this.dish = Dish.findByDishId(dishId);

        // Extract date of tab
        dateOfTab = getIntent().getExtras().getString(Dish.ARG_DATE);
        isCurrentDay = dateOfTab.equals(strDate);

        // Init ViewHolder
        this.initViewHolder();

        // Populate View
        this.populateView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dish_detail, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        final MenuItem actionTakePhoto = menu.findItem(R.id.action_take_photo);
        final Drawable actionTakePhotoIcon = actionTakePhoto.getIcon();
        if(isCurrentDay && this.isBuildingOpenNow(dish.getBuildingId())) {
            actionTakePhoto.setEnabled(true);
            actionTakePhoto.getIcon().setAlpha(ENABLED_ALPHA);
        } else {
            actionTakePhoto.setEnabled(false);
            actionTakePhoto.getIcon().setAlpha(DISABLED_ALPHA);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_show_gallery:
                return true;
            case R.id.action_take_photo:
                takeDishPicture();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode) {
            case PICK_FROM_CAMERA:

                try {
                    Log.d(TAG, "uploadedBytes [1] " + uploadedBytes);

                    //Log.d(TAG, "uploadedBytes [2] " + uploadedBytes.length);

                    uploadPicture(cropCenterOfImage());

                    final File f = new File(mImageCaptureUri.getPath());
                    if (f.exists()) {
                        f.delete();
                    }
                } catch (PictureFileEmptyException e) {
                    Toast.makeText(this, R.string.toast_takephoto_failed, Toast.LENGTH_LONG).show();
                }
                break;
        }
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
            vh.ingredients.setText("Enthält: " + TextUtils.join(", ", ingredients));
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

        //Log.v(TAG, "isBuildingOpenNow : " + this.isBuildingOpenNow(dish.getBuildingId()));
        //Log.v(TAG, "isCurrentDay:       " + isCurrentDay);

        // 1. Check if rating for date and dish exists
        if(this.setOwnRatingBar()){
            // Disable Voting Controls and set text
            vh.headingRating.setText(R.string.own_rating);
            vh.sendRatingButton.setVisibility(View.GONE);
            vh.ratingBar.setEnabled(false);
        } else {
            // Check if Rating/Photo possible
            if(isCurrentDay && this.isBuildingOpenNow(dish.getBuildingId())){
                // Button onClickListener
                this.vh.sendRatingButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final Rating newRating = new Rating();
                        newRating.setDishId(dish.getDishId());
                        newRating.setValue((int) vh.ratingBar.getRating());
                        newRating.setDate(dateOfTab);

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
                                vh.headingRating.setText(R.string.own_rating);
                                vh.sendRatingButton.setVisibility(View.GONE);
                                vh.ratingBar.setEnabled(false);

                                // Update whole ratings view
                                populateRatingView();
                            }
                        }.execute(newRating);
                    }
                });
            } else {
                // Rating/Phtot not possible
                vh.headingRating.setText(R.string.no_rating_possible);
                vh.sendRatingButton.setEnabled(false);
                vh.ratingBar.setEnabled(false);
            }
        }

        // Photo stuff;
        final Photo dbRandomPhoto = Photo.selectRandomByDishId(dish.getDishId());
        setPhotoView(dbRandomPhoto);
    }

    private void setPhotoView(final Photo dbPhoto){
        final DownloadFullPhotoTask.IDownloadComplete callback = this;
        if(dbPhoto != null) {
            if (dbPhoto.hasFull()) {
                setDishPhoto(dbPhoto.getFull());
            } else if (dbPhoto.hasThumb()) {
                setDishPhoto(dbPhoto.getThumb());

                // Show download ui element
                vh.ivDownloadPhoto.setVisibility(View.VISIBLE);

                //Setup onClick
                vh.ivDownloadPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        vh.ivDownloadPhoto.setVisibility(View.GONE);
                        vh.pbDownload.setVisibility(View.VISIBLE);

                        //Download Full Photo Async
                        new DownloadFullPhotoTask(callback).execute(dbPhoto);
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

    /**
     * If user has rated this dish today, print this rating.
     * @return true if user has rated dish today, otherwise false
     */
    private boolean setOwnRatingBar(){
        final LocalRating dbRating = LocalRating.findByDishIdAndDate(dish.getDishId(), dateOfTab);
        if(dbRating != null){
            // User rating exists for this dish and date
            vh.headingRating.setText(R.string.own_rating);
            vh.ratingBar.setRating(dbRating.getValue());

            return true;
        }
        return false;
    }

    private void populateRatingView(){
        this.vh.avgRating.setText(
                String.format(Locale.GERMAN, "%.1f", dish.getAvgRating())
        );

        final int[] ratings = dish.getRatings();
        //Log.v(TAG, "dish.getRatings(): " + Arrays.toString(ratings));
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

    /*
     Photo Stuff
     */
    private Uri mImageCaptureUri;

    private static final int PICK_FROM_CAMERA = 1;

    private byte[] uploadedBytes;

    /**
     * Method used to tell this activity to call a camera intent.
     * @return true
     */
    private boolean takeDishPicture() {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        mImageCaptureUri = Uri.fromFile(
                new File(getApplicationContext().getExternalCacheDir(), "mensa_dish.jpg")
        );

        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

        try {
            intent.putExtra("return-data", true);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }

        return true;
    }

    private File cropCenterOfImage() throws PictureFileEmptyException {
        if(mImageCaptureUri != null){
            Log.v(TAG, "Cropping image...");
            final String path = mImageCaptureUri.getPath();
            final Bitmap sourceBitmap   =  BitmapFactory.decodeFile(path);

            final int sourceHeight = sourceBitmap.getHeight();
            final int sourceWidth  = sourceBitmap.getWidth();
            final int sourceMin = Math.min(sourceHeight, sourceWidth);

            int x,y;

            if(sourceMin == sourceHeight){
                x = sourceWidth/2 - sourceMin/2;
                y = 0;
            } else {
                x = 0;
                y = sourceHeight/2 - sourceMin/2;
            }

            final Bitmap centerArea = Bitmap.createBitmap(sourceBitmap, x, y, sourceMin, sourceMin);
            final Bitmap resized    = Bitmap.createScaledBitmap(centerArea, 800, 800, false);

            try {
                final File tmpFile = File.createTempFile("BINhungrig", "_cropped");

                final FileOutputStream fos = new FileOutputStream(tmpFile);
                //final ByteArrayOutputStream stream = new ByteArrayOutputStream();
                resized.compress(Bitmap.CompressFormat.JPEG, 90, fos);

                fos.flush();
                fos.close();

                return tmpFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new PictureFileEmptyException();
        }
        return null;
    }

    private void uploadPicture(final File tmpFile){
        new UploadPictureTask()
                .setDishId(dish.getDishId())
                .execute(tmpFile);
    }

    private class UploadPictureTask extends UploadBinaryTask {

        @Override
        protected void onPostExecute(Photo uploadedPhoto) {
            if(uploadedPhoto != null) {
                Toast.makeText(DishDetailActivity.this, R.string.toast_upload_successful, Toast.LENGTH_LONG).show();

                // Update photo view
                setPhotoView(uploadedPhoto);
            } else {
                Toast.makeText(DishDetailActivity.this, R.string.toast_upload_failed, Toast.LENGTH_LONG).show();
            }
        }

    }

}
