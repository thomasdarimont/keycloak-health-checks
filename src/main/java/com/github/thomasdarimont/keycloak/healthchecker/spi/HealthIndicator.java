package com.github.thomasdarimont.keycloak.healthchecker.spi;

import com.github.thomasdarimont.keycloak.healthchecker.model.HealthStatus;
import org.keycloak.provider.Provider;

public interface HealthIndicator extends Provider {

    /**
     * The name of the health indicator
     *
     * @return
     */
    String getName();

    /**
     * Performs the health check.
     *
     * @return the outcome of the health check
     */
    HealthStatus check();

    /**
     * Determines if the health check is applicable.
     *
     * @return
     */
    default boolean isApplicable() {
        return true;
    }

    default void close() {
    }
}
