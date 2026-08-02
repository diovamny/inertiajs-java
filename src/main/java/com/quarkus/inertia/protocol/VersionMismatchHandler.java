package com.quarkus.inertia.protocol;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import io.smallrye.mutiny.Uni;

import com.quarkus.inertia.version.VersionProvider;

@RequestScoped
public class VersionMismatchHandler {

    private final VersionProvider versionProvider;

    @Inject
    public VersionMismatchHandler(VersionProvider versionProvider) {
        this.versionProvider = versionProvider;
    }

    public Uni<Object> handle(String clientVersion, String currentUrl) {
        var serverVersion = versionProvider.getVersion();
        if (clientVersion != null && !clientVersion.equals(serverVersion)) {
            return Uni.createFrom().item(
                Response.status(Response.Status.CONFLICT)
                    .header("X-Inertia-Location", currentUrl)
                    .header("X-Inertia-Version", serverVersion)
                    .build()
            );
        }
        return Uni.createFrom().item(Response.ok().build());
    }
}
