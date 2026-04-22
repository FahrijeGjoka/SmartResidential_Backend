package com.smartresidential.backend.multitenancy.hibernate;

import org.hibernate.engine.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class SchemaMultiTenantConnectionProvider implements MultiTenantConnectionProvider<String> {

    private static final String DEFAULT_SCHEMA = "public";

    private final DataSource dataSource;

    public SchemaMultiTenantConnectionProvider(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getAnyConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public void releaseAnyConnection(Connection connection) throws SQLException {
        resetSchema(connection);
        connection.close();
    }

    @Override
    public Connection getConnection(String tenantIdentifier) throws SQLException {
        Connection connection = getAnyConnection();
        String schema = resolveSchema(tenantIdentifier);

        try {
            setSchema(connection, schema);
        } catch (SQLException ex) {
            connection.close();
            throw new SQLException("Failed to switch to schema: " + schema, ex);
        }

        return connection;
    }

    @Override
    public void releaseConnection(String tenantIdentifier, Connection connection) throws SQLException {
        try {
            resetSchema(connection);
        } finally {
            connection.close();
        }
    }

    @Override
    public boolean supportsAggressiveRelease() {
        return false;
    }

    @Override
    public boolean isUnwrappableAs(Class<?> unwrapType) {
        return MultiTenantConnectionProvider.class.equals(unwrapType)
                || SchemaMultiTenantConnectionProvider.class.equals(unwrapType);
    }

    @Override
    public <T> T unwrap(Class<T> unwrapType) {
        if (isUnwrappableAs(unwrapType)) {
            return unwrapType.cast(this);
        }
        throw new IllegalArgumentException("Cannot unwrap to " + unwrapType.getName());
    }

    private String resolveSchema(String tenantIdentifier) {
        if (tenantIdentifier == null || tenantIdentifier.isBlank()) {
            return DEFAULT_SCHEMA;
        }
        return tenantIdentifier;
    }

    private void setSchema(Connection connection, String schema) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET search_path TO " + quoteIdentifier(schema));
        }
    }

    private void resetSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("SET search_path TO " + DEFAULT_SCHEMA);
        }
    }

    private String quoteIdentifier(String identifier) {
        return "\"" + identifier.replace("\"", "\"\"") + "\"";
    }
}