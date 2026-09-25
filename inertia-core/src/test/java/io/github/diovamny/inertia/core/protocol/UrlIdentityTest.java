package io.github.diovamny.inertia.core.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UrlIdentityTest {

    @ParameterizedTest
    @ValueSource(strings = {"/dash", "/", "/a/b?x=1"})
    void relativePathsAreInternal(String location) {
        assertThat(UrlIdentity.isExternal(location, "https", "app.test")).isFalse();
    }

    @Test
    void blankOrNullLocationIsInternal() {
        assertThat(UrlIdentity.isExternal(null, "https", "app.test")).isFalse();
        assertThat(UrlIdentity.isExternal("  ", "https", "app.test")).isFalse();
    }

    @Test
    void sameOriginIsInternal() {
        assertThat(UrlIdentity.isExternal("https://app.test/dash", "https", "app.test")).isFalse();
        assertThat(UrlIdentity.isExternal("https://app.test:8443/dash", "https", "app.test:8443")).isFalse();
    }

    @Test
    void schemeAndHostCompareCaseInsensitively() {
        assertThat(UrlIdentity.isExternal("HTTPS://APP.TEST/dash", "https", "app.test")).isFalse();
        assertThat(UrlIdentity.isExternal("https://APP.test/dash", "https", "app.test")).isFalse();
    }

    @Test
    void defaultPortsNormalize() {
        assertThat(UrlIdentity.isExternal("http://app.test:80/x", "http", "app.test")).isFalse();
        assertThat(UrlIdentity.isExternal("http://app.test/x", "http", "app.test:80")).isFalse();
        assertThat(UrlIdentity.isExternal("https://app.test:443/x", "https", "app.test")).isFalse();
    }

    @Test
    void differentHostSchemeOrPortIsExternal() {
        assertThat(UrlIdentity.isExternal("https://other.test/", "https", "app.test")).isTrue();
        assertThat(UrlIdentity.isExternal("http://app.test/", "https", "app.test")).isTrue();
        assertThat(UrlIdentity.isExternal("https://app.test:8443/", "https", "app.test")).isTrue();
        assertThat(UrlIdentity.isExternal("https://app.test/", "https", "app.test:8443")).isTrue();
    }

    @Test
    void unparseableAndOpaqueTargetsFailClosed() {
        assertThat(UrlIdentity.isExternal("https://", "https", "app.test")).isTrue();
        assertThat(UrlIdentity.isExternal("mailto:user@app.test", "https", "app.test")).isTrue();
        assertThat(UrlIdentity.isExternal("https://app.test/", null, null)).isTrue();
    }

    @Test
    void protocolRelativePathsStayInternalAsBefore() {
        // Historical quirk, preserved: no scheme means internal.
        assertThat(UrlIdentity.isExternal("//other.test/x", "https", "app.test")).isFalse();
    }

    @Test
    void ipv6AuthoritiesCompare() {
        assertThat(UrlIdentity.isExternal("http://[::1]/x", "http", "[::1]")).isFalse();
        assertThat(UrlIdentity.isExternal("http://[::1]:8080/x", "http", "[::1]:8080")).isFalse();
        assertThat(UrlIdentity.isExternal("http://[::1]:9090/x", "http", "[::1]:8080")).isTrue();
    }
}
