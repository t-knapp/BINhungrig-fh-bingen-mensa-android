package de.fhbingen.mensa.data.serializer;

import com.activeandroid.serializer.TypeSerializer;

import java.sql.Date;

/**
 * Created by tknapp on 09.11.15.
 */
public class SqlDateSerializer extends TypeSerializer {
    @Override
    public Class<?> getDeserializedType() {
        return Date.class;
    }

    @Override
    public Class<?> getSerializedType() {
        return String.class;
    }

    @Override
    public Object serialize(Object data) {
        if(data == null) {
            return null;
        }

        // yyyy-mm-dd
        return ((Date) data).toString();
    }

    @Override
    public Date deserialize(Object data) {
        if(data == null){
            return null;
        }
        //data is string in yyyy-mm-dd format
        return Date.valueOf((String) data);
    }
}
