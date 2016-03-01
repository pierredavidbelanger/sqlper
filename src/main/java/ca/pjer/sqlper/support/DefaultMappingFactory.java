package ca.pjer.sqlper.support;

import ca.pjer.sqlper.*;

import java.sql.*;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.regex.Pattern;

public class DefaultMappingFactory implements MappingFactory {

    // TODO configurable ?
    private static final Pattern PARAM_PATTERN = Pattern.compile("[^:](:([\\p{Alpha}_][\\p{Alnum}_]*))");

    private final MapperRegistry mapperRegistry;

    private final Map<String, ParsedSql> parsedSqlCache;
    private final Map<String, MappingMetaData> preparedStatementMetaDataCache;
    private final Map<String, MappingMetaData> resultSetMetaDataCache;

    public DefaultMappingFactory() {
        this(new DefaultMapperRegistry());
    }

    public DefaultMappingFactory(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
        parsedSqlCache = Collections.synchronizedMap(new WeakHashMap<String, ParsedSql>());
        preparedStatementMetaDataCache = Collections.synchronizedMap(new WeakHashMap<String, MappingMetaData>());
        resultSetMetaDataCache = Collections.synchronizedMap(new WeakHashMap<String, MappingMetaData>());
    }

    @Override
    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Override
    public ParsedSql parseSql(String sql) {
        ParsedSql parsedSql = parsedSqlCache.get(sql);
        if (parsedSql == null) {
            parsedSql = new PatternParsedSql(PARAM_PATTERN, sql);
            parsedSqlCache.put(sql, parsedSql);
        }
        return parsedSql;
    }

    @Override
    public MappingMetaData extractMetaData(ParsedSql parsedSql, PreparedStatement preparedStatement) {
        MappingMetaData mappingMetaData = preparedStatementMetaDataCache.get(parsedSql.getSql());
        if (mappingMetaData == null) {
            try {
                String[] parameterNames = parsedSql.getParameterNames();
                ParameterMetaData metaData = preparedStatement.getParameterMetaData();
                int count = metaData.getParameterCount();
                String[] names = new String[count];
                int[] sqlTypes = new int[count];
                for (int i = 0; i < count; i++) {
                    names[i] = i < parameterNames.length ? parameterNames[i] : null;
                    sqlTypes[i] = metaData.getParameterType(i + 1);
                }
                mappingMetaData = new MappingMetaDataImpl(count, names, sqlTypes);
                preparedStatementMetaDataCache.put(parsedSql.getSql(), mappingMetaData);
            } catch (SQLException e) {
                throw new SqlperException("Unable to extract meta data from prepared statement", e);
            }
        }
        return mappingMetaData;
    }

    @Override
    public MappingMetaData extractMetaData(ParsedSql parsedSql, ResultSet resultSet) {
        MappingMetaData mappingMetaData = resultSetMetaDataCache.get(parsedSql.getSql());
        if (mappingMetaData == null) {
            try {
                ResultSetMetaData metaData = resultSet.getMetaData();
                int count = metaData.getColumnCount();
                String[] names = new String[count];
                int[] sqlTypes = new int[count];
                for (int i = 0; i < count; i++) {
                    names[i] = metaData.getColumnLabel(i + 1);
                    sqlTypes[i] = metaData.getColumnType(i + 1);
                }
                mappingMetaData = new MappingMetaDataImpl(count, names, sqlTypes);
                resultSetMetaDataCache.put(parsedSql.getSql(), mappingMetaData);
            } catch (SQLException e) {
                throw new SqlperException("Unable to extract meta data from result set", e);
            }
        }
        return mappingMetaData;
    }
}
