package de.fhbingen.mensa;


import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView credits = (TextView) findViewById(R.id.textView_appCredits);
        credits.setMovementMethod(LinkMovementMethod.getInstance());
    }
}