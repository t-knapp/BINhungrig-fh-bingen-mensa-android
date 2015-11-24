package de.fhbingen.mensa.data.orm;

import android.database.Cursor;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import java.sql.Date;

/**
 * Created by tknapp on 09.11.15.
 */

@Table(name = "Ratings")
public class Rating extends Model {

    public final static String COL_RATINGID = "ratingId";
    @Column(name = COL_RATINGID, unique = true)
    public int ratingId;

    @Column(name = "seq")
    public long seq;

    public final static String COL_DATE = "date";
    @Column(name = COL_DATE)
    public String date;

    @Column(name = "value")
    public int value;

    public final static String COL_FK_DISHID = "fk_DishId";
    //@Column(name = COL_FK_DISHID)
    //public Dish dish;

    //Needed by Jackson
    @Column(name = COL_FK_DISHID)
    public long dishId;

    public String getDate() {
        return date;
    }

    public int getValue() {
        return value;
    }

    public long getDishId() {
        return dishId;
    }

    public int getRatingId() {
        return ratingId;
    }

    @Override
    public String toString() {
        return String.format(
                "Rating [id: %d, seq: %d, date: %s, value: %d, fk_dishId: %d]",
                ratingId, seq, date, value, dishId
        );
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void setDishId(long dishId) {
        this.dishId = dishId;
    }

    public Rating update(final Rating newRating) {
        if(newRating.ratingId == this.ratingId){
            if(newRating.seq > this.seq){
                this.date   = newRating.date;
                this.value  = newRating.value;
                this.dishId = newRating.dishId;
                this.seq    = newRating.seq;
            }
        }
        return this;
    }

    public static float getAvgRating(final int dishId){
        final String sql = "SELECT AVG(`value`) AS `AVG` FROM `Ratings` WHERE `" + COL_FK_DISHID + "` = ?";
        final Cursor cursor = Cache.openDatabase().rawQuery(sql, new String[]{Integer.toString(dishId)});
        cursor.moveToFirst();
        final float result = cursor.getFloat(cursor.getColumnIndexOrThrow("AVG"));
        cursor.close();
        return result;
    }

}
