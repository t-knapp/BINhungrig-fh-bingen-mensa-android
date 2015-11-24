package de.fhbingen.mensa.data.orm;

import android.text.TextUtils;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;

import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Created by tknapp on 24.11.15.
 */
public class Ingredient extends Model {

    public static final String COL_KEY = "key";
    @Column(name = COL_KEY)
    private String key;

    public static final String COL_DESCRIPTION = "description";
    @Column(name = COL_DESCRIPTION)
    private String description;

    @Column(name = "seq")
    private long seq;

    public String getKey() {
        return key;
    }

    public String getDescription() {
        return description;
    }

    public long getSeq() {
        return seq;
    }

    public static String[] loopUpIngredientKeys(final String csvKeys){
        final String whereIn = "'" + TextUtils.join("','",StringUtils.commaDelimitedListToStringArray(csvKeys)) + "'";
        final List<Ingredient> ingredientList = new Select()
                .from(Ingredient.class)
                .where(COL_KEY + " IN (" + whereIn + ")")
                .execute();
        final String[] result = new String[ingredientList.size()];
        int iter = 0;
        for(final Ingredient i : ingredientList){
            result[iter] = i.getDescription();
            iter++;
        }
        return result;
    }

    @Override
    public String toString() {
        return String.format(
                "Ingredient [key: %s, description: %s, seq: %d]"
                , key
                , description
                , seq
        );
    }

    public Ingredient update(final Ingredient newIngredient) {
        if(newIngredient.key == this.key){
            if(newIngredient.seq > this.seq){
                this.description = newIngredient.description;
                this.seq         = newIngredient.seq;
            }
        }
        return this;
    }
}
