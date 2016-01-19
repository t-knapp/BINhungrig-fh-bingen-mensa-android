package de.fhbingen.mensa.data.orm;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * ActiveAndroid ORM Entity
 * for Sequence.
 *
 * This is the center of pull replication sync logic.
 *
 * Created by tknapp on 07.11.15.
 */
@Table(name = "_sequence")
public class Sequence extends Model {

    public static final String SEQNAME = "sync";

    @Column(name = "seq_name")
    @JsonIgnore
    public String name = SEQNAME;

    @Column(name = "seq_val")
    public long lastSequence = 0;

    public long getLastSequence() {
        return lastSequence;
    }

    public Sequence update(final Sequence sequence) {
        lastSequence = sequence.lastSequence;
        return this;
    }

    @Override
    public String toString() {
        return String.format(
                "Sequence [seq_name: %s, seq: %d]",
                name, lastSequence
        );
    }

}
