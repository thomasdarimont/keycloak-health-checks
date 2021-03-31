package com.github.thomasdarimont.keycloak.healthchecker.spi.infinispan;

import com.github.thomasdarimont.keycloak.healthchecker.model.HealthStatus;
import com.github.thomasdarimont.keycloak.healthchecker.model.KeycloakHealthStatus;
import com.github.thomasdarimont.keycloak.healthchecker.spi.AbstractHealthIndicator;
import lombok.extern.jbosslog.JBossLog;
import org.infinispan.health.ClusterHealth;
import org.infinispan.health.Health;
import org.infinispan.manager.EmbeddedCacheManager;
import org.keycloak.Config;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JBossLog
public class InfinispanHealthIndicator extends AbstractHealthIndicator {

    private static final String KEYCLOAK_CACHE_MANAGER_JNDI_NAME = "java:jboss/infinispan/container/keycloak";

    InfinispanHealthIndicator(Config.Scope config) {
        super("infinispan");
    }

    @Override
    public HealthStatus check() {

        Health infinispanHealth = lookupCacheManager().getHealth();
        ClusterHealth clusterHealth = infinispanHealth.getClusterHealth();

        KeycloakHealthStatus status = determineClusterHealth(clusterHealth);

        List<Map<Object, Object>> detailedCacheHealthInfo = infinispanHealth.getCacheHealth().stream().map(c -> {
            Map<Object, Object> item = new LinkedHashMap<>();
            item.put("cacheName", c.getCacheName());
            item.put("healthStatus", c.getStatus());
            return item;
        }).collect(Collectors.toList());

        status//
                .withAttribute("clusterName", clusterHealth.getClusterName()) //
                .withAttribute("healthStatus", clusterHealth.getHealthStatus()) //
                .withAttribute("numberOfNodes", clusterHealth.getNumberOfNodes()) //
                .withAttribute("nodeNames", clusterHealth.getNodeNames())
                .withAttribute("cacheDetails", detailedCacheHealthInfo)
        ;

        return status;
    }

    private EmbeddedCacheManager lookupCacheManager() {
        try {
            return (EmbeddedCacheManager) new InitialContext().lookup(KEYCLOAK_CACHE_MANAGER_JNDI_NAME);
        } catch (NamingException e) {
            log.warnv("Could not find EmbeddedCacheManager with name: {0}", KEYCLOAK_CACHE_MANAGER_JNDI_NAME);
            throw new RuntimeException(e);
        }
    }

    private KeycloakHealthStatus determineClusterHealth(ClusterHealth clusterHealth) {

        switch (clusterHealth.getHealthStatus()) {
            case HEALTHY:
            case HEALTHY_REBALANCING:
                return reportUp();
            default:
                return reportDown();
        }
    }
}
