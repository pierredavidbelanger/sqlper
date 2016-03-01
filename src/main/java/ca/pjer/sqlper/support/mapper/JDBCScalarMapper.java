package ca.pjer.sqlper.support.mapper;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JDBCScalarMapper extends ScalarMapper<Object> {

    private Class scalarType;

    public JDBCScalarMapper(Class scalarType) {
        this.scalarType = scalarType;
    }

    @Override
    protected void set(PreparedStatement statement, int column, Object object, int sqlType) throws SQLException {
        if (object == null) {
            statement.setNull(column, sqlType);
        } else {
            statement.setObject(column, object, sqlType);
        }
    }

    @Override
    protected Object get(ResultSet resultSet, int column, Class type, int sqlType) throws SQLException {
        if (scalarType == null) {
            return resultSet.getObject(column);
        } else {
            return resultSet.getObject(column, scalarType);
        }
    }
}
