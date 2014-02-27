package de.fhbingen.mensa;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public class GalleryActivity extends Activity {

	private Button btn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gallery);
		
		btn = (Button) findViewById(R.id.button_complain);
		
		final WebView webView = (WebView) findViewById(R.id.webView1);
				
		webView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
				
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {	
				int id_pictures = -1;
				
				Pattern p = Pattern.compile(".*&id_pictures=(\\d+)");
				Matcher m = p.matcher(url);
				if (m.find()) {
					id_pictures = Integer.parseInt(m.group(1));
				}
				
				btn.setEnabled(!alreadyComplained(id_pictures));
				
				webView.loadUrl(url);
				return true;
			}
		});
		
		int id_dishes   = getIntent().getExtras().getInt("id_dishes");
		int id_pictures = getIntent().getExtras().getInt("id_pictures");
		
		webView.loadUrl(Mensa.GALLERYURL + "id_dishes="+id_dishes+"&id_pictures="+id_pictures);
		
		btn.setEnabled(!alreadyComplained(id_pictures));
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.gallery, menu);
		return false;
	}
	
	private boolean alreadyComplained(int id_pictures){
		//TODO: DB Stuff.
		return false;
	}

}
