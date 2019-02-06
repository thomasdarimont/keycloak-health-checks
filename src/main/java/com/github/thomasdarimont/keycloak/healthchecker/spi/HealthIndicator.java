package com.github.thomasdarimont.keycloak.healthchecker.spi;

import com.github.thomasdarimont.keycloak.healthchecker.model.HealthStatus;
import org.keycloak.provider.Provider;

public interface HealthIndicator extends Provider {

    String getName();

    HealthStatus check();

    default void close() {
    }
}
