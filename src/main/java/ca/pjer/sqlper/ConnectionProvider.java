package ca.pjer.sqlper;

import java.sql.Connection;

public interface ConnectionProvider {

    Connection getConnection();

}
