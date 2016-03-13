package ca.pjer.sqlper.support;

import ca.pjer.sqlper.Mapper;
import ca.pjer.sqlper.MapperRegistry;
import ca.pjer.sqlper.support.mapper.*;

import java.math.BigDecimal;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultMapperRegistry implements MapperRegistry {

    private final Logger logger = Logger.getLogger(getClass().getName());

    private final Map<Class, Mapper> runtimeMappers = Collections.synchronizedMap(new LinkedHashMap<Class, Mapper>());
    private final Map<Class, Mapper> customMappers = Collections.synchronizedMap(new LinkedHashMap<Class, Mapper>());
    private final Map<Class, Mapper> defaultMappers = Collections.synchronizedMap(new LinkedHashMap<Class, Mapper>());

    public DefaultMapperRegistry() {

        // JDBC native scalar

        registerDefaultMapper(new JDBCScalarMapper(null), (Class) null);

        registerDefaultMapper(new JDBCScalarMapper(String.class), String.class);

        registerDefaultMapper(new JDBCScalarMapper(Boolean.class), Boolean.TYPE, Boolean.class);
        registerDefaultMapper(new JDBCScalarMapper(Byte.class), Byte.TYPE, Byte.class);
        registerDefaultMapper(new JDBCScalarMapper(Short.class), Short.TYPE, Short.class);
        registerDefaultMapper(new JDBCScalarMapper(Integer.class), Integer.TYPE, Integer.class);
        registerDefaultMapper(new JDBCScalarMapper(Long.class), Long.TYPE, Long.class);
        registerDefaultMapper(new JDBCScalarMapper(Float.class), Float.TYPE, Float.class);
        registerDefaultMapper(new JDBCScalarMapper(Double.class), Double.TYPE, Double.class);

        registerDefaultMapper(new JDBCScalarMapper(byte[].class), byte[].class);

        registerDefaultMapper(new JDBCScalarMapper(BigDecimal.class), BigDecimal.class);
        registerDefaultMapper(new JDBCScalarMapper(URL.class), URL.class);
        registerDefaultMapper(new JDBCScalarMapper(UUID.class), UUID.class);

        registerDefaultMapper(new JDBCScalarMapper(java.sql.Date.class), java.sql.Date.class);
        registerDefaultMapper(new JDBCScalarMapper(java.sql.Time.class), java.sql.Time.class);
        registerDefaultMapper(new JDBCScalarMapper(java.sql.Timestamp.class), java.sql.Timestamp.class);

        registerDefaultMapper(new JDBCScalarMapper(java.sql.Clob.class), java.sql.Clob.class);
        registerDefaultMapper(new JDBCScalarMapper(java.sql.Blob.class), java.sql.Blob.class);

        registerDefaultMapper(new JDBCScalarMapper(java.sql.Array.class), java.sql.Array.class);
        registerDefaultMapper(new JDBCScalarMapper(java.sql.Struct.class), java.sql.Struct.class);
        registerDefaultMapper(new JDBCScalarMapper(java.sql.SQLData.class), java.sql.SQLData.class);
        registerDefaultMapper(new JDBCScalarMapper(java.sql.Ref.class), java.sql.Ref.class);
        registerDefaultMapper(new JDBCScalarMapper(java.sql.RowId.class), java.sql.RowId.class);

        registerDefaultMapper(new JDBCScalarMapper(java.sql.NClob.class), java.sql.NClob.class);
        registerDefaultMapper(new JDBCScalarMapper(java.sql.SQLXML.class), java.sql.SQLXML.class);

        // Other scalar

        registerDefaultMapper(new CharacterConverterMapper(), Character.TYPE, Character.class);
        registerDefaultMapper(new DateConverterMapper(), java.util.Date.class);
        registerDefaultMapper(new CalendarConverterMapper(), Calendar.class);
        registerDefaultMapper(new URIConverterMapper(), URI.class);

        // Object / Bean

        registerDefaultMapper(new ListObjectMapper(), List.class);
        registerDefaultMapper(new MapObjectMapper(UpperUnderscoreComparator.INSTANCE), Map.class);

        registerDefaultMapper(new ReflexionObjectMapper(UpperUnderscoreComparator.INSTANCE), Object.class);
    }

    public DefaultMapperRegistry(Map<Class, Mapper> mappers) {
        this();
        this.customMappers.putAll(mappers);
    }

    private void registerDefaultMapper(Mapper mapper, Class... types) {
        for (Class type : types) {
            defaultMappers.put(type, mapper);
        }
    }

    @Override
    public <T> void register(Class<? extends T> type, Mapper<T> mapper) {
        customMappers.put(type, mapper);
    }

    @Override
    public <T> Mapper<T> find(Class<T> type) {
        Mapper mapper = runtimeMappers.get(type);
        if (mapper != null) {
            return mapper;
        }
        if (type != null) {
            mapper = customMappers.get(type);
            if (mapper == null) {
                for (Map.Entry<Class, Mapper> entry : customMappers.entrySet()) {
                    if (entry.getKey() != null && entry.getKey().isAssignableFrom(type)) {
                        logger.log(Level.INFO, "No exact mapper found for {0}, but {0} is assignable to {1} so user defined mapper {2} will be used", new Object[]{type, entry.getKey(), entry.getValue()});
                        mapper = entry.getValue();
                        break;
                    }
                }
                if (mapper == null) {
                    mapper = defaultMappers.get(type);
                    if (mapper == null) {
                        for (Map.Entry<Class, Mapper> entry : defaultMappers.entrySet()) {
                            if (entry.getKey() != null && entry.getKey().isAssignableFrom(type)) {
                                logger.log(Level.INFO, "No exact mapper found for {0}, but {0} is assignable to {1} so default mapper {2} will be used", new Object[]{type, entry.getKey(), entry.getValue()});
                                mapper = entry.getValue();
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            mapper = customMappers.get(null);
            if (mapper == null) {
                mapper = defaultMappers.get(null);
            }
        }
        if (mapper == null) {
            throw new RuntimeException("No mapper found for type " + type);
        }
        runtimeMappers.put(type, mapper);
        return mapper;
    }
}
