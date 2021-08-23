package com.github.thomasdarimont.keycloak.healthchecker.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

        Map<String, Object> details = new LinkedHashMap<>(healthInfos.size());

        for (HealthStatus healthInfo : healthInfos) {
            details.put(healthInfo.getName(), healthInfo.getDetails());
        }

        return details;
    }

    public void addHealthInfo(HealthStatus healthInfo) {
        healthInfos.add(healthInfo);
    }
}