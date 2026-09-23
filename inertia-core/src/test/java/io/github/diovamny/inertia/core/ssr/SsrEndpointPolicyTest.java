package io.github.diovamny.inertia.core.ssr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

class SsrEndpointPolicyTest {

    @Test
    void localEndpointsAreAllowedWithoutRemoteFlag() {
        assertThat(SsrEndpointPolicy.validate("http://localhost:13714/render", false, List.of()))
            .isEqualTo("http://localhost:13714");
        assertThat(SsrEndpointPolicy.validate("http://127.0.0.1:13714", false, List.of()))
            .isEqualTo("http://127.0.0.1:13714");
        assertThat(SsrEndpointPolicy.validate("http://[::1]:13714/render", false, List.of()))
            .isEqualTo("http://[::1]:13714");
    }

    @Test
    void remoteIsRejectedByDefault() {
        assertThatThrownBy(() ->
            SsrEndpointPolicy.validate("https://ssr.example.com/render", false, List.of("ssr.example.com")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("remote-enabled");
    }

    @Test
    void remoteRequiresHttpsAndAllowlistTogether() {
        assertThatThrownBy(() ->
            SsrEndpointPolicy.validate("http://ssr.example.com/render", true, List.of("ssr.example.com")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("https");
        assertThatThrownBy(() ->
            SsrEndpointPolicy.validate("https://evil.example.com/render", true, List.of("ssr.example.com")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("allowed-hosts");
        assertThat(SsrEndpointPolicy.validate("https://SSR.example.com/render", true, List.of("ssr.example.com")))
            .isEqualTo("https://ssr.example.com:443");
    }

    @Test
    void rejectsCredentialsFragmentsAndNonHttpSchemes() {
        assertThatThrownBy(() ->
            SsrEndpointPolicy.validate("http://user:pass@localhost:13714/render", false, List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("userinfo");
        assertThatThrownBy(() ->
            SsrEndpointPolicy.validate("http://localhost:13714/render#frag", false, List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("fragment");
        assertThatThrownBy(() ->
            SsrEndpointPolicy.validate("ftp://localhost/render", false, List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("scheme");
        assertThatThrownBy(() -> SsrEndpointPolicy.validate("  ", false, List.of()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("blank");
    }

    @Test
    void errorMessagesNeverEchoCredentials() {
        assertThatThrownBy(() ->
            SsrEndpointPolicy.validate("http://user:secret@localhost:13714/render", false, List.of()))
            .hasMessageNotContaining("secret");
    }
}
