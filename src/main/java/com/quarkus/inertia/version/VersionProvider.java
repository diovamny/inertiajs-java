package com.quarkus.inertia.version;

public interface VersionProvider {
    String getVersion();

    default void setVersion(String version) {
        // no-op for custom providers; DefaultVersionProvider supports runtime overrides
    }
}
