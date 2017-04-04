package de.tdlabs.keycloak.extensions.healthchecker.spi.database;

import de.tdlabs.keycloak.extensions.healthchecker.spi.HealthIndicator;
import de.tdlabs.keycloak.extensions.healthchecker.spi.HealthIndicatorFactory;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;

public class DatabaseHealthIndicatorFactory implements HealthIndicatorFactory {

  public final static String ID = "database-health";

  private Config.Scope config;

  @Override
  public HealthIndicator create(KeycloakSession session) {
    return new DatabaseHealthIndicator(session, config);
  }

  @Override
  public void init(Config.Scope config) {
    this.config = config;
  }

  @Override
  public void postInit(KeycloakSessionFactory factory) {

  }

  @Override
  public void close() {
    //NOOP
  }

  @Override
  public String getId() {
    return ID;
  }
}
