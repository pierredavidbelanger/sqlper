package ca.pjer.sqlper.support;

import ca.pjer.sqlper.ConnectionProvider;
import ca.pjer.sqlper.SqlperException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DataSourceConnectionProvider implements ConnectionProvider {

    private final DataSource dataSource;

    public DataSourceConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            throw new SqlperException("Unable to get connection from dataSource", e);
        }
    }
}
