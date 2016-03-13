package ca.pjer.sqlper.support.mapper;

import java.sql.Timestamp;
import java.util.Date;

public class DateConverterMapper extends ConverterMapper<java.util.Date, java.sql.Timestamp> {

    public DateConverterMapper() {
        super(java.sql.Timestamp.class);
    }

    @Override
    protected Timestamp convertToNative(Class<Date> fromType, Date object, Class<Timestamp> toType) throws Exception {
        return new Timestamp(object.getTime());
    }

    @Override
    protected Date convertFromNative(Class<Timestamp> fromType, Timestamp object, Class<Date> toType) throws Exception {
        return object;
    }
}
