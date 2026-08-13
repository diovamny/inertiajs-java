package com.quarkus.inertia.config;

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class InertiaConfigUnitTest {

    private InertiaConfig makeConfig(String rootTemplate, String versionStrategy,
            Optional<String> versionCustom, boolean encryptHistory,
            Optional<String> rootView, boolean camelizeProps) {
        return new InertiaConfig() {
            @Override public String rootTemplate() { return rootTemplate; }
            @Override public boolean ssrEnabled() { return false; }
            @Override public String ssrUrl() { return "http://localhost:13714"; }
            @Override public String versionStrategy() { return versionStrategy; }
            @Override public Optional<String> versionCustom() { return versionCustom; }
            @Override public boolean encryptHistory() { return encryptHistory; }
            @Override public boolean camelizeProps() { return camelizeProps; }
            @Override public boolean csrfEnabled() { return true; }
            @Override public Optional<String> rootView() { return rootView; }
            @Override public Optional<java.util.List<String>> flashKeys() { return Optional.empty(); }
            @Override public boolean alwaysIncludeErrors() { return true; }
            @Override public int errorStatus() { return 500; }
            @Override public String errorComponent() { return "ErrorPage"; }
            @Override public boolean lazyEtagEnabled() { return true; }
            @Override public Optional<java.util.List<String>> ssrExcludePaths() { return Optional.empty(); }
        };
    }

    private InertiaConfig defaultConfig() {
        return makeConfig("index.html", "sha256", Optional.empty(), false, Optional.empty(), false);
    }

    @Test
    void shouldRejectBlankRootTemplate() {
        var config = makeConfig("", "sha256", Optional.empty(), false, Optional.empty(), false);
        assertThatThrownBy(() -> new InertiaConfigValidator(config).onStart(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("root-template");
    }

    @Test
    void shouldRejectInvalidVersionStrategy() {
        var config = makeConfig("index.html", "invalid", Optional.empty(), false, Optional.empty(), false);
        assertThatThrownBy(() -> new InertiaConfigValidator(config).onStart(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("version-strategy");
    }

    @Test
    void shouldAcceptValidConfig() {
        var config = defaultConfig();
        assertThatNoException().isThrownBy(() -> new InertiaConfigValidator(config).onStart(null));
    }

    @Test
    void shouldAcceptCustomVersionStrategy() {
        var config = makeConfig("index.html", "custom", Optional.of("v1.0.0"), false, Optional.empty(), false);
        assertThatNoException().isThrownBy(() -> new InertiaConfigValidator(config).onStart(null));
    }

    @Test
    void shouldUseDefaultValues() {
        var config = defaultConfig();
        assertThat(config.rootTemplate()).isEqualTo("index.html");
        assertThat(config.ssrEnabled()).isFalse();
        assertThat(config.ssrUrl()).isEqualTo("http://localhost:13714");
        assertThat(config.versionStrategy()).isEqualTo("sha256");
        assertThat(config.errorStatus()).isEqualTo(500);
        assertThat(config.errorComponent()).isEqualTo("ErrorPage");
        assertThat(config.alwaysIncludeErrors()).isTrue();
    }
}
