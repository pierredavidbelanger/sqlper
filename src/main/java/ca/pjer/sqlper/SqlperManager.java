package ca.pjer.sqlper;

public interface SqlperManager {

    ConnectionProvider getConnectionProvider();

    MappingFactory getMappingFactory();

    Sqlper open();

}
