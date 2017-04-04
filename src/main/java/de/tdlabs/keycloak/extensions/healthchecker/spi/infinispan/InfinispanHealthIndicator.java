package de.tdlabs.keycloak.extensions.healthchecker.spi.infinispan;

import de.tdlabs.keycloak.extensions.healthchecker.model.HealthStatus;
import de.tdlabs.keycloak.extensions.healthchecker.spi.AbstractHealthIndicator;
import org.infinispan.manager.CacheContainer;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;

import javax.naming.InitialContext;

public class InfinispanHealthIndicator extends AbstractHealthIndicator {

  private final KeycloakSession session;
  private final Config.Scope config;

  public InfinispanHealthIndicator(KeycloakSession session, Config.Scope config) {
    super("infinispan");
    this.session = session;
    this.config = config;
  }

  @Override
  public HealthStatus check() {

    try {
      InitialContext initialContext = new InitialContext();
      CacheContainer cacheProvider = (CacheContainer)initialContext.lookup("java:comp/env/infinispan/Keycloak");


    } catch (Exception ex) {
      return reportDown().withAttribute("message", ex.getMessage());
    }


    return reportUp();
  }
}
