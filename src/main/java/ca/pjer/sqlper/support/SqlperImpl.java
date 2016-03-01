package ca.pjer.sqlper.support;

import ca.pjer.sqlper.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SqlperImpl implements Sqlper {

    private final Connection connection;
    private final MappingFactory mappingFactory;

    public SqlperImpl(Connection connection, MappingFactory mappingFactory) {
        this.connection = connection;
        this.mappingFactory = mappingFactory;
    }

    @Override
    public Connection getConnection() {
        return connection;
    }

    @Override
    public int update(String sql) {
        ParsedSql parsedSql = mappingFactory.parseSql(sql);
        try (PreparedStatement preparedStatement = connection.prepareStatement(parsedSql.getSql())) {
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SqlperException("Unable to prepare statement: " + parsedSql.getSql(), e);
        }
    }

    @Override
    public int update(String sql, Object parameters) {
        MapperRegistry mapperRegistry = mappingFactory.getMapperRegistry();
        ParsedSql parsedSql = mappingFactory.parseSql(sql);
        try (PreparedStatement preparedStatement = connection.prepareStatement(parsedSql.getSql())) {
            MappingMetaData parametersMetaData = mappingFactory.extractMetaData(parsedSql, preparedStatement);
            Mapper parametersMapper = mapperRegistry.find(parameters.getClass());
            parametersMapper.map(mapperRegistry, preparedStatement, parametersMetaData, 0, parameters.getClass(), parameters);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new SqlperException("Unable to prepare statement: " + parsedSql.getSql(), e);
        }
    }

    @Override
    public int update(String sql, Object parameters, String... returning) {
        MapperRegistry mapperRegistry = mappingFactory.getMapperRegistry();
        ParsedSql parsedSql = mappingFactory.parseSql(sql);
        try (PreparedStatement preparedStatement = connection.prepareStatement(parsedSql.getSql(), returning)) {
            MappingMetaData parametersMetaData = mappingFactory.extractMetaData(parsedSql, preparedStatement);
            Mapper parametersMapper = mapperRegistry.find(parameters.getClass());
            parametersMapper.map(mapperRegistry, preparedStatement, parametersMetaData, 0, parameters.getClass(), parameters);
            int count = preparedStatement.executeUpdate();
            try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
                if (resultSet.next()) {
                    MappingMetaData resultSetMetaData = mappingFactory.extractMetaData(parsedSql, resultSet);
                    parametersMapper.map(mapperRegistry, resultSet, resultSetMetaData, 0, parameters.getClass(), parameters);
                }
            }
            return count;
        } catch (SQLException e) {
            throw new SqlperException("Unable to prepare statement: " + parsedSql.getSql(), e);
        }
    }

    @Override
    public <T> List<T> query(String sql, Class<T> resultsType) {
        MapperRegistry mapperRegistry = mappingFactory.getMapperRegistry();
        ParsedSql parsedSql = mappingFactory.parseSql(sql);
        try (PreparedStatement preparedStatement = connection.prepareStatement(parsedSql.getSql())) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                MappingMetaData resultsMetaData = mappingFactory.extractMetaData(parsedSql, resultSet);
                Mapper<T> resultsMapper = mapperRegistry.find(resultsType);
                // TODO: find a good initial capacity ? maybe cache the last size for resultsType ?
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(resultsMapper.map(mapperRegistry, resultSet, resultsMetaData, 0, resultsType, null));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new SqlperException("Unable to prepare statement: " + parsedSql.getSql(), e);
        }
    }

    @Override
    public <T> List<T> query(String sql, Object parameters, Class<T> resultsType) {
        MapperRegistry mapperRegistry = mappingFactory.getMapperRegistry();
        ParsedSql parsedSql = mappingFactory.parseSql(sql);
        try (PreparedStatement preparedStatement = connection.prepareStatement(parsedSql.getSql())) {
            MappingMetaData parametersMetaData = mappingFactory.extractMetaData(parsedSql, preparedStatement);
            Mapper parametersMapper = mapperRegistry.find(parameters.getClass());
            parametersMapper.map(mapperRegistry, preparedStatement, parametersMetaData, 0, parameters.getClass(), parameters);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                MappingMetaData resultsMetaData = mappingFactory.extractMetaData(parsedSql, resultSet);
                Mapper<T> resultsMapper = mapperRegistry.find(resultsType);
                // TODO: find a good initial capacity ? maybe cache the last size for resultsType ?
                List<T> results = new ArrayList<>();
                while (resultSet.next()) {
                    results.add(resultsMapper.map(mapperRegistry, resultSet, resultsMetaData, 0, resultsType, null));
                }
                return results;
            }
        } catch (SQLException e) {
            throw new SqlperException("Unable to prepare statement: " + parsedSql.getSql(), e);
        }
    }

    @Override
    public <T> T queryOne(String sql, Class<T> resultsType) {
        MapperRegistry mapperRegistry = mappingFactory.getMapperRegistry();
        ParsedSql parsedSql = mappingFactory.parseSql(sql);
        try (PreparedStatement preparedStatement = connection.prepareStatement(parsedSql.getSql())) {
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    MappingMetaData resultsMetaData = mappingFactory.extractMetaData(parsedSql, resultSet);
                    Mapper<T> resultsMapper = mapperRegistry.find(resultsType);
                    T result = resultsMapper.map(mapperRegistry, resultSet, resultsMetaData, 0, resultsType, null);
                    if (resultSet.next()) {
                        throw new SqlperException("More than one row returned from a queryOne");
                    }
                    return result;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new SqlperException("Unable to prepare statement: " + parsedSql.getSql(), e);
        }
    }

    @Override
    public <T> T queryOne(String sql, Object parameters, Class<T> resultsType) {
        MapperRegistry mapperRegistry = mappingFactory.getMapperRegistry();
        ParsedSql parsedSql = mappingFactory.parseSql(sql);
        try (PreparedStatement preparedStatement = connection.prepareStatement(parsedSql.getSql())) {
            MappingMetaData parametersMetaData = mappingFactory.extractMetaData(parsedSql, preparedStatement);
            Mapper parametersMapper = mapperRegistry.find(parameters.getClass());
            parametersMapper.map(mapperRegistry, preparedStatement, parametersMetaData, 0, parameters.getClass(), parameters);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    MappingMetaData resultsMetaData = mappingFactory.extractMetaData(parsedSql, resultSet);
                    Mapper<T> resultsMapper = mapperRegistry.find(resultsType);
                    T result = resultsMapper.map(mapperRegistry, resultSet, resultsMetaData, 0, resultsType, null);
                    if (resultSet.next()) {
                        throw new SqlperException("More than one row returned from a queryOne");
                    }
                    return result;
                }
                return null;
            }
        } catch (SQLException e) {
            throw new SqlperException("Unable to prepare statement: " + parsedSql.getSql(), e);
        }
    }

    @Override
    public void queryOne(String sql, Object parameters) {
        MapperRegistry mapperRegistry = mappingFactory.getMapperRegistry();
        ParsedSql parsedSql = mappingFactory.parseSql(sql);
        try (PreparedStatement preparedStatement = connection.prepareStatement(parsedSql.getSql())) {
            MappingMetaData parametersMetaData = mappingFactory.extractMetaData(parsedSql, preparedStatement);
            Mapper parametersMapper = mapperRegistry.find(parameters.getClass());
            parametersMapper.map(mapperRegistry, preparedStatement, parametersMetaData, 0, parameters.getClass(), parameters);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    MappingMetaData resultsMetaData = mappingFactory.extractMetaData(parsedSql, resultSet);
                    parametersMapper.map(mapperRegistry, resultSet, resultsMetaData, 0, parameters.getClass(), parameters);
                    if (resultSet.next()) {
                        throw new SqlperException("More than one row returned from queryOne");
                    }
                } else {
                    throw new SqlperException("No row returned from void queryOne");
                }
            }
        } catch (SQLException e) {
            throw new SqlperException("Unable to prepare statement: " + parsedSql.getSql(), e);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            throw new SqlperException("Unable to close connection", e);
        }
    }
}
