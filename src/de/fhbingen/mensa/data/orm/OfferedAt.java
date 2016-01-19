package de.fhbingen.mensa.data.orm;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

/**
 * ActiveAndroid ORM Entity
 * for OfferedAt. This is a join-table between Dates and Dishes.
 * In common usecases this entity shall not be defined but is necessary by pull-replication model
 *
 * Created by tknapp on 09.11.15.
 */
@Table(name = "offeredAt")
public class OfferedAt extends Model {

    @Column(name = "fk_dishId")
    private long dishId;

    @Column(name = "fk_dateId")
    private long dateId;

    private static final String COL_SEQ = "seq";
    @Column(name = COL_SEQ)
    private long seq;

    public long getDishId() {
        return dishId;
    }

    public long getDateId() {
        return dateId;
    }

    public long getSeq() {
        return seq;
    }

    //TODO: This is possibly not necessary
    public OfferedAt update(final OfferedAt newer) {
        if(newer.dishId == dishId && newer.dateId == dateId){
            if(newer.seq > seq){
                seq = newer.seq;
            }
        }
        return this;
    }

    @Override
    public String toString() {
        return String.format(
                "OfferedAt [dishId: %d, dateId: %d, seq: %d]",
                dishId, dateId, seq
        );
    }

    //
    // Important for "dynamic" deletion
    //
    public final static String DELETEID = COL_SEQ;

}
