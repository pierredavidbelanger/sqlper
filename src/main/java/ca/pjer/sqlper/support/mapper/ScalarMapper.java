package ca.pjer.sqlper.support.mapper;

import ca.pjer.sqlper.Mapper;
import ca.pjer.sqlper.MapperRegistry;
import ca.pjer.sqlper.MappingMetaData;
import ca.pjer.sqlper.SqlperException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public abstract class ScalarMapper<T> implements Mapper<T> {

    @Override
    public void map(MapperRegistry mapperRegistry, PreparedStatement preparedStatement, MappingMetaData metaData, int index, Class<T> type, T object) {
        try {
            set(preparedStatement, index + 1, object, metaData.getSqlTypes()[index]);
        } catch (SQLException e) {
            throw new SqlperException("Unable to set column '" + (index + 1) + "' with value '" + object + "' of type '" + type + "'", e);
        }
    }

    @Override
    public T map(MapperRegistry mapperRegistry, ResultSet resultSet, MappingMetaData metaData, int index, Class<T> type, T object) {
        try {
            return get(resultSet, index + 1, type, metaData.getSqlTypes()[index]);
        } catch (SQLException e) {
            throw new SqlperException("Unable to get column '" + (index + 1) + "' of type '" + type + "'", e);
        }
    }

    protected abstract void set(PreparedStatement statement, int column, T object, int sqlType) throws SQLException;

    protected abstract T get(ResultSet resultSet, int column, Class<T> type, int sqlType) throws SQLException;

}
