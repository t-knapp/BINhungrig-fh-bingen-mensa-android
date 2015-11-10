package de.fhbingen.mensa.data.orm;

/**
 * Created by tknapp on 09.11.15.
 */
public class Delete {

    public long seq;

    public String tableName;

    public long deleteSeqNumber;

    public long getSeq() {
        return seq;
    }

    public String getTableName() {
        return tableName;
    }

    public long getDeleteSeqNumber() {
        return deleteSeqNumber;
    }
}
