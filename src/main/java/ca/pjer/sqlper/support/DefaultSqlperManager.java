package ca.pjer.sqlper.support;

import ca.pjer.sqlper.ConnectionProvider;
import ca.pjer.sqlper.MappingFactory;
import ca.pjer.sqlper.Sqlper;
import ca.pjer.sqlper.SqlperManager;

public class DefaultSqlperManager implements SqlperManager {

    private final ConnectionProvider connectionProvider;
    private final MappingFactory mappingFactory;

    public DefaultSqlperManager(ConnectionProvider connectionProvider) {
        this(connectionProvider, new DefaultMappingFactory());
    }

    public DefaultSqlperManager(ConnectionProvider connectionProvider, MappingFactory mappingFactory) {
        this.connectionProvider = connectionProvider;
        this.mappingFactory = mappingFactory;
    }

    @Override
    public ConnectionProvider getConnectionProvider() {
        return connectionProvider;
    }

    @Override
    public MappingFactory getMappingFactory() {
        return mappingFactory;
    }

    @Override
    public Sqlper open() {
        return new SqlperImpl(connectionProvider.getConnection(), mappingFactory);
    }
}
