package de.tdlabs.keycloak.extensions.healthchecker.spi.filesystem;

import de.tdlabs.keycloak.extensions.healthchecker.model.HealthStatus;
import de.tdlabs.keycloak.extensions.healthchecker.spi.AbstractHealthIndicator;
import org.keycloak.Config;

import java.io.File;

class FilesystemHealthIndicator extends AbstractHealthIndicator {

  private static final String DEFAULT_PATH = ".";

  private static final long DEFAULT_THRESHOLD_IN_BYTES = 1024 * 1024 * 100; // 100mb

  private final String path;

  private final long thresholdInBytes;

  FilesystemHealthIndicator(Config.Scope config) {
    super("filesystem");
    this.path = config.get("path", DEFAULT_PATH);
    this.thresholdInBytes = config.getLong("thresholdInBytes", DEFAULT_THRESHOLD_IN_BYTES);
  }

  @Override
  public HealthStatus check() {

    File path = new File(this.path);
    long freeBytes = path.getFreeSpace();
    long threshold = thresholdInBytes;

    boolean belowThreshold = freeBytes > threshold;

    return belowThreshold ? reportUp() : reportDown().withAttribute("freebytes", freeBytes);
  }
}
