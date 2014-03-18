package de.fhbingen.mensa;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

import android.content.ClipData;
import android.content.Context;
import android.database.Cursor;
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
import android.support.v4.content.CursorLoader;
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

    private Uri getmImageCaptureUri(){
        if (mImageCaptureUri == null){
            Log.i(TAG, "ExternalStorage is writable [getImageCaptureUri]: " + writableExternalStorage());
            if (writableExternalStorage()) {
                // the default path for saving pictures for the active user
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                if (!path.exists()){
                    path.mkdir();
                }

                File file = new File(
                        path,
                        "mensa_tmp_image_" + String.valueOf(System.currentTimeMillis()) + ".jpg"
                );

                mImageCaptureUri = Uri.fromFile(file);
            }
            else{
                Log.e(TAG, "External Storage is not writeable!");
            }

        }
        return mImageCaptureUri;
    }

    private Uri getmOutPutUri(){
        if (mOutPutUri == null){
            Log.i(TAG, "ExternalStorage is writable [getmOutPutUri]: " + writableExternalStorage());
            if (writableExternalStorage()) {
                // the default path for saving pictures for the active user
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                if (!path.exists()){
                   path.mkdir();
                }

                File file = new File(
                        path,
                        "mensa_output_image_" + String.valueOf(System.currentTimeMillis()) + ".jpg"
                );

                mOutPutUri = Uri.fromFile(file);
            }
            else{
                Log.e(TAG, "External Storage is not writeable!");
            }
        }
        return mOutPutUri;
    }

    private Uri mImageCaptureUri;
    private Uri mOutPutUri;


    private static final int PICK_FROM_CAMERA = 1;
    private static final int CROP_FROM_CAMERA = 2;

    private void doCrop(Uri inputUri, Uri outputUri) {
        try{
            Log.d(TAG, "In doCrop");

            final Intent cropIntent = getCropImageIntent(inputUri, outputUri);
            Log.d(TAG, "Starting the Intent for Cropping " + cropIntent.toString());
            startActivityForResult(cropIntent, CROP_FROM_CAMERA);


        	}
        catch(Exception e){
            Log.e(TAG, "Cannot crop image");
            Toast.makeText(this, R.string.noCrop, Toast.LENGTH_LONG);
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
    /**
     * @param requestCode Code of the request
     * @param resultCode Code of the result
     * @param data the returned intent
     */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "requestCode " + requestCode + " resultCode: " + resultCode );
	    if (resultCode != RESULT_OK) return;

	    switch (requestCode) {
            // Photo from the camera -> must be croped
		    case PICK_FROM_CAMERA:
                // Uri for cropping
                final Uri uri;
                // Uri is set in the intent
                if (data != null && data.getData() != null){
                    uri = data.getData();
                }
                else{
                   uri = getmImageCaptureUri();
                }
                Log.d(TAG, "in Pick_FROM_CAMERA" + uri.toString());

                doCrop(uri, getmImageCaptureUri());

		    	break;
            // Cropped photo -> must be saved
		    case CROP_FROM_CAMERA:
                Log.d(TAG, "in crop_from_camera onActivityResult");

                try{
                    /*File file = new File(getmImageCaptureUri().getPath());
                    if (file == null){
                        file = new File(getmImageCaptureUri().toString());
                    }
                    Log.d(TAG, "file to URI" + file.toURI() + " getmImageCaptureUri " + getmImageCaptureUri() + " equals ? " + (file.toURI().equals(getmImageCaptureUri())));
                    Bitmap bitmap = BitmapFactory.decodeFile(
                            file.getAbsolutePath()
                    );
                    if (bitmap == null){
                        bitmap = BitmapFactory.decodeFile(
                                getmImageCaptureUri().getPath()
                        );
                    }
                    if (bitmap == null){
                        FileInputStream fis = null;
                        try{
                            fis = new FileInputStream(file);
                        }
                        catch (FileNotFoundException ex){
                            Log.e(TAG, "FileNotFountException for getmImageCaptureUri");
                        }
                        bitmap = BitmapFactory.decodeStream(fis);
                    } */

                    Bitmap bitmap = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(getmImageCaptureUri())
                    );
                    Log.i(TAG, "Bitmap: " + bitmap.getWidth() + bitmap.getHeight());



                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream);
                    byte[]  byteArray = outStream.toByteArray();

                    //TODO: Save Picture in mensas collection
                    // Only set iv if no picture is set before.


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
                        Log.e(TAG, e.toString());
                    }

                    imageView.setImageBitmap(bitmap);
                    imageView.setImageURI(getmImageCaptureUri());
                    imageView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showGallery(); //TODO does view need to be transmitted?
                        }
                    });
                }
                catch(Exception e){
                    Log.e(TAG, e.toString() + "\n" + e.getMessage());
                }
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


            // imageCapture needed because its the input for cropping
          intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, getmImageCaptureUri());

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

    /**
     * Adding the extras to the intent which should crop the image
     * outputX and outputY look at @param photoSize.
     * aspectx and aspectY is set to 1.
     * Image should not scaled, but cropped.
     *
     * @param photoSize the "endsize" of the image -> its a sqaure image
     * @param cropIntent the intent on which the extras should be set
     */
    private void addCropExtras(Intent cropIntent, int photoSize){
        cropIntent.putExtra("outputX", photoSize);
        cropIntent.putExtra("outputY", photoSize);
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("scale", false);
        cropIntent.putExtra("return-data", false);
        cropIntent.putExtra("crop", true);
        cropIntent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
    }

    private void addPhotoPickerExtras(Intent cropIntent, Uri photoUri){
        cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
        //cropIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        //cropIntent.setClipData(ClipData.newRawUri(MediaStore.EXTRA_OUTPUT, photoUri));
    }

    /**
     * Creates an Intent for cropping an Image
     * @param inputUri - represents the data with which the Intent should do it's magic
     * @param outputUri - represents the path on which the Output should be saved
     * @return the intent for cropping an Image
     */
    private Intent getCropImageIntent(Uri inputUri, Uri outputUri){
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(inputUri, "image/*");
        addPhotoPickerExtras(intent, outputUri);
        addCropExtras(intent, 500);
        return intent;
    }

    /**
     *
     * @return true if the externalStorage is writable
     */
    private boolean writableExternalStorage(){
        String state = Environment.getExternalStorageState();
        // only if it's true the external storage is writeable!
        if (Environment.MEDIA_MOUNTED.equals(state)){
            return true;
        }
        return false;
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onResume();
        final MenuItem actionTakePhoto = menu.findItem(R.id.action_take_photo);
        final MenuItem actionShowGallery = menu.findItem(R.id.action_show_gallery);
        try {
            final boolean dishServedToday = dish.isServedToday();
            actionTakePhoto.setEnabled(dishServedToday);
            actionShowGallery.setEnabled(dishServedToday);
            final Drawable cameraIcon = actionTakePhoto.getIcon();
            final Drawable galleryIcon = actionShowGallery.getIcon();
            if (!dishServedToday) {
                cameraIcon.setAlpha(DISABLED_ALPHA);
                galleryIcon.setAlpha(DISABLED_ALPHA);
            } else {
                cameraIcon.setAlpha(ENABLED_ALPHA);
                galleryIcon.setAlpha(ENABLED_ALPHA);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            actionTakePhoto.setEnabled(true);
            actionShowGallery.setEnabled(true);
        }
        return true;
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };

        //This method was deprecated in API level 11
        //Cursor cursor = managedQuery(contentUri, proj, null, null, null);

        CursorLoader cursorLoader = new CursorLoader(
                this,
                contentUri, proj, null, null, null);
        Cursor cursor = cursorLoader.loadInBackground();

        int column_index =
                cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    private final String TAG = "DishDetailActivity";
}
