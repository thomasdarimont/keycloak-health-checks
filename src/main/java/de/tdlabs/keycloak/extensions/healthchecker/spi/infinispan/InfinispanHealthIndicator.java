package de.tdlabs.keycloak.extensions.healthchecker.spi.infinispan;

import de.tdlabs.keycloak.extensions.healthchecker.spi.AbstractHealthIndicator;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import javax.naming.InitialContext;
import org.infinispan.manager.EmbeddedCacheManager;

class InfinispanHealthIndicator extends AbstractHealthIndicator {

  private final KeycloakSession session;
  private final String jndiName;

  InfinispanHealthIndicator(KeycloakSession session, Config.Scope config) {
    super("infinispan");
    this.session = session;
    this.jndiName = config.get("jndiName", "java:jboss/infinispan/container/keycloak");
  }

  @Override
  public de.tdlabs.keycloak.extensions.healthchecker.model.HealthStatus check() {

    try {
      InitialContext ic = new InitialContext();
      EmbeddedCacheManager manager = (EmbeddedCacheManager) ic.lookup(jndiName);
      if (manager.getHealth().getCacheHealth().contains(org.infinispan.health.HealthStatus.UNHEALTHY)) {
        return reportDown();
      } else {
        return reportUp();
      }
    } catch (Exception ex) {
      return reportDown().withAttribute("message", ex.getMessage());
    }
  }
}
