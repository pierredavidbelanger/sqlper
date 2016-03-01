package ca.pjer.sqlper.support;

import ca.pjer.sqlper.Mapper;
import ca.pjer.sqlper.MapperRegistry;
import ca.pjer.sqlper.support.mapper.*;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultMapperRegistry implements MapperRegistry {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Map<Class, Mapper> mappers;

    public DefaultMapperRegistry() {
        this(Collections.<Class, Mapper>emptyMap());

        // JDBC native scalar

        register(new JDBCScalarMapper(null), (Class) null);

        register(new JDBCScalarMapper(String.class), String.class);

        register(new JDBCScalarMapper(Boolean.class), Boolean.TYPE, Boolean.class);
        register(new JDBCScalarMapper(Byte.class), Byte.TYPE, Byte.class);
        register(new JDBCScalarMapper(Short.class), Short.TYPE, Short.class);
        register(new JDBCScalarMapper(Integer.class), Integer.TYPE, Integer.class);
        register(new JDBCScalarMapper(Long.class), Long.TYPE, Long.class);
        register(new JDBCScalarMapper(Float.class), Float.TYPE, Float.class);
        register(new JDBCScalarMapper(Double.class), Double.TYPE, Double.class);

        register(new JDBCScalarMapper(byte[].class), byte[].class);

        register(new JDBCScalarMapper(BigDecimal.class), BigDecimal.class);
        register(new JDBCScalarMapper(URL.class), URL.class);

        register(new JDBCScalarMapper(java.sql.Date.class), java.sql.Date.class);
        register(new JDBCScalarMapper(java.sql.Time.class), java.sql.Time.class);
        register(new JDBCScalarMapper(java.sql.Timestamp.class), java.sql.Timestamp.class);

        register(new JDBCScalarMapper(java.sql.Clob.class), java.sql.Clob.class);
        register(new JDBCScalarMapper(java.sql.Blob.class), java.sql.Blob.class);

        register(new JDBCScalarMapper(java.sql.Array.class), java.sql.Array.class);
        register(new JDBCScalarMapper(java.sql.Struct.class), java.sql.Struct.class);
        register(new JDBCScalarMapper(java.sql.SQLData.class), java.sql.SQLData.class);
        register(new JDBCScalarMapper(java.sql.Ref.class), java.sql.Ref.class);
        register(new JDBCScalarMapper(java.sql.RowId.class), java.sql.RowId.class);

        register(new JDBCScalarMapper(java.sql.NClob.class), java.sql.NClob.class);
        register(new JDBCScalarMapper(java.sql.SQLXML.class), java.sql.SQLXML.class);

        // Other scalar

        register(new CharacterConverterMapper(), Character.TYPE, Character.class);
        register(new DateConverterMapper(), java.util.Date.class);
        register(new CalendarConverterMapper(), java.util.Calendar.class);

        // Object / Bean

        register(new ListObjectMapper(), List.class);
        register(new MapObjectMapper(UpperUnderscoreComparator.INSTANCE), Map.class);

        register(new ReflexionObjectMapper(UpperUnderscoreComparator.INSTANCE), Object.class);
    }

    public DefaultMapperRegistry(Map<Class, Mapper> mappers) {
        this.mappers = Collections.synchronizedMap(new LinkedHashMap<>(mappers));
    }

    protected void register(Mapper mapper, Class... types) {
        for (Class type : types) {
            register(type, mapper);
        }
    }

    @Override
    public <T> void register(Class<? extends T> type, Mapper<T> mapper) {
        mappers.put(type, mapper);
    }

    @Override
    public <T> Mapper<T> find(Class<T> type) {
        Mapper mapper = mappers.get(type);
        if (mapper != null) {
            return mapper;
        }
        if (type != null) {
            for (Map.Entry<Class, Mapper> entry : mappers.entrySet()) {
                if (entry.getKey() != null && entry.getKey().isAssignableFrom(type)) {
                    logger.log(Level.INFO, "No exact mapper found for {0}, but {0} is assignable to {1} so {2} will be used", new Object[]{type, entry.getKey(), entry.getValue()});
                    mapper = entry.getValue();
                    break;
                }
            }
        }
        if (mapper == null) {
            throw new RuntimeException("No mapper found for type " + type);
        }
        mappers.put(type, mapper);
        return mapper;
    }
}
