package de.fhbingen.mensa;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.fhbingen.mensa.asynctask.DownloadFullPhotoTask;
import de.fhbingen.mensa.data.event.NetworkStatusEvent;
import de.fhbingen.mensa.data.orm.LocalComplains;
import de.fhbingen.mensa.data.orm.Photo;
import de.fhbingen.mensa.service.UpdateContentService;
import de.greenrobot.event.EventBus;

/**
 * Activity showing photo in detail and let user complain about photo.
 */
public class PhotoDetailActivity extends Activity implements DownloadFullPhotoTask.IDownloadComplete {

    private ViewHolder vh;
    private Photo dbPhoto;
    private boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        final long photoId = getIntent().getExtras().getLong(Photo.COL_PHOTOID);
        dbPhoto = Photo.findByPhotoId(photoId);

        isConnected = ((Mensa)getApplication()).isConnected();

        initViewHolder();

        populateView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register EventBus
        if(!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister EventBus
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(NetworkStatusEvent event){
        isConnected = event.isConnected();

        populateView();
    }

    @Override
    public void onDownloadComplete(byte[] bytes) {
        vh.pbDownload.setVisibility(View.GONE);

        setPhotoView(bytes);
    }

    private void setPhotoView(byte[] bytes) {
        vh.ivPhoto.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
    }

    private void populateView() {
        //Photo
        if(dbPhoto != null){
            byte[] bytes;
            if(dbPhoto.hasFull()) {
                bytes = dbPhoto.getFull();
                vh.ivDownloadFull.setEnabled(false);
                vh.ivDownloadFull.setClickable(false);
                vh.ivDownloadFull.setVisibility(View.GONE);
            } else {
                bytes = dbPhoto.getThumb();

                if(isConnected) {
                    vh.ivDownloadFull.setEnabled(true);
                    vh.ivDownloadFull.setClickable(true);
                    vh.ivDownloadFull.setVisibility(View.VISIBLE);

                    final DownloadFullPhotoTask.IDownloadComplete callback = this;
                    vh.ivDownloadFull.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            vh.ivDownloadFull.setEnabled(false);
                            vh.ivDownloadFull.setClickable(false);
                            vh.ivDownloadFull.setVisibility(View.GONE);

                            vh.pbDownload.setVisibility(View.VISIBLE);

                            new DownloadFullPhotoTask(callback).execute(dbPhoto);
                        }
                    });
                } else {
                    vh.ivDownloadFull.setEnabled(false);
                    vh.ivDownloadFull.setClickable(false);
                    vh.ivDownloadFull.setVisibility(View.GONE);
                    vh.ivDownloadFull.setOnClickListener(null);
                }
            }

            setPhotoView(bytes);
        }

        //Complain controls
        final LocalComplains dbComplain = LocalComplains.findByPhotoId(dbPhoto.getPhotoId());
        if(dbComplain != null){
            //Already complained
            vh.tvComplainLabel.setVisibility(View.VISIBLE);
            vh.btnComplain.setVisibility(View.GONE);
            vh.btnComplain.setClickable(false);
            vh.btnComplain.setEnabled(false);
        } else {
            if(isConnected) {
                vh.btnComplain.setClickable(true);
                vh.btnComplain.setEnabled(true);
                vh.btnComplain.setClickable(true);

                //Complains possible
                vh.btnComplain.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AsyncTask<Long, Void, Void>() {

                            @Override
                            protected Void doInBackground(Long... params) {
                                final RestTemplate restTemplate = new RestTemplate();
                                restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
                                restTemplate.postForObject(
                                        UpdateContentService.UrlBuilder.getComplainURL(params[0])
                                        , null
                                        , String.class
                                );
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                final LocalComplains dbComplain = new LocalComplains();
                                dbComplain.setPhotoId(dbPhoto.getPhotoId());

                                final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.GERMAN);
                                final String strDate = sdf.format(Calendar.getInstance().getTime());
                                dbComplain.setDate(strDate);

                                dbComplain.save();

                                Toast.makeText(PhotoDetailActivity.this, R.string.complain_saved, Toast.LENGTH_SHORT).show();

                                vh.btnComplain.setVisibility(View.GONE);
                                vh.btnComplain.setClickable(false);
                                vh.btnComplain.setEnabled(false);
                                vh.tvComplainLabel.setVisibility(View.VISIBLE);
                            }

                        }.execute(dbPhoto.getPhotoId());
                    }
                });
            } else {
                // Disable controls if not connected
                vh.btnComplain.setClickable(false);
                vh.btnComplain.setEnabled(false);
                vh.btnComplain.setOnClickListener(null);
            }
        }
    }

    private void initViewHolder(){
        if(vh == null) {
            vh = new ViewHolder();
        }
        vh.ivPhoto         = (ImageView) findViewById(R.id.iv_photo_detail);
        vh.tvComplainLabel = (TextView) findViewById(R.id.tv_complain_label);
        vh.btnComplain     = (Button) findViewById(R.id.btn_complain);
        vh.ivDownloadFull  = (ImageView) findViewById(R.id.iv_photo_download_full);
        vh.pbDownload      = (ProgressBar) findViewById(R.id.pB_download_full);
    }

    private static class ViewHolder {
        public ImageView ivPhoto, ivDownloadFull;
        public Button btnComplain;
        public TextView tvComplainLabel;
        public ProgressBar pbDownload;
    }

    private final static String TAG = PhotoDetailActivity.class.getSimpleName();
}
