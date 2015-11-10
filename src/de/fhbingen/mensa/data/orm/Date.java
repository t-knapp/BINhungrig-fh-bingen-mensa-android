package de.fhbingen.mensa.data.orm;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * Created by tknapp on 09.11.15.
 */
@Table(name = "Dates")
public class Date extends Model {

    @Column(name = "dateId", unique = true)
    private long dateId;

    @Column(name = "seq")
    private long seq;

    @Column(name = "date")
    private java.sql.Date date;

    //TODO: getDishes: Join Dishes via OfferedAt

    public long getDateId() {
        return dateId;
    }

    public long getSeq() {
        return seq;
    }

    public java.sql.Date getDate() {
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
