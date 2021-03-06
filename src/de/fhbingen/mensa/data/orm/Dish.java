package de.fhbingen.mensa.data.orm;

import android.database.Cursor;
import android.util.Log;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

import java.util.List;

/**
 * ActiveAndroid ORM Entity
 * for Dish
 *
 * Created by tknapp on 07.11.15.
 */

@Table(name = "Dishes")
public class Dish extends Model {

    public Dish(){
        super();
    }

    public Dish(int dishId, int seq, String title, float priceStd, float priceNonStd, long buildingId){
        super();
        this.dishId = dishId;
        this.seq = seq;
        this.title = title;
        this.priceStd = priceStd;
        this.priceNonStd = priceNonStd;
        this.buildingId = buildingId;
    }

    public Dish(final Dish o){
        this(o.dishId, o.seq, o.title, o.priceStd, o.priceNonStd, o.buildingId);
    }

    public final static String COL_DISHID = "dishId";
    @Column(name = COL_DISHID, unique = true)
    public int dishId;

    @Column(name = "seq")
    public int seq;

    @Column(name = "title")
    public  String title;

    @Column(name = "ingredients")
    public String ingredients;

    public static final String COL_TYPE = "type";
    @Column(name = COL_TYPE)
    public int type;

    public static final String COL_PRICE_STD = "priceStd";
    @Column(name = COL_PRICE_STD)
    public float priceStd;

    public static final String COL_PRICE_NON_STD = "priceNonStd";
    @Column(name = COL_PRICE_NON_STD)
    public float priceNonStd;


    //@Column(name = "fk_buildingId")
    //public Building building;

    //Needed by Jackson
    @Column(name = "fk_buildingId")
    public long buildingId;

    public int getDishId() {
        return dishId;
    }

    public String getTitle() {
        return title;
    }

    public String getIngredients() {
        return ingredients;
    }

    public int getSeq() {
        return seq;
    }

    public int getType() {
        return type;
    }

    public float getPriceStd() {
        return priceStd;
    }

    public float getPriceNonStd() {
        return priceNonStd;
    }

    public long getBuildingId() {
        return buildingId;
    }

    //
    // Methods
    //

    public Dish update(final Dish newer){
        if(newer.dishId == this.dishId) {
            if (newer.seq > this.seq) {
                title = newer.title;
                priceStd = newer.priceStd;
                priceNonStd = newer.priceNonStd;
                buildingId = newer.buildingId;
                seq = newer.seq;
            }
        }
        return this;
    }

    /**
     * Builds SQL Statement for fetching dishes for a date and a building
     *
     * @param date
     * @param subscribedBuilding
     * @return
     */
    public static String getSqlStatementForDateAndBuilding(final java.sql.Date date, final long subscribedBuilding){
        //String buildings = Arrays.toString(subscribedBuildingIds).replace("[", "").replace("]", "").trim();
        //Log.d("DishOld.Select...", buildings);
        final String tableName = Cache.getTableInfo(Dish.class).getTableName();
        final From tmp = new Select(tableName + ".*, " + tableName + ".dishId as _id").from(Dish.class)
                .join(OfferedAt.class).on("dishId = fk_dishId")
                .join(Date.class).on("fk_dateId = dateId")
                        //.join(Building.class).on("buildingId = fk_buildingId")
                .where("date = ? AND fk_buildingId = ?", date, subscribedBuilding)
                .orderBy("dishId DESC");
        //.and("buildingId IN (?)", buildings);
        String sql = tmp.toSql();
        Log.v("getSqlStatement...", sql);
        return  sql;
    }


    public static Dish findByDishId(final int dishId){
        return new Select().from(Dish.class).where("dishId = ?", dishId).executeSingle();
    }

    public float getAvgRating(final boolean all, final String strDate){
        return Rating.getAvgRating(dishId, all, strDate);
    }

    /**
     * Returns int array with 7 cells.
     * (index 0-4 for number of stars[index 0 = 1 star])
     * (index 5 holds maximum of votes)
     * (index 6 holds total number of votes)
     * @param all
     * @param strDate
     * @return
     */
    public int[] getRatings(boolean all, final String strDate) {
        final String sql = (all)
                ? (String.format(
                  "SELECT value, COUNT(value) FROM `Ratings` WHERE %s = ? GROUP BY value"
                  , Rating.COL_FK_DISHID))
                : (String.format(
                  "SELECT value, COUNT(value) FROM `Ratings` WHERE %s = ? AND %s = ? GROUP BY value"
                  , Rating.COL_FK_DISHID
                  , Rating.COL_DATE));

        final Cursor cursor = (all)
                ? Cache.openDatabase().rawQuery(sql, new String[]{Integer.toString(dishId)})
                : Cache.openDatabase().rawQuery(sql, new String[]{Integer.toString(dishId), strDate});

        final int[] result = new int[7]; // Index 5 is maximum; Index 6 is number of votes
        int numStars;
        int numVotes;
        int maxNumVotes = 0;
        int sumVotes = 0;
        while (cursor.moveToNext()){
            numStars = cursor.getInt(0); // value 1...5 Stars
            numVotes = cursor.getInt(1); // count of occurences
            result[numStars - 1] = numVotes;
            if(numVotes > maxNumVotes){
                maxNumVotes = numVotes;
            }
            sumVotes += numVotes;
        }
        result[5] = maxNumVotes;
        result[6] = sumVotes;

        cursor.close();
        return result;
    }

    @Override
    public String toString() {
        return String.format(
                "Dish [id: %d, seq: %d, title: %s, priceStd: %f, priceNonStd: %f, fk_buildingId: %d]",
                dishId, seq, title, priceStd, priceNonStd, buildingId
        );
    }

    public final static String ARG_DATE = "arg_date";

    //
    // Important for "dynamic" deletion
    //
    public final static String DELETEID = COL_DISHID;

}
