package de.fhbingen.mensa;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockActivity;

/**
 * Created by tknapp on 13.04.14.
 */
public class IngredientsActivity extends SherlockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ingredients);
        getSupportActionBar().setHomeButtonEnabled(true);
    }
}
