package ca.pjer.sqlper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public interface MappingFactory {

    MapperRegistry getMapperRegistry();

    ParsedSql parseSql(String sql);

    MappingMetaData extractMetaData(ParsedSql parsedSql, PreparedStatement preparedStatement);

    MappingMetaData extractMetaData(ParsedSql parsedSql, ResultSet resultSet);

}
