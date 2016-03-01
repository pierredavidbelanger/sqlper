package ca.pjer.sqlper;

import java.sql.Connection;
import java.util.List;

public interface Sqlper extends AutoCloseable {

    Connection getConnection();

    int update(String sql);

    int update(String sql, Object parameters);

    int update(String sql, Object parameters, String... returning);

    <T> List<T> query(String sql, Class<T> resultsType);

    <T> List<T> query(String sql, Object parameters, Class<T> resultsType);

    <T> T queryOne(String sql, Class<T> resultsType);

    <T> T queryOne(String sql, Object parameters, Class<T> resultsType);

    void queryOne(String sql, Object parameters);

}
