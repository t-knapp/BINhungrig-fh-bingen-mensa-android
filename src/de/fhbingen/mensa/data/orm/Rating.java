package de.fhbingen.mensa.data.orm;

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

    public Rating update(final Rating newRating) {
        if(newRating.ratingId == this.ratingId){
            if(newRating.seq > this.seq){
                this.date   = newRating.date;
                this.value  = newRating.value;
                this.dishId = newRating.dishId;
            }
        }
        return this;
    }
}
