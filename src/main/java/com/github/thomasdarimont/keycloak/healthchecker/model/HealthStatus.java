package com.github.thomasdarimont.keycloak.healthchecker.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.Map;

public interface HealthStatus {

    String getName();

    HealthState getState();

    Map<String, Object> getDetails();

    @JsonIgnore
    default boolean isUp() {
        return getState() == HealthState.UP;
    }
}
