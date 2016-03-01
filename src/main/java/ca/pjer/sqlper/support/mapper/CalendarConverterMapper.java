package ca.pjer.sqlper.support.mapper;

public class CalendarConverterMapper extends ConverterMapper<java.util.Calendar, java.sql.Timestamp> {

    public CalendarConverterMapper() {
        super(java.sql.Timestamp.class);
    }

    @Override
    protected java.sql.Timestamp convert(Class<java.util.Calendar> type, java.util.Calendar object) throws Exception {
        return new java.sql.Timestamp(object.getTimeInMillis());
    }

    @Override
    protected java.util.Calendar convert(java.sql.Timestamp object) throws Exception {
        java.util.Calendar c = java.util.Calendar.getInstance();
        c.setTime(object);
        return c;
    }
}
