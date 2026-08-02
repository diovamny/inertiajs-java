package com.quarkus.inertia.protocol;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.quarkus.inertia.version.VersionProvider;

class VersionMismatchHandlerUnitTest {

    @Test
    void shouldReturnOkWhenVersionsMatch() {
        var provider = new VersionProvider() {
            @Override public String getVersion() { return "v1"; }
        };
        var handler = new VersionMismatchHandler(provider);
        var result = handler.handle("v1", "/home");
        var response = (jakarta.ws.rs.core.Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    void shouldReturn409WhenVersionsDontMatch() {
        var provider = new VersionProvider() {
            @Override public String getVersion() { return "v2"; }
        };
        var handler = new VersionMismatchHandler(provider);
        var result = handler.handle("v1", "/home");
        var response = (jakarta.ws.rs.core.Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(409);
        assertThat(response.getHeaderString("X-Inertia-Location")).isEqualTo("/home");
    }

    @Test
    void shouldReturnOkWhenClientVersionIsNull() {
        var provider = new VersionProvider() {
            @Override public String getVersion() { return "v2"; }
        };
        var handler = new VersionMismatchHandler(provider);
        var result = handler.handle(null, "/home");
        var response = (jakarta.ws.rs.core.Response) result.await().indefinitely();
        assertThat(response.getStatus()).isEqualTo(200);
    }
}
