package de.fhbingen.mensa.data.orm;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;

/**
 * Created by tknapp on 24.11.15.
 */
public class LocalRating extends Model {

    public static final String COL_DISHID = "dishId";
    @Column(name = COL_DISHID)
    private long dishId;

    public static final String COL_DATE = "date";
    @Column(name = COL_DATE)
    private String date;

    public static final String COL_VALUE = "value";
    @Column(name = COL_VALUE)
    private int value;

    public long getDishId() {
        return dishId;
    }

    public String getDate() {
        return date;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("LocalRating [dishId: %d, date: %s, value: %d]", dishId, date, value);
    }

    public static LocalRating findByDishIdAndDate(final long dishId, final String date){
        return new Select()
                .from(LocalRating.class)
                .where(COL_DISHID + " = ? AND " + COL_DATE + " = ?", dishId, date)
                .executeSingle();
    }

    public static void insertLocalRating(final long dishId, final String date, final int value){
        final LocalRating selected = findByDishIdAndDate(dishId, date);
        if(selected == null){
            final LocalRating newRating = new LocalRating();
            newRating.dishId = dishId;
            newRating.date   = date;
            newRating.value  = value;
            newRating.save();
        }
    }

    public static LocalRating fromRating(final Rating rating){
        final LocalRating result = new LocalRating();
        result.dishId = rating.getDishId();
        result.date   = rating.getDate();
        result.value  = rating.getValue();
        return result;
    }
}
