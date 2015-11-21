package de.fhbingen.mensa.data.orm;

import android.database.Cursor;
import android.util.Log;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.activeandroid.util.SQLiteUtils;

import java.util.Arrays;
import java.util.List;

/**
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

    @Column(name = "dishId", unique = true)
    public int dishId;

    @Column(name = "seq")
    public int seq;

    @Column(name = "title")
    public  String title;

    @Column(name = "ingredients")
    public String ingredients;

    @Column(name = "type")
    public byte type;

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

    public byte getType() {
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

    //TODO: Code-Style
    public static final List<Dish> selectForDateAndBuilding(final java.sql.Date date, final long subscribedBuilding){
        //String buildings = Arrays.toString(subscribedBuildingIds).replace("[", "").replace("]", "").trim();
        //Log.d("DishOld.Select...", buildings);
        final From tmp = new Select().from(Dish.class)
                .join(OfferedAt.class).on("dishId = fk_dishId")
                .join(Date.class).on("fk_dateId = dateId")
                //.join(Building.class).on("buildingId = fk_buildingId")
                .where("date = ? AND fk_buildingId = ?", date, subscribedBuilding);
                //.and("buildingId IN (?)", buildings);
        return tmp.execute();
    }

    //TODO: Code-Style
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


    public static Cursor fetchResultCursor(final java.sql.Date date, final long subscribedBuilding){
        Log.v("fetchResultCursor()", String.format("(%s, %d)", date.toString(), subscribedBuilding));
        return Cache.openDatabase().rawQuery(
                getSqlStatementForDateAndBuilding(date, subscribedBuilding),
                new String[] {date.toString(), Long.toString(subscribedBuilding)}
        );
    }

    public static Dish findByDishId(final int dishId){
        return new Select().from(Dish.class).where("dishId = ?", dishId).executeSingle();
    }

    public float getAvgRating(){
        final String sql = "SELECT AVG(`value`) AS `AVG` FROM `Ratings` WHERE `" + Rating.COL_FK_DISHID + "` = ?";
        final Cursor cursor = Cache.openDatabase().rawQuery(sql, new String[]{Integer.toString(dishId)});
        final float result = cursor.getFloat(cursor.getColumnIndexOrThrow("AVG"));
        cursor.close();
        return result;
    }

    public float getAvgRatingForDate(final String date){
        final String sql = String.format(
                "SELECT AVG(`value`) AS `AVG` FROM `Ratings` WHERE `%s` = ? AND `%s` = ?"
                , Rating.COL_FK_DISHID
                , Rating.COL_DATE
        );
        final Cursor cursor = Cache.openDatabase().rawQuery(sql,
                new String[]{
                        Integer.toString(dishId)
                        , date
                }
        );
        final float result = cursor.getFloat(cursor.getColumnIndexOrThrow("AVG"));
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
}
