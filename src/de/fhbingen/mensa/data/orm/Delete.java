package de.fhbingen.mensa.data.orm;

/**
 * Object for holding Delete Action. No entity.
 *
 * Created by tknapp on 09.11.15.
 */
public class Delete {

    public long seq;

    public String tableName;

    public long deleteId;

    public long getSeq() {
        return seq;
    }

    public String getTableName() {
        return tableName;
    }

    public long getDeleteId() {
        return deleteId;
    }
}
