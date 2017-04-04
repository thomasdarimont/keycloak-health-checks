package de.tdlabs.keycloak.extensions.healthchecker.spi;

import de.tdlabs.keycloak.extensions.healthchecker.model.HealthStatus;
import de.tdlabs.keycloak.extensions.healthchecker.model.KeycloakHealthStatus;

public abstract class AbstractHealthIndicator implements HealthIndicator {

  private final String name;

  public AbstractHealthIndicator(String name) {
    this.name = name;
  }

  protected KeycloakHealthStatus reportUp(){
    return KeycloakHealthStatus.up(getName());
  }

  protected KeycloakHealthStatus reportDown(){
    return KeycloakHealthStatus.down(getName());
  }


  public abstract HealthStatus check();

  @Override
  public void close() {
    //NOOP
  }

  public String getName() {
    return name;
  }
}
