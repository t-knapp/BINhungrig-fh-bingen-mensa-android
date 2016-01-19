package de.fhbingen.mensa.data.orm;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * ActiveAndroid ORM Entity
 * for Dates in yyyy-mm-dd format
 *
 * String is used for storage b.c. some problems with jackson de-/serialize of java.sql.date
 * @see: http://wiki.fasterxml.com/JacksonFAQDateHandling
 *
 * Created by tknapp on 09.11.15.
 */
@Table(name = "Dates")
public class Date extends Model {

    public static final String COL_DATEID = "dateId";
    @Column(name = COL_DATEID, unique = true)
    private long dateId;

    @Column(name = "seq")
    private long seq;

    @Column(name = "date")
    private String date;

    public long getDateId() {
        return dateId;
    }

    public long getSeq() {
        return seq;
    }

    public String getDate() {
        return date;
    }

    public Date update(final Date newer){
        if(newer.dateId == dateId){
            if(newer.seq > seq){
                date = newer.date;
                seq = newer.seq;
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return String.format(
                "Date [dateId: %d, seq: %d, date: %s]",
                dateId, seq, date
        );
    }

}
