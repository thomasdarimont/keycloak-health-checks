package com.github.thomasdarimont.keycloak.healthchecker.spi.infinispan;

import com.github.thomasdarimont.keycloak.healthchecker.spi.HealthIndicator;
import com.github.thomasdarimont.keycloak.healthchecker.spi.HealthIndicatorFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class InfinispanHealthIndicatorFactory implements HealthIndicatorFactory {

    private static final String ID = "infinispan-health";

    private Config.Scope config;

    @Override
    public HealthIndicator create(KeycloakSession session) {
        return new InfinispanHealthIndicator(config);
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