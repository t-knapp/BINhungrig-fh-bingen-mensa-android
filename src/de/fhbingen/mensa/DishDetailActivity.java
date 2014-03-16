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
		imageView = (ImageView) findViewById(R.id.dish_picture);
		dish         = (Dish) getIntent().getExtras().getSerializable("data");
		labelRatings = (TextView) findViewById(R.id.textView_headingDoRating);

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
		
		if(thumbBytes.length > 0){
			
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
            if(!dish.isServedToday()){
                bar.setEnabled(false);
                btn.setEnabled(false);
                labelRatings.setText("Keine Bewertung möglich");
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        int dbRating = db.selectRating(dish.getId_dishes());
		if(dbRating != -1){
			bar.setRating(dbRating);
			btn.setVisibility(View.GONE);
			bar.setIsIndicator(true);
			labelRatings.setText("Eigene Bewertung");
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
				labelRatings.setText("Deine Bewertung");

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
    private static final int CROP_FROM_CAMERA = 2;
	private void doCrop() {
    	Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setType("image/*");

        List<ResolveInfo> list = getPackageManager().queryIntentActivities( intent, 0 );

        int size = list.size();

        if (size == 0) {
        	Toast.makeText(this, "Can not find image crop app", Toast.LENGTH_SHORT).show();

            return;
        } else {
        	intent.setData(mImageCaptureUri);

            intent.putExtra("outputX", 500);
            intent.putExtra("outputY", 500);
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            intent.putExtra("scale", false);
            intent.putExtra("return-data", true);

        	if (size == 1) {
        		Intent i 		= new Intent(intent);
	        	ResolveInfo res	= list.get(0);

	        	i.setComponent( new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

	        	startActivityForResult(i, CROP_FROM_CAMERA);
        	}
        }
	}

	private void toast(){
    	Toast.makeText(this, "Upload erfolgreich", Toast.LENGTH_LONG).show();
    }

	private class UploadPictureTask extends UploadBinaryTask {

        @Override
    	protected void onPostExecute(String result) {
    		toast();
    	}
    }
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode != RESULT_OK) return;

	    switch (requestCode) {
		    case PICK_FROM_CAMERA:
		    	doCrop();

		    	break;
		    case CROP_FROM_CAMERA:
		        Bundle extras = data.getExtras();

		        if (extras != null) {
		            Bitmap photo = extras.getParcelable("data");

		            ByteArrayOutputStream stream = new ByteArrayOutputStream();
		            photo.compress(Bitmap.CompressFormat.JPEG, 85, stream);
		            byte[] byteArray = stream.toByteArray();

		            //TODO: Save Picture in mensas collection
		            // Only set imageView if no picture is set before.
		            imageView.setImageBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));

		            byte[] encodedBytes = Base64.encode(byteArray, Base64.DEFAULT);

		            try {
		            	String dataString = new String(encodedBytes, "UTF-8").replaceAll("\\n", "");
		            	String queryString = Mensa.APIURL + "insertDishPhoto="+dish.getId_dishes()+"&data=";

		            	new UploadPictureTask().execute(
							queryString,
							dataString
							);
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

		            //Log.d("IPA", "encodedBytes.lenght: " + encodedBytes.length);

		            //mImageView.setImageBitmap(BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length));

		            //mImageView.setImageBitmap(photo);
		        }

		        File f = new File(mImageCaptureUri.getPath());

		        if (f.exists()) f.delete();

		        break;

	    }
	}

    /**
     * Method used to tell this activity to call a camera intent.
     * @return true
     */
    private boolean takeDishPicture() {
        //Intent i = new Intent(v.getContext(), ImagePickActivity.class);
        //startActivity(i);

        Intent intent 	 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        mImageCaptureUri = Uri.fromFile(new File(Environment
                .getExternalStorageDirectory(),
                "tmp_avatar_" + String.valueOf(System.currentTimeMillis()) + ".jpg"));

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
        Log.d("DDA", "startWebView");

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
            actionTakePhoto.setEnabled(dishServedToday);
            actionShowGallery.setEnabled(dishServedToday);
            if (!dishServedToday) {
                final Drawable cameraIcon = actionTakePhoto.getIcon();
                cameraIcon.setAlpha(DISABLED_ALPHA);

                final Drawable galleryIcon = actionShowGallery.getIcon();
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
