package de.fhbingen.mensa;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import android.graphics.drawable.Drawable;
import android.support.v4.app.NavUtils;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Menu;
import de.fhbingen.mensa.Exceptions.PictureFileEmptyException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.Toast;
import android.widget.RatingBar.OnRatingBarChangeListener;
import android.widget.TextView;
import android.view.View.OnClickListener;

public class DishDetailActivity extends SherlockActivity {

    public static final int DISABLED_ALPHA = 50;
    public static final int ENABLED_ALPHA = 255;
    private Mensa mensa;
	private Dish dish;
	private ImageView imageView;
	private TextView tv;
	private RatingBar bar;
	private Button btn;
	private TextView labelRatings;
	private int[] ratings = new int[5];
		
	private final Database db = new Database(this);
    private final DishDetailActivity instance = this;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_dish_detail);
        getSupportActionBar().setHomeButtonEnabled(true);

		mensa        = (Mensa) this.getApplication();
		imageView    = (ImageView) findViewById(R.id.dish_picture);
		dish         = (Dish) getIntent().getExtras().getSerializable("data");
		labelRatings = (TextView) findViewById(R.id.textView_headingDoRating);

        Log.d("mensa.Mensa", dish.toString());

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
		setAvgRating(dish.getAvgRating());
		
		//Load rating in AsyncTask
		new LoadRatingsActivity()
			.execute(Mensa.APIURL + "getRatings=" + dish.getId_dishes());
				
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
		//final ImageView imageView = (ImageView) findViewById(R.id.dish_picture);
		
		byte[] thumbBytes = dish.getThumb();
		
		//if(thumbBytes.length > 0){
        if(dish.getId_pictures() != -1){
			
			byte[] pictureBytes = dish.getPicture();
			if(pictureBytes != null){
				//Show large image
				setPicture(pictureBytes);
			} else {
				// Show thumbnail
				byte[] decodedString = Base64.decode(thumbBytes, Base64.DEFAULT);
				
				setPicture(decodedString);

                final LoadPictureActivity loadPictureActivity =
                    new LoadPictureActivity(DishDetailActivity.this);

                if (SettingsHelper.isAutoDownloadLargePicturesEnabled()) {
                    loadPictureActivity.execute(
                            Mensa.APIURL + "getDishPhotoData=" + dish.getId_pictures()
                    );
                } else {
                    imageView.setClickable(true);
                    imageView.setFocusable(true);
                    imageView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            loadPictureActivity.execute(Mensa.APIURL + "getDishPhotoData=" + dish.getId_pictures());
                            imageView.setClickable(false);
                            imageView.setFocusable(false);
                        }
                    });
                }
			}
		}
				
		//Rating
		bar = (RatingBar) findViewById(R.id.ratingBarDish);
		btn = (Button) findViewById(R.id.button_complain);

        try {
            if(!dish.isServedToday() || mensa.isStillClosed() || mensa.isAlreadyClosed()){
                bar.setEnabled(false);
                btn.setEnabled(false);
                labelRatings.setText(R.string.no_rating_possible);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int dbRating = db.selectRating(dish.getId_dishes());
		if(dbRating != -1){
			bar.setRating(dbRating);
			btn.setVisibility(View.GONE);
			bar.setIsIndicator(true);
			labelRatings.setText(R.string.own_rating);
		} else {
		
			bar.setOnRatingBarChangeListener(new OnRatingBarChangeListener() {
				
				@Override
				public void onRatingChanged(RatingBar ratingBar, float rating,
						boolean fromUser) {
					ratingBar.setRating(rating>0?rating:1);				
				}
			});
			
			
			btn.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					new InsertRatingActivity()
						.execute(Mensa.APIURL
								+ "insertDishRating=" 
								+ dish.getId_dishes() 
								+ ";" + (int)bar.getRating());
					
					btn.setVisibility(View.GONE);
					bar.setIsIndicator(true);
				}
			});
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.dish_detail, menu);
		return true;
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_take_photo:
                takeDishPicture();
                break;
            case R.id.action_show_gallery:
                showGallery();
                break;
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setPicture(byte[] pictureBytes){
		if(pictureBytes != null){
			if(pictureBytes.length > 0){
				imageView.setImageBitmap(BitmapFactory.decodeByteArray(pictureBytes, 0, pictureBytes.length));
			}
		}
	}

	public void setRating(int[] ratings){
		CustomBar cb;
		int max = 0;
		int numberRatings = 0;

		//Calculate count and max
		for (int i : ratings) {
			max = i > max ? i : max;
			numberRatings += i;
		}

		//Draw rating
		for (int i = 0; i < ratings.length; i++){
			cb = (CustomBar) findViewById(CUSTOMBARS[i]);
			cb.setData(i, max, ratings[i]);
		}

		//Set number of rating
		tv = (TextView) findViewById(R.id.textView_numberRatings);
		tv.setText(String.format("%d Bewertungen", numberRatings));
		tv.setVisibility(View.VISIBLE);
	}

	public double setAvgRating(){
		int cnt = 0;
		double sum = 0;
		for(int i = 0; i < ratings.length; i++){
			sum += (i+1) * ratings[i];
			cnt += ratings[i];
		}
		setAvgRating(sum/cnt);
		return sum/cnt;
	}

	public void setAvgRating(double avg){
		tv = (TextView) findViewById(R.id.textView_avgRating);
		tv.setText(String.format(Locale.GERMAN, "%.1f", avg));
	}

	private final int[] CUSTOMBARS = {R.id.customBar1, R.id.customBar2, R.id.customBar3, R.id.customBar4, R.id.customBar5};

	private class LoadPictureActivity extends ContentTask{

		public LoadPictureActivity(Activity act) {
			//c = act;
		}

		//private Context c;
		//private ProgressDialog dialog;
		private ProgressBar pg = (ProgressBar) findViewById(R.id.progressBar1);

		@Override
		protected void onPreExecute() {
			//dialog = new ProgressDialog(c);
			//dialog.setMessage("Lade Foto ...");
			//dialog.setCancelable(false);
			//dialog.show();
			pg.setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(String result) {

			JSONObject jsonObj;
			String data = null;
			try {
				jsonObj = new JSONObject(result);
				data = jsonObj.getString("pictureData");
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if(data != null){
				byte[] decodedString = Base64.decode(data.getBytes(), Base64.DEFAULT);
				mensa.setDishPicture(dish.getDate(), dish.getId_dishes(), decodedString);
				setPicture(decodedString);
			}
			//dialog.dismiss();
			pg.setVisibility(View.GONE);
		}

    }
	private class LoadRatingsActivity extends ContentTask{

		@Override
		protected void onPostExecute(String result) {
			JSONArray jsonArray;

			try {
				jsonArray = new JSONArray(result);
				for(int i = 0; i < jsonArray.length(); i++){
					ratings[i] = jsonArray.getInt(i);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			setRating(ratings);
		}

    }
	private class InsertRatingActivity extends ContentTask{

        @Override
		protected void onPostExecute(String result) {
			if(result != null && result.equals("true")){
				btn.setVisibility(View.GONE);
				bar.setIsIndicator(true);
				labelRatings.setText(R.string.own_rating);

				//Store rating and id in SQLite
				db.insertRating(dish.getId_dishes(), (int)bar.getRating());

				//Update rating
				ratings[(int)bar.getRating()-1]++;
				setRating(ratings);
				double avg = setAvgRating();

				//Update dish in mensas collection
				mensa.setAvgRating(dish.getDate(), dish.getId_dishes(), avg);
			} else {
				btn.setVisibility(View.GONE);
				bar.setIsIndicator(true);
			}
		}
    }
	/*
	______ _      _                         _____ _          __  __
	| ___ (_)    | |                       /  ___| |        / _|/ _|
	| |_/ /_  ___| |_ _   _ _ __ ___ ______\ `--.| |_ _   _| |_| |_
	|  __/| |/ __| __| | | | '__/ _ \______|`--. \ __| | | |  _|  _|
	| |   | | (__| |_| |_| | | |  __/      /\__/ / |_| |_| | | | |
	\_|   |_|\___|\__|\__,_|_|  \___|      \____/ \__|\__,_|_| |_|
	 */


	private Uri mImageCaptureUri;

    private static final int PICK_FROM_CAMERA = 1;

    private byte[] uploadedBytes;

    private byte[] cropCenterOfImage() throws PictureFileEmptyException {
        if(mImageCaptureUri != null){
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
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resized.compress(Bitmap.CompressFormat.JPEG, 85, stream);

            return stream.toByteArray();
        } else {
            throw new PictureFileEmptyException();
        }
    }

    private void uploadPicture(byte[] byteArray){

        byte[] encodedBytes = Base64.encode(byteArray, Base64.DEFAULT);

        try {
            final String dataString = new String(encodedBytes, "UTF-8").replaceAll("\\n", "");
            final String queryString = Mensa.APIURL + "insertDishPhoto="+dish.getId_dishes()+"&data=";

            new UploadPictureTask().execute(
                    queryString,
                    dataString
            );
        } catch (UnsupportedEncodingException e) {
            Toast.makeText(this, R.string.toast_upload_failed, Toast.LENGTH_LONG).show();
        }
    }

	private class UploadPictureTask extends UploadBinaryTask {

        @Override
    	protected void onPostExecute(String result) {
            Log.d("mensa.Mensa", "Result : " + result);

            Toast.makeText(DishDetailActivity.this, R.string.toast_upload_successful, Toast.LENGTH_LONG).show();

            final int newPicId = Integer.parseInt(result);

            //Notify Mensa and UI
            if(dish.getId_pictures() == -1){
                mensa.setDishPicture(dish.getDate(), dish.getId_dishes(), uploadedBytes, newPicId);
                imageView.setImageBitmap(BitmapFactory.decodeByteArray(uploadedBytes, 0, uploadedBytes.length));
            }
    	}

    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode != RESULT_OK) return;

	    switch (requestCode) {
		    case PICK_FROM_CAMERA:

                try {
                    uploadedBytes = cropCenterOfImage();

                    uploadPicture(uploadedBytes);

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

    /**
     * Method used to tell this activity to call a camera intent.
     * @return true
     */
    private boolean takeDishPicture() {
        final Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        mImageCaptureUri = Uri.fromFile(new File(getApplicationContext().getExternalCacheDir(),
                "mensa_dish.jpg"));

        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);

        try {
            intent.putExtra("return-data", true);
            startActivityForResult(intent, PICK_FROM_CAMERA);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Will show the gallery view for the current dish.
     */
    private void showGallery() {
        Intent webView = new Intent(this, GalleryActivity.class);
        webView.putExtra("id_dishes", dish.getId_dishes());
        webView.putExtra("id_pictures", dish.getId_pictures());
        startActivity(webView);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onResume();
        final MenuItem actionTakePhoto   = menu.findItem(R.id.action_take_photo);
        final MenuItem actionShowGallery = menu.findItem(R.id.action_show_gallery);
        try {
            final boolean dishServedToday = dish.isServedToday();
            final boolean alreadyClosed   = Mensa.isAlreadyClosed(); /* is it too late? */
            final boolean stillClosed     = Mensa.isStillClosed();   /* or is it too soon? */
            final boolean hasCamera = SettingsHelper.hasCamera();

            final boolean isActionPhotoEnabled = dishServedToday
                                        && !alreadyClosed
                                        && !stillClosed
                                        && hasCamera;
            final boolean isActionGalleryEnabled = dish.hasPicture();

            Log.d("CAMERA", String.format("dishServedToday: %b, alreadyClosed: %b, stillClosed: %b, hasCamera: %b, " +
                    "isActionPhotoEnabled: %b", dishServedToday, alreadyClosed, stillClosed, hasCamera,
                    isActionPhotoEnabled));

            actionTakePhoto.setEnabled(isActionPhotoEnabled);
            actionShowGallery.setEnabled(isActionGalleryEnabled);

            final Drawable cameraIcon = actionTakePhoto.getIcon();
            final Drawable galleryIcon = actionShowGallery.getIcon();

            actionTakePhoto.setVisible(hasCamera);
            
            if (isActionPhotoEnabled) {
                cameraIcon.setAlpha(ENABLED_ALPHA);
            } else {
                cameraIcon.setAlpha(DISABLED_ALPHA);
            }

            if (isActionGalleryEnabled) {
                galleryIcon.setAlpha(ENABLED_ALPHA);
            } else {
                galleryIcon.setAlpha(DISABLED_ALPHA);
            }

        } catch (ParseException e) {
            e.printStackTrace();
            actionTakePhoto.setEnabled(true);
            actionShowGallery.setEnabled(true);
        }
        return true;
    }
}
