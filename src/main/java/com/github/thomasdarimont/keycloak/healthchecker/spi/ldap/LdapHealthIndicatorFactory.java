package com.github.thomasdarimont.keycloak.healthchecker.spi.ldap;

import com.github.thomasdarimont.keycloak.healthchecker.spi.HealthIndicator;
import com.github.thomasdarimont.keycloak.healthchecker.spi.HealthIndicatorFactory;
import com.github.thomasdarimont.keycloak.healthchecker.spi.infinispan.InfinispanHealthIndicator;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class LdapHealthIndicatorFactory implements HealthIndicatorFactory {

    private static final String ID = "ldap-health";

    private Config.Scope config;

    @Override
    public HealthIndicator create(KeycloakSession session) {
        return new LdapHealthIndicator(session,config);
    }

    @Override
    public void init(Config.Scope config) {
        this.config = config;
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
        //NOOP
    }

    @Override
    public void close() {
        //NOOP
    }

    @Override
    public String getId() {
        return ID;
    }
}