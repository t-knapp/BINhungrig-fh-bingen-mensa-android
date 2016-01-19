package de.fhbingen.mensa;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.AsyncTask;
import android.util.Log;

import com.activeandroid.util.IOUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;
import org.springframework.web.client.RestTemplate;

import de.fhbingen.mensa.data.orm.Photo;
import de.fhbingen.mensa.service.UpdateContentService;

/**
 * AsyncTast for uploading taken pictures
 */
 public class UploadBinaryTask extends AsyncTask<File, Void, Photo> {

	private long dishId;

	protected UploadBinaryTask setDishId(final long dishId){
        this.dishId = dishId;
        return this;
    }

	@Override
	protected Photo doInBackground(File... args) {
        StringBuilder builder = new StringBuilder();
		HttpClient httpClient = new DefaultHttpClient();

        //<issue 11> https://github.com/t-knapp/BINhungrig-fh-bingen-mensa-android/issues/11
        //Set User Agent for server-sided stats
        httpClient.getParams().setParameter(HttpProtocolParams.USER_AGENT, Mensa.getUserAgentString());
        //</issue 11>

        final HttpPost httpPost = new HttpPost(UpdateContentService.UrlBuilder.PHOTOS);
        final MultipartEntityBuilder multipartEntityBuilder = MultipartEntityBuilder.create();
        multipartEntityBuilder.addBinaryBody("file", args[0]);
        multipartEntityBuilder.addTextBody("dishId", Long.toString(dishId));

        final HttpEntity httpEntity = multipartEntityBuilder.build();

        httpPost.setEntity(httpEntity);

        HttpResponse response;
        HttpEntity resEntity = null;
        Photo photo = null;
        try {
            response = httpClient.execute(httpPost);
            resEntity = response.getEntity();
            //System.out.println(response.getStatusLine());
            if (resEntity != null) {
                //System.out.println(EntityUtils.toString(resEntity));
                if(response.getStatusLine().getStatusCode() == 201) {
                    final ObjectMapper mapper = new ObjectMapper();
                    photo = mapper.readValue(EntityUtils.toString(resEntity), Photo.class);


                    //Save to db
                    photo.setFull(getBytesFullFromFile(args[0]));
                    photo.save();
                }
            }
            if (resEntity != null) {
                resEntity.consumeContent();
            }
            //Delete file
            if(args[0].exists()) {
                args[0].delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        httpClient.getConnectionManager().shutdown();

        return photo;
	}

     private byte[] getBytesFullFromFile(final File tmpFile){
         final byte[] buffer = new byte[(int)tmpFile.length()];
         try {
             BufferedInputStream bis = new BufferedInputStream(new FileInputStream(tmpFile));
             bis.read(buffer, 0, buffer.length);

             bis.close();
         } catch (IOException e) {
             e.printStackTrace();
         }

         return buffer;
     }

}
