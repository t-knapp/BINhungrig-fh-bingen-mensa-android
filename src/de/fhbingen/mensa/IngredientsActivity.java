package de.fhbingen.mensa;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.List;

import de.fhbingen.mensa.data.orm.Ingredient;

public class IngredientsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
         * Dynamically add TableRows to table in activity_ingredients
         * @see: http://stackoverflow.com/questions/7916527/android-using-layouts-as-a-template-for-creating-multiple-layout-instances
         */

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        RelativeLayout parent = (RelativeLayout) inflater.inflate(R.layout.activity_ingredients, null);

        TableLayout table = (TableLayout) parent.findViewById(R.id.tl_ingredient);

        final List<Ingredient> ingredientList = Ingredient.findAllOrderedByKey();
        for(final Ingredient i : ingredientList){
            View tableRow = inflater.inflate(R.layout.ingredient_table_row, null);

            TextView tvKey = (TextView) tableRow.findViewById(R.id.tv_ingredient_key);
            tvKey.setText(i.getKey());

            TextView tvValue = (TextView) tableRow.findViewById(R.id.tv_ingredient_value);
            tvValue.setText(i.getDescription());

            table.addView(tableRow);
        }

        setContentView(parent);
    }
}
