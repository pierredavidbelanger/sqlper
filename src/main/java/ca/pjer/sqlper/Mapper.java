package ca.pjer.sqlper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface Mapper<T> {

    void map(MapperRegistry mapperRegistry, PreparedStatement preparedStatement, MappingMetaData metaData, int index, Class<T> type, T object);

    T map(MapperRegistry mapperRegistry, ResultSet resultSet, MappingMetaData metaData, int index, Class<T> type, T object);

}
