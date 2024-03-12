package com.github.thomasdarimont.keycloak.healthchecker.rest;


import com.github.thomasdarimont.keycloak.healthchecker.model.AggregatedHealthStatus;
import com.github.thomasdarimont.keycloak.healthchecker.model.HealthStatus;
import com.github.thomasdarimont.keycloak.healthchecker.spi.GuardedHealthIndicator;
import com.github.thomasdarimont.keycloak.healthchecker.spi.HealthIndicator;
import org.keycloak.models.KeycloakSession;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

public class HealthCheckResource {

    public static final Response NOT_FOUND = Response.status(Response.Status.NOT_FOUND).build();

    private static final Comparator<HealthIndicator> HEALTH_INDICATOR_COMPARATOR = Comparator.comparing(
            HealthIndicator::getName,
            Comparator.naturalOrder());

    protected final KeycloakSession session;

    public HealthCheckResource(KeycloakSession session) {
        this.session = session;
    }

    @GET
    @Path("check")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkHealth() {

        Set<HealthIndicator> checks = new TreeSet<>(HEALTH_INDICATOR_COMPARATOR);
        checks.addAll(this.session.getAllProviders(HealthIndicator.class));

        return aggregatedHealthStatusFrom(checks)
                .map(this::toHealthResponse)
                .orElse(NOT_FOUND);
    }

    @GET
    @Path("check/{indicator}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkHealthFor(@PathParam("indicator") String name) {

        return tryFindFirstHealthIndicatorWithName(name)
                .map(GuardedHealthIndicator::new)
                .map(HealthIndicator::check)
                .map(this::toHealthResponse)
                .orElse(NOT_FOUND);
    }

    protected Optional<HealthStatus> aggregatedHealthStatusFrom(Set<HealthIndicator> healthIndicators) {

        return healthIndicators.stream() //
                .map(GuardedHealthIndicator::new) //
                .filter(HealthIndicator::isApplicable) // only show relevant health indicators
                .map(HealthIndicator::check) //
                .reduce(this::combineHealthStatus); //
    }

    protected Response toHealthResponse(HealthStatus health) {

        if (health.isUp()) {
            return Response.ok(health).build();
        }

        return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(health).build();
    }

    protected Optional<HealthIndicator> tryFindFirstHealthIndicatorWithName(String healthIndicatorName) {

        Set<HealthIndicator> allProviders = this.session.getAllProviders(HealthIndicator.class);
        return allProviders.stream().filter(i -> i.getName().equals(healthIndicatorName)).findFirst();
    }

    protected HealthStatus combineHealthStatus(HealthStatus first, HealthStatus second) {

        if (!(first instanceof AggregatedHealthStatus)) {

            AggregatedHealthStatus healthStatus = new AggregatedHealthStatus();
            healthStatus.addHealthInfo(first);
            healthStatus.addHealthInfo(second);

            return healthStatus;
        }

        AggregatedHealthStatus accumulator = (AggregatedHealthStatus) first;
        accumulator.addHealthInfo(second);

        return accumulator;
    }
}
