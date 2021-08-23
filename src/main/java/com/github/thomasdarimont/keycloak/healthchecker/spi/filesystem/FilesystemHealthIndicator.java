package com.github.thomasdarimont.keycloak.healthchecker.spi.filesystem;

import com.github.thomasdarimont.keycloak.healthchecker.model.HealthStatus;
import com.github.thomasdarimont.keycloak.healthchecker.spi.AbstractHealthIndicator;
import org.keycloak.Config;

import java.io.File;

public class FilesystemHealthIndicator extends AbstractHealthIndicator {

    public static final String DEFAULT_PATH = ".";

    public static final long DEFAULT_THRESHOLD_IN_BYTES = 1024 * 1024 * 1024; // 1 GB

    protected final String path;

    protected final long thresholdInBytes;

    public FilesystemHealthIndicator(Config.Scope config) {
        super("filesystem");
        this.path = config.get("path", DEFAULT_PATH);
        this.thresholdInBytes = config.getLong("thresholdInBytes", DEFAULT_THRESHOLD_IN_BYTES);
    }

    @Override
    public HealthStatus check() {

        long freeBytes = getFreeBytesFromStore();

        boolean belowThreshold = freeBytes > thresholdInBytes;

        return (belowThreshold ? reportUp() : reportDown()).withAttribute("freebytes", freeBytes);
    }

    protected long getFreeBytesFromStore() {
        File path = new File(this.path);
        return path.getFreeSpace();
    }
}
