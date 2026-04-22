package com.smartresidential.backend.services;

import org.flywaydb.core.Flyway;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;

@Service
public class TenantProvisioningService {

    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;

    public TenantProvisioningService(JdbcTemplate jdbcTemplate, DataSource dataSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.dataSource = dataSource;
    }

    public void provisionTenant(String schemaName) {
        validateSchemaName(schemaName);
        createSchema(schemaName);
        runTenantMigrations(schemaName);
    }

    private void createSchema(String schemaName) {
        String sql = "CREATE SCHEMA IF NOT EXISTS \"" + schemaName.replace("\"", "\"\"") + "\"";
        jdbcTemplate.execute(sql);
    }

    private void runTenantMigrations(String schemaName) {
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .schemas(schemaName)
                .defaultSchema(schemaName)
                .locations("classpath:db/migration/tenant")
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
    }

    private void validateSchemaName(String schemaName) {
        if (schemaName == null || schemaName.isBlank()) {
            throw new IllegalArgumentException("Schema name must not be null or blank.");
        }
    }
}