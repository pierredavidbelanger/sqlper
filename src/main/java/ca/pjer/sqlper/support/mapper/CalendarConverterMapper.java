package ca.pjer.sqlper.support.mapper;

import java.sql.Timestamp;
import java.util.Calendar;

public class CalendarConverterMapper extends ConverterMapper<java.util.Calendar, java.sql.Timestamp> {

    public CalendarConverterMapper() {
        super(java.sql.Timestamp.class);
    }

    @Override
    protected Timestamp convertToNative(Class<Calendar> fromType, Calendar object, Class<Timestamp> toType) throws Exception {
        return new Timestamp(object.getTimeInMillis());
    }

    @Override
    protected Calendar convertFromNative(Class<Timestamp> fromType, Timestamp object, Class<Calendar> toType) throws Exception {
        Calendar c = Calendar.getInstance();
        c.setTime(object);
        return c;
    }
}
