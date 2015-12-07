package asynctask;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import de.fhbingen.mensa.data.orm.Photo;
import de.fhbingen.mensa.service.UpdateContentService;

/**
 * Created by tknapp on 07.12.15.
 */
public class DownloadFullPhotoTask extends AsyncTask<Photo, Void, Photo> {

    public interface IDownloadComplete {
        public void onDownloadComplete(final byte[] bytes);
    }

    public DownloadFullPhotoTask(IDownloadComplete callback){
        callbackInterface = callback;
    }

    @Override
    protected Photo doInBackground(Photo... dbPhotos) {
        Photo dbPhoto = dbPhotos[0];

        try {
            final URLConnection urlConnection = new URL(
                    UpdateContentService.UrlBuilder.getPhotoURL(dbPhoto.getPhotoId())
            ).openConnection();

            Log.v(TAG, "content-length: " + urlConnection.getContentLength());

            InputStream is = urlConnection.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);

            //FileOutputStream fos = new FileOutputStream(file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BufferedOutputStream bos = new BufferedOutputStream(baos);

            int nRead;
            byte[] data = new byte[16384];
            while ((nRead = bis.read(data, 0, data.length)) != -1) {
                bos.write(data, 0, nRead);
            }
            bis.close();
            bos.flush();

            // Save to DB
            dbPhoto.setFull(baos.toByteArray());
            dbPhoto.save();

            bos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return dbPhoto;
    }

    @Override
    protected void onPostExecute(Photo photo) {
        super.onPostExecute(photo);

        callbackInterface.onDownloadComplete(photo.getFull());
    }

    private static final String TAG = DownloadFullPhotoTask.class.getSimpleName();

    private IDownloadComplete callbackInterface;

}
