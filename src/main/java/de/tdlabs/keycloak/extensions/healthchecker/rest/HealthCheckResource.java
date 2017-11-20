package de.tdlabs.keycloak.extensions.healthchecker.rest;


import de.tdlabs.keycloak.extensions.healthchecker.model.HealthStatus;
import de.tdlabs.keycloak.extensions.healthchecker.model.HealthState;
import de.tdlabs.keycloak.extensions.healthchecker.spi.HealthIndicator;
import org.keycloak.models.KeycloakSession;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class HealthCheckResource {

  private final KeycloakSession session;

  HealthCheckResource(KeycloakSession session) {
    this.session = session;
  }

  @GET
  @Path("check")
  @Produces(MediaType.APPLICATION_JSON)
  public Response checkHealth() {

    Set<HealthIndicator> healthIndicators = this.session.getAllProviders(HealthIndicator.class);

    HealthStatus health = aggreagte(healthIndicators);

    if (health.isUp()) {
      return Response.ok(health).build();
    }

    return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(health).build();
  }

  private HealthStatus aggreagte(Set<HealthIndicator> healthIndicators) {
    return healthIndicators //
        .stream() //
        .map(HealthIndicator::check) //
        .map(s -> (HealthStatus) s)
        .reduce(HealthCheckResource::combineHealthStatus) //
        .orElseGet(AggregatedHealthStatus::new);
  }

  private static HealthStatus combineHealthStatus(HealthStatus first, HealthStatus second) {

    if (!AggregatedHealthStatus.class.isInstance(first)) {

      AggregatedHealthStatus healthStatus = new AggregatedHealthStatus();
      healthStatus.addHealthInfo(first);
      healthStatus.addHealthInfo(second);

      return healthStatus;
    }

    AggregatedHealthStatus accumulator = (AggregatedHealthStatus) first;
    accumulator.addHealthInfo(second);

    return accumulator;
  }

  private static class AggregatedHealthStatus implements HealthStatus {

    private final List<HealthStatus> healthInfos = new ArrayList<>();

    @Override
    public String getName() {
      return "keycloak";
    }

    @Override
    public HealthState getState() {
      return healthInfos.stream() //
        .map(HealthStatus::getState) //
        .filter(HealthState.DOWN::equals)
        .findAny() //
        .orElse(HealthState.UP) //
        ;
    }

    @Override
    public Map<String, Object> getDetails() {

      Map<String, Object> details = new TreeMap<>();

      for (HealthStatus healthInfo : healthInfos) {
        details.put(healthInfo.getName(), healthInfo.getDetails());
      }

      return details;
    }

    void addHealthInfo(HealthStatus healthInfo) {
      healthInfos.add(healthInfo);
    }
  }
}
