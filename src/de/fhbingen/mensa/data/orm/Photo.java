package de.fhbingen.mensa.data.orm;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.From;
import com.activeandroid.query.Select;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Created by tknapp on 26.11.15.
 */
@Table(name = "Photos")
public class Photo extends Model {

    public final static String COL_PHOTOID = "photoId";
    @Column(name = COL_PHOTOID)
    private long photoId;

    @Column(name = "seq")
    private long seq;

    public final static String COL_DISHID = "dishId";
    @Column(name = COL_DISHID)
    private long dishId;

    @Column(name = "date")
    private String date;

    @Column(name = "thumb")
    private byte[] thumb;

    @JsonIgnore
    @Column(name = "full")
    private byte[] full;

    public long getPhotoId() {
        return photoId;
    }

    public void setPhotoId(long photoId) {
        this.photoId = photoId;
    }

    public long getSeq() {
        return seq;
    }

    public void setSeq(long seq) {
        this.seq = seq;
    }

    public long getDishId() {
        return dishId;
    }

    public void setDishId(long dishId) {
        this.dishId = dishId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public byte[] getThumb() {
        return thumb;
    }

    public void setThumb(byte[] thumb) {
        this.thumb = thumb;
    }

    public byte[] getFull() {
        return full;
    }

    public void setFull(byte[] full) {
        this.full = full;
    }

    @Override
    public String toString() {
        return String.format(
                "Photo [photoId: %d, dishId: %d, seq: %d, date: %s, thumb: %b, full: %b]"
                , photoId
                , dishId
                , seq
                , date
                , thumb != null
                , full != null
        );
    }

    public Photo update(Photo other) {
        if(other.photoId == photoId){
            if(other.seq > seq){
                photoId = other.photoId;
                seq     = other.seq;
                dishId  = other.dishId;
                thumb   = other.thumb;
                // UseCase: User took and uploaded photo. Full is saved local but seq must not be
                //          updated because other content may have changed. Local seq stays 0
                //          Photo entity is loaded with next changes-Request but with full null
                //          this will overwrite local bytes with null and forces user to reload
                //          full (tl:dr; Skip if server.full is null)
                if(other.full != null) {
                    full = other.full;
                }
                date    = other.date;
            }
        }
        return this;
    }

    public boolean hasThumb(){
        return thumb != null;
    }

    public boolean hasFull(){
        return full != null;
    }

    public static Photo selectRandomByDishId(final long dishId){
        final From from =  new Select()
                .from(Photo.class)
                .where(COL_DISHID + " = ?", dishId)
                .orderBy("RANDOM()")
                .limit(1);
       return from.executeSingle();
    }

    public static Photo findByPhotoId(final long photoId){
        return new Select()
                .from(Photo.class)
                .where(COL_PHOTOID + " = ?", photoId)
                .executeSingle();
    }

    //
    // Important for "dynamic" deletion
    //
    public final static String DELETEID = COL_PHOTOID;
}
