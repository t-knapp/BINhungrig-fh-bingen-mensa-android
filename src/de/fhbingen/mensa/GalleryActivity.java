package de.fhbingen.mensa;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class GalleryActivity extends Activity {

	private Button btn;
	private final Database db = new Database(this);
	private WebView webView;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);
		
		btn = (Button) findViewById(R.id.button_complain);
		
		 webView = (WebView) findViewById(R.id.webView1);
				
		webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
				
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {	
								
				btn.setEnabled(!alreadyComplained(extractId_Pictures(url)));
				
				webView.loadUrl(url);
				return true;
			}
		});
		
		int id_dishes   = getIntent().getExtras().getInt("id_dishes");
		int id_pictures = getIntent().getExtras().getInt("id_pictures");
		
		webView.loadUrl(Mensa.GALLERYURL + "id_dishes="+id_dishes+"&id_pictures="+id_pictures);
		
		btn.setEnabled(!alreadyComplained(id_pictures));
		
		btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				webView.getUrl();
				new InsertComplainActivity().execute(Mensa.APIURL + "insertPictureComplain=" + extractId_Pictures());
			}
			
		});
	}
	
	private int extractId_Pictures(){
		return extractId_Pictures(webView.getUrl());
	}
	
	private int extractId_Pictures(String url){
		int id_pictures = -1;
		
		Pattern p = Pattern.compile(".*&id_pictures=(\\d+)");
		Matcher m = p.matcher(url);
		if (m.find()) {
			id_pictures = Integer.parseInt(m.group(1));
		}
		
		return id_pictures;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.gallery, menu);
		return false;
	}
	
	private boolean alreadyComplained(int id_pictures){
		return db.complainedAboutPicture(id_pictures);
	}

	private class InsertComplainActivity extends ContentTask{
		@Override
		protected void onPostExecute(String result) {
			if(result != null && result.equals("true")){
				db.insertComplain(extractId_Pictures());
				btn.setEnabled(false);
			}
		}
	}
}
