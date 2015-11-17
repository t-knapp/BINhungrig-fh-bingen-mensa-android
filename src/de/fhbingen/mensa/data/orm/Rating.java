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

    @Column(name = "ratingId", unique = true)
    public int ratingId;

    @Column(name = "seq")
    public long seq;

    @Column(name = "date")
    public String date;

    @Column(name = "value")
    public short value;

    @Column(name = "fk_DishId")
    public Dish dish;

    //Needed by Jackson
    public long dishId;

    @Override
    public String toString() {
        return String.format(
                "Rating [id: %d, seq: %d, date: %s, value: %d, priceNonStd: %f, fk_dishId: %d]",
                ratingId, seq, date, value, dishId
        );
    }
}
