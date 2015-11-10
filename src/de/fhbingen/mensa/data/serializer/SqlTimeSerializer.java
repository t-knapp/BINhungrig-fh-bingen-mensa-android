package de.fhbingen.mensa.data.serializer;

import com.activeandroid.serializer.TypeSerializer;

import java.sql.Date;
import java.sql.Time;

/**
 * Created by tknapp on 09.11.15.
 */
public class SqlTimeSerializer extends TypeSerializer {
    @Override
    public Class<?> getDeserializedType() {
        return Time.class;
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
        return ((Time) data).toString();
    }

    @Override
    public Time deserialize(Object data) {
        if(data == null){
            return null;
        }
        //data is string in yyyy-mm-dd format
        return Time.valueOf((String) data);
    }
}
