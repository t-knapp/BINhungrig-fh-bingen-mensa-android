package de.fhbingen.mensa.data.orm;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.query.Select;

/**
 * ActiveAndroid ORM Entity
 * for LocalComplains. Complains are send to server, this entity is necessary to prevent
 * multiple complains on one device for same picture.
 *
 * Created by tknapp on 14.12.15.
 */
public class LocalComplains extends Model {

    public final static String COL_PHOTOID = "photoId";
    @Column(name = COL_PHOTOID)
    private long photoId;

    public final static String COL_COMPLAINDATE = "date";
    @Column(name = COL_COMPLAINDATE)
    private String date;

    public void setDate(String date) {
        this.date = date;
    }

    public void setPhotoId(long photoId) {
        this.photoId = photoId;
    }

    public long getPhotoId() {
        return photoId;
    }

    public String getDate() {
        return date;
    }

    public static LocalComplains findByPhotoId(final long photoId){
        return new Select()
                .from(LocalComplains.class)
                .where(COL_PHOTOID + " = ?", photoId)
                .executeSingle();
    }

    @Override
    public String toString() {
        return String.format("LocalComplains: [photoId: %d, date: %s]", photoId, date);
    }
}
