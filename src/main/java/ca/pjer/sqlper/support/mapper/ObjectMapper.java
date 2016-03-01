package ca.pjer.sqlper.support.mapper;

import ca.pjer.sqlper.Mapper;
import ca.pjer.sqlper.MapperRegistry;
import ca.pjer.sqlper.MappingMetaData;
import ca.pjer.sqlper.SqlperException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public abstract class ObjectMapper<T> implements Mapper<T> {

    @Override
    public void map(MapperRegistry mapperRegistry, PreparedStatement preparedStatement, MappingMetaData metaData, int index, Class<T> type, T object) {
        for (int i = index; i < metaData.getCount(); i++) {
            String name = metaData.getNames()[i];
            Object value = getPropertyValueOrThrow(metaData, object, name, i);
            Class valueType = getPropertyTypeOrThrow(metaData, object, name, i);
            Mapper valueMapper = mapperRegistry.find(valueType);
            valueMapper.map(mapperRegistry, preparedStatement, metaData, i, valueType, value);
        }
    }

    @Override
    public T map(MapperRegistry mapperRegistry, ResultSet resultSet, MappingMetaData metaData, int index, Class<T> type, T object) {
        if (object == null) {
            object = newInstanceOrThrow(metaData, type);
        }
        for (int i = index; i < metaData.getCount(); i++) {
            String name = metaData.getNames()[i];
            Class valueType = getPropertyTypeOrThrow(metaData, object, name, i);;
            Mapper valueMapper = mapperRegistry.find(valueType);
            Object value = valueMapper.map(mapperRegistry, resultSet, metaData, i, valueType, null);
            setPropertyValueOrThrow(metaData, object, name, i, value);
        }
        return object;
    }

    protected T newInstanceOrThrow(MappingMetaData metaData, Class<T> type) {
        try {
            return newInstance(metaData, type);
        } catch (Exception e) {
            throw new SqlperException("Unable to create instance of " + type, e);
        }
    }

    protected Class getPropertyTypeOrThrow(MappingMetaData metaData, T object, String name, int index) {
        try {
            return getPropertyType(metaData, object, name, index);
        } catch (Exception e) {
            throw new SqlperException("Unable to get type from property name '" + name + "' of object '" + object + "' type '" + object.getClass() + "'", e);
        }
    }

    protected Object getPropertyValueOrThrow(MappingMetaData metaData, T object, String name, int index) {
        try {
            return getPropertyValue(metaData, object, name, index);
        } catch (Exception e) {
            throw new SqlperException("Unable to get value from property name '" + name + "' of object '" + object + "' type '" + object.getClass() + "'", e);
        }
    }

    protected void setPropertyValueOrThrow(MappingMetaData metaData, T object, String name, int index, Object value) {
        try {
            setPropertyValue(metaData, object, name, index, value);
        } catch (Exception e) {
            throw new SqlperException("Unable to set value '" + value + "' type '" + (value != null ? value.getClass() : "null") + "' into property name '" + name + "' of object '" + object + "' type '" + object.getClass() + "'", e);
        }
    }

    protected abstract T newInstance(MappingMetaData metaData, Class<T> type) throws Exception;

    protected abstract Class getPropertyType(MappingMetaData metaData, T object, String name, int index) throws Exception;

    protected abstract Object getPropertyValue(MappingMetaData metaData, T object, String name, int index) throws Exception;

    protected abstract void setPropertyValue(MappingMetaData metaData, T object, String name, int index, Object value) throws Exception;

}
