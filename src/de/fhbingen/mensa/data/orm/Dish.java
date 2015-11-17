package de.fhbingen.mensa.data.orm;

import android.database.Cursor;
import android.util.Log;

import com.activeandroid.Cache;
import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;

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
    @Column(name = COL_PRICE_NON_STD)
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


    @Override
    public String toString() {
        return String.format(
                "Dish [id: %d, seq: %d, title: %s, priceStd: %f, priceNonStd: %f, fk_buildingId: %d]",
                dishId, seq, title, priceStd, priceNonStd, buildingId
        );
    }
}
