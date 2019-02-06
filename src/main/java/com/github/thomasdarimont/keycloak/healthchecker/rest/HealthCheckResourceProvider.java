package com.github.thomasdarimont.keycloak.healthchecker.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class HealthCheckResourceProvider implements RealmResourceProvider {

    private final KeycloakSession session;

    public HealthCheckResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public Object getResource() {
        return new HealthCheckResource(session);
    }

    @Override
    public void close() {
        // NOOP
    }
}
