package de.fhbingen.mensa;


import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import com.actionbarsherlock.app.SherlockActivity;

public class AboutActivity extends SherlockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setHomeButtonEnabled(true);
        TextView credits = (TextView) findViewById(R.id.textView_appCredits);
        credits.setMovementMethod(LinkMovementMethod.getInstance());
    }
}