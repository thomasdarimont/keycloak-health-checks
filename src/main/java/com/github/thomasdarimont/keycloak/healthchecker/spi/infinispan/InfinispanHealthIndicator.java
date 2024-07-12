package com.github.thomasdarimont.keycloak.healthchecker.spi.infinispan;

import com.github.thomasdarimont.keycloak.healthchecker.model.HealthStatus;
import com.github.thomasdarimont.keycloak.healthchecker.model.KeycloakHealthStatus;
import com.github.thomasdarimont.keycloak.healthchecker.spi.AbstractHealthIndicator;
import io.quarkus.arc.Arc;
import lombok.extern.jbosslog.JBossLog;
import org.infinispan.health.ClusterHealth;
import org.infinispan.health.Health;
import org.infinispan.manager.EmbeddedCacheManager;
import org.keycloak.Config;
import org.keycloak.quarkus.runtime.storage.legacy.infinispan.CacheManagerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@JBossLog
public class InfinispanHealthIndicator extends AbstractHealthIndicator {

    public static final String KEYCLOAK_CACHE_MANAGER_JNDI_NAME = "java:jboss/infinispan/container/keycloak";

    protected final String jndiName;

    public InfinispanHealthIndicator(Config.Scope config) {
        super("infinispan");
        this.jndiName = config.get("jndiName", KEYCLOAK_CACHE_MANAGER_JNDI_NAME);
    }

    @Override
    public HealthStatus check() {

        Health infinispanHealth = getInfinispanHealth();
        ClusterHealth clusterHealth = infinispanHealth.getClusterHealth();

        KeycloakHealthStatus status = determineClusterHealth(clusterHealth);

        List<Map<Object, Object>> detailedCacheHealthInfo = infinispanHealth.getCacheHealth().stream().map(c -> {
            Map<Object, Object> item = new LinkedHashMap<>();
            item.put("cacheName", c.getCacheName());
            item.put("healthStatus", c.getStatus());
            return item;
        }).collect(Collectors.toList());

        status//
                .withAttribute("hostInfo", infinispanHealth.getHostInfo())
                .withAttribute("clusterName", clusterHealth.getClusterName()) //
                .withAttribute("healthStatus", clusterHealth.getHealthStatus()) //
                .withAttribute("numberOfNodes", clusterHealth.getNumberOfNodes()) //
                .withAttribute("nodeNames", clusterHealth.getNodeNames())
                .withAttribute("cacheDetails", detailedCacheHealthInfo);

        return status;
    }

    protected Health getInfinispanHealth() {
        return lookupCacheManager().getHealth();
    }

    protected EmbeddedCacheManager lookupCacheManager() {

        // Manual lookup via Arc for Quarkus Keycloak
        return Arc.container().instance(CacheManagerFactory.class).get().getOrCreateEmbeddedCacheManager();
    }

    protected KeycloakHealthStatus determineClusterHealth(ClusterHealth clusterHealth) {

        switch (clusterHealth.getHealthStatus()) {
            case HEALTHY:
                return reportUp();
            case HEALTHY_REBALANCING:
                return reportUp();
            case DEGRADED:
            case FAILED:
                return reportDown();
            default:
                return reportDown();
        }
    }
}
