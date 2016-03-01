package ca.pjer.sqlper.support.mapper;

public class DateConverterMapper extends ConverterMapper<java.util.Date, java.sql.Timestamp> {

    public DateConverterMapper() {
        super(java.sql.Timestamp.class);
    }

    @Override
    protected java.sql.Timestamp convert(Class<java.util.Date> type, java.util.Date object) throws Exception {
        return new java.sql.Timestamp(object.getTime());
    }

    @Override
    protected java.util.Date convert(java.sql.Timestamp object) throws Exception {
        return object;
    }
}
