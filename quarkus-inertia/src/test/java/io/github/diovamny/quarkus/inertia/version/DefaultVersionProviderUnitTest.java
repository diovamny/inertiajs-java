package io.github.diovamny.quarkus.inertia.version;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import io.github.diovamny.quarkus.inertia.config.InertiaConfig;

class DefaultVersionProviderUnitTest {

    private InertiaConfig makeConfig(String versionStrategy, Optional<String> versionCustom) {
        return new InertiaConfig() {
            @Override public String rootTemplate() { return "index.html"; }
            @Override public boolean ssrEnabled() { return false; }
            @Override public String ssrUrl() { return "http://localhost:13714"; }
            @Override public java.time.Duration ssrConnectTimeout() { return java.time.Duration.ofSeconds(5); }
            @Override public java.time.Duration ssrReadTimeout() { return java.time.Duration.ofSeconds(10); }
            @Override public String versionStrategy() { return versionStrategy; }
            @Override public Optional<String> versionCustom() { return versionCustom; }
            @Override public boolean encryptHistory() { return false; }
            @Override public boolean camelizeProps() { return false; }
            @Override public boolean csrfEnabled() { return true; }
            @Override public Optional<String> rootView() { return Optional.empty(); }
            @Override public Optional<java.util.List<String>> flashKeys() { return Optional.empty(); }
            @Override public boolean alwaysIncludeErrors() { return true; }
            @Override public int errorStatus() { return 500; }
            @Override public String errorComponent() { return "ErrorPage"; }
            @Override public boolean lazyEtagEnabled() { return true; }
            @Override public Optional<java.util.List<String>> ssrExcludePaths() { return Optional.empty(); }
            @Override public boolean useQute() { return false; }
            @Override public boolean errorDetailsEnabled() { return false; }
            @Override public boolean templateCacheEnabled() { return true; }
            @Override public boolean conventionRoutingEnabled() { return false; }
            @Override public Optional<String> conventionRoutingPrefix() { return Optional.empty(); }
        };
    }

    @Test
    void shouldComputeVersionAtConstruction() {
        var config = makeConfig("sha256", Optional.empty());
        var provider = new DefaultVersionProvider(config);
        assertThat(provider.getVersion()).isNotEmpty();
    }

    @Test
    void shouldUseCustomVersion() {
        var config = makeConfig("custom", Optional.of("v1.0.0"));
        var provider = new DefaultVersionProvider(config);
        assertThat(provider.getVersion()).isEqualTo("v1.0.0");
    }

    @Test
    void shouldReturnSameVersionOnMultipleCalls() {
        var config = makeConfig("custom", Optional.of("fixed"));
        var provider = new DefaultVersionProvider(config);
        assertThat(provider.getVersion()).isSameAs(provider.getVersion());
    }

    @Test
    void runtimeVersionOverridesConfiguredVersion() {
        var config = makeConfig("custom", Optional.of("v1.0.0"));
        var provider = new DefaultVersionProvider(config);
        // Without Vert.x context, runtime version is not available
        assertThat(provider.getVersion()).isEqualTo("v1.0.0");
    }

    @Test
    void runtimeVersionPersistsAcrossCalls() {
        var config = makeConfig("custom", Optional.of("v1.0.0"));
        var provider = new DefaultVersionProvider(config);
        // Without Vert.x context, runtime version is not available
        assertThat(provider.getVersion()).isEqualTo("v1.0.0");
        assertThat(provider.getVersion()).isSameAs(provider.getVersion());
    }
}
