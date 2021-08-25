package com.github.thomasdarimont.keycloak.healthchecker.spi;


import com.github.thomasdarimont.keycloak.healthchecker.model.HealthStatus;
import com.github.thomasdarimont.keycloak.healthchecker.model.KeycloakHealthStatus;

public class GuardedHealthIndicator implements HealthIndicator {

    private final HealthIndicator healthIndicator;

    public GuardedHealthIndicator(HealthIndicator healthIndicator) {
        this.healthIndicator = healthIndicator;
    }

    @Override
    public String getName() {
        return this.healthIndicator.getName();
    }

    @Override
    public HealthStatus check() {

        try {
            return this.healthIndicator.check();
        } catch (Exception ex) {
            return KeycloakHealthStatus.down(this.healthIndicator.getName()) //
                    .withAttribute("error", "health-check")
                    .withAttribute("errorMessage", ex.getMessage());
        }
    }

    @Override
    public boolean isApplicable() {
        return healthIndicator.isApplicable();
    }
}
