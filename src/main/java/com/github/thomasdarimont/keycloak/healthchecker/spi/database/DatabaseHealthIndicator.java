package com.github.thomasdarimont.keycloak.healthchecker.spi.database;

import com.github.thomasdarimont.keycloak.healthchecker.model.HealthStatus;
import com.github.thomasdarimont.keycloak.healthchecker.spi.AbstractHealthIndicator;
import com.github.thomasdarimont.keycloak.healthchecker.support.KeycloakUtil;
import lombok.extern.jbosslog.JBossLog;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.utils.StringUtil;

import jakarta.enterprise.inject.spi.CDI;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLSyntaxErrorException;
import java.sql.Statement;

@JBossLog
public class DatabaseHealthIndicator extends AbstractHealthIndicator {

    public static final String KEYCLOAK_DATASOURCE_JNDI_NAME = "java:jboss/datasources/KeycloakDS";

    protected final String healthQuery;

    protected final String jndiName;

    protected final int connectionTimeoutMillis;

    public DatabaseHealthIndicator(KeycloakSession session, Config.Scope config) {
        super("database");
        this.jndiName = config.get("jndiName", KEYCLOAK_DATASOURCE_JNDI_NAME);
        this.healthQuery = config.get("query");
        this.connectionTimeoutMillis = config.getInt("connectionTimeout", 1000);
    }

    @Override
    public HealthStatus check() {

        try {
            DataSource dataSource = lookupDataSource();
            if (isDatabaseReady(dataSource, healthQuery)) {
                return reportUp().withAttribute("connection", "established");
            }
        } catch (Exception ex) {
            return reportDown().withAttribute("connection", "error").withAttribute("message", ex.getMessage());
        }

        return reportDown();
    }

    protected DataSource lookupDataSource() throws Exception {
        if (KeycloakUtil.isRunningOnKeycloak()) {
            return (DataSource) new InitialContext().lookup(jndiName);
        }

        // Manual lookup via CDI for Keycloak.X
        return CDI.current().select(DataSource.class).get();
    }

    protected boolean isDatabaseReady(DataSource dataSource, String healthQuery) throws Exception {

        try (Connection connection = dataSource.getConnection()) {
            boolean valid;
            if (StringUtil.isNotBlank(healthQuery)) {
                try (Statement statement = connection.createStatement()) {
                    try {
                        valid = statement.execute(healthQuery);
                    } catch (SQLSyntaxErrorException syntaxErrorException) {
                        log.errorf("Health Query is invalid", syntaxErrorException.getMessage());
                        valid = false;
                    }
                }
            } else {
                valid = connection.isValid(connectionTimeoutMillis);
            }
            log.debugf("Connection is Valid %s", valid);
            return valid;
        }
    }
}
