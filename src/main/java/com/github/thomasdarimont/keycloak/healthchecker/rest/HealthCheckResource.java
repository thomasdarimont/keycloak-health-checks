package com.github.thomasdarimont.keycloak.healthchecker.rest;


import com.github.thomasdarimont.keycloak.healthchecker.model.AggregatedHealthStatus;
import com.github.thomasdarimont.keycloak.healthchecker.model.HealthStatus;
import com.github.thomasdarimont.keycloak.healthchecker.spi.GuardedHeathIndicator;
import com.github.thomasdarimont.keycloak.healthchecker.spi.HealthIndicator;
import org.keycloak.models.KeycloakSession;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Optional;
import java.util.Set;

public class HealthCheckResource {

    private static final Response NOT_FOUND = Response.status(Response.Status.NOT_FOUND).build();

    private final KeycloakSession session;

    public HealthCheckResource(KeycloakSession session) {
        this.session = session;
    }

    @GET
    @Path("check")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkHealth() {

        return aggregatedHealthStatusFrom(this.session.getAllProviders(HealthIndicator.class))
                .map(this::toHealthResponse)
                .orElse(NOT_FOUND);
    }

    @GET
    @Path("check/{indicator}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkHealthFor(@PathParam("indicator") String name) {

        return tryFindFirstHealthIndicatorWithName(name)
                .map(GuardedHeathIndicator::new)
                .map(HealthIndicator::check)
                .map(this::toHealthResponse)
                .orElse(NOT_FOUND);
    }

    private Optional<HealthStatus> aggregatedHealthStatusFrom(Set<HealthIndicator> healthIndicators) {

        return healthIndicators.stream() //
                .map(GuardedHeathIndicator::new) //
                .map(HealthIndicator::check) //
                .reduce(this::combineHealthStatus); //
    }

    private Response toHealthResponse(HealthStatus health) {

        if (health.isUp()) {
            return Response.ok(health).build();
        }

        return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(health).build();
    }

    private Optional<HealthIndicator> tryFindFirstHealthIndicatorWithName(String healthIndicatorName) {

        Set<HealthIndicator> allProviders = this.session.getAllProviders(HealthIndicator.class);
        return allProviders.stream().filter(i -> i.getName().equals(healthIndicatorName)).findFirst();
    }

    private HealthStatus combineHealthStatus(HealthStatus first, HealthStatus second) {

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
