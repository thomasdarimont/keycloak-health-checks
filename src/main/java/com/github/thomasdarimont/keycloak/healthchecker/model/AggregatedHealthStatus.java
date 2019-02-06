package com.github.thomasdarimont.keycloak.healthchecker.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AggregatedHealthStatus implements HealthStatus {

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

        Map<String, Object> details = new HashMap<>(healthInfos.size());

        for (HealthStatus healthInfo : healthInfos) {
            details.put(healthInfo.getName(), healthInfo.getDetails());
        }

        return details;
    }

    public void addHealthInfo(HealthStatus healthInfo) {
        healthInfos.add(healthInfo);
    }


//  public static void main(String[] args) throws Exception {
//
//    List<HealthStatus> hs = Arrays.asList( //
//      KeycloakHealthStatus.reportUp("status1") //
//      , KeycloakHealthStatus.reportDown("status2").withAttribute("message", "oh oh") //
//      , KeycloakHealthStatus.reportUp("status3") //
//    );
//
//    HealthStatus status = hs.stream() //
//      .reduce(HealthCheckResource::combineHealthStatus) //
//      .orElseGet(() -> KeycloakHealthStatus.reportUp("health"));
//
//    ObjectMapper om = new ObjectMapper();
//    om.enable(SerializationFeature.INDENT_OUTPUT);
//    System.out.println(om.writeValueAsString(status));
//  }
}