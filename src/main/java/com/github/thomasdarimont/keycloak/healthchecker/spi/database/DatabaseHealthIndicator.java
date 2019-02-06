package com.github.thomasdarimont.keycloak.healthchecker.spi.database;

import com.github.thomasdarimont.keycloak.healthchecker.model.HealthStatus;
import com.github.thomasdarimont.keycloak.healthchecker.spi.AbstractHealthIndicator;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;

import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

class DatabaseHealthIndicator extends AbstractHealthIndicator {

    private final KeycloakSession session;
    private final String healthQuery;
    private final String jndiName;

    DatabaseHealthIndicator(KeycloakSession session, Config.Scope config) {
        super("database");
        this.session = session;
        this.jndiName = config.get("jndiName", "java:jboss/datasources/KeycloakDS");
        this.healthQuery = config.get("query", "SELECT 1");
    }

    @Override
    public HealthStatus check() {

        try {
            DataSource dataSource = (DataSource) new InitialContext().lookup(jndiName);

            try (Connection connection = dataSource.getConnection()) {

                try (Statement statement = connection.createStatement()) {
                    if (statement.execute(healthQuery)) {
                        return reportUp().withAttribute("connection", "established");
                    }
                }
            }
        } catch (Exception ex) {
            return reportDown().withAttribute("connection", "error").withAttribute("message", ex.getMessage());
        }

        return reportDown();
    }
}
