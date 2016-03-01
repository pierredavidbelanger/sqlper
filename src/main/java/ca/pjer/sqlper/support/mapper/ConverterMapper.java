package ca.pjer.sqlper.support.mapper;

import ca.pjer.sqlper.Mapper;
import ca.pjer.sqlper.MapperRegistry;
import ca.pjer.sqlper.MappingMetaData;
import ca.pjer.sqlper.SqlperException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public abstract class ConverterMapper<T, N> implements Mapper<T> {

    private final Class<N> nativeType;
    private final boolean nullSafe;

    public ConverterMapper(Class<N> nativeType) {
        this(nativeType, true);
    }

    public ConverterMapper(Class<N> nativeType, boolean nullSafe) {
        this.nativeType = nativeType;
        this.nullSafe = nullSafe;
    }

    @Override
    public void map(MapperRegistry mapperRegistry, PreparedStatement preparedStatement, MappingMetaData metaData, int index, Class<T> type, T object) {
        if (object == null && nullSafe) {
            Mapper nullMapper = mapperRegistry.find(null);
            nullMapper.map(mapperRegistry, preparedStatement, metaData, index, type, object);
        } else {
            N nativeValue;
            try {
                nativeValue = convert(type, object);
            } catch (Exception e) {
                throw new SqlperException("Unable to convert value '" + object + "' of type '" + type + "' to type '" + nativeType + "'", e);
            }
            Mapper<N> nativeMapper = mapperRegistry.find(nativeType);
            nativeMapper.map(mapperRegistry, preparedStatement, metaData, index, nativeType, nativeValue);
        }
    }

    @Override
    public T map(MapperRegistry mapperRegistry, ResultSet resultSet, MappingMetaData metaData, int index, Class<T> type, T object) {
        Mapper<N> nativeMapper = mapperRegistry.find(nativeType);
        N nativeValue = nativeMapper.map(mapperRegistry, resultSet, metaData, index, nativeType, null);
        if (nativeValue == null && nullSafe) {
            return null;
        } else {
            try {
                return convert(nativeValue);
            } catch (Exception e) {
                throw new SqlperException("Unable to convert value '" + nativeValue + "' of type '" + nativeType + "' to type '" + type + "'", e);
            }
        }
    }

    protected abstract N convert(Class<T> type, T object) throws Exception;

    protected abstract T convert(N object) throws Exception;

}
