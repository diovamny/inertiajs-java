package io.github.diovamny.quarkus.inertia.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.jupiter.api.Test;

class InertiaConfigUnitTest {

    private InertiaConfig makeConfig(String rootTemplate, String versionStrategy,
            Optional<String> versionCustom, boolean encryptHistory, boolean clearHistory,
            Optional<String> rootView, boolean camelizeProps, String csrfRefreshPolicy) {
        return new InertiaConfig() {
            @Override public String rootTemplate() { return rootTemplate; }
            @Override public boolean ssrEnabled() { return false; }
            @Override public String ssrUrl() { return "http://localhost:13714"; }
            @Override public java.time.Duration ssrConnectTimeout() { return java.time.Duration.ofSeconds(5); }
            @Override public java.time.Duration ssrReadTimeout() { return java.time.Duration.ofSeconds(10); }
            @Override public int ssrBreakerFailureThreshold() { return 5; }
            @Override public java.time.Duration ssrBreakerCooldown() { return java.time.Duration.ofSeconds(30); }
            @Override public boolean ssrCacheEnabled() { return false; }
            @Override public java.time.Duration ssrCacheTtl() { return java.time.Duration.ofMinutes(15); }
            @Override public boolean ssrSupervisorEnabled() { return false; }
            @Override public String ssrSupervisorCommand() { return "node"; }
            @Override public String ssrSupervisorEntry() { return "dist-ssr/ssr.mjs"; }
            @Override public Optional<String> ssrSupervisorWorkdir() { return Optional.empty(); }
            @Override public int ssrSupervisorMaxRestarts() { return 5; }
            @Override public String versionStrategy() { return versionStrategy; }
            @Override public Optional<String> versionCustom() { return versionCustom; }
            @Override public boolean encryptHistory() { return encryptHistory; }
            @Override public boolean clearHistory() { return clearHistory; }
            @Override public String csrfRefreshPolicy() { return csrfRefreshPolicy; }
            @Override public boolean serverHead() { return false; }
            @Override public String metaTitleTemplate() { return "%s"; }
            @Override public boolean camelizeProps() { return camelizeProps; }
            @Override public boolean csrfEnabled() { return true; }
            @Override public Optional<String> rootView() { return rootView; }
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
            @Override public Optional<String> securityMode() { return Optional.empty(); }
            @Override public boolean securityFailOnFallback() { return false; }
            @Override public boolean securityAllowDisabledInProduction() { return false; }
            @Override public String securityLoginUrl() { return "/login"; }
            @Override public String securityForbiddenComponent() { return "Errors/Forbidden"; }
            @Override public String securityCsrfFailurePath() { return "/"; }
            @Override public String securityCsrfFlashKey() { return "error"; }
            @Override public String securityCsrfFlashMessage() { return "La página expiró. Vuelve a intentarlo."; }
            @Override public String securityCookieSameSite() { return "Lax"; }
            @Override public boolean securityCookieSecure() { return false; }
            @Override public String securityCookiePath() { return "/"; }
            @Override public Optional<String> securityCookieDomain() { return Optional.empty(); }
            @Override public Optional<java.util.List<String>> securityReactiveCsrfPaths() { return Optional.empty(); }
            @Override public boolean authPropsEnabled() { return false; }
        };
    }

    private InertiaConfig defaultConfig() {
        return makeConfig("index.html", "sha256", Optional.empty(), false, false,
            Optional.empty(), false, "always");
    }

    @Test
    void shouldRejectBlankRootTemplate() {
        var config = makeConfig("", "sha256", Optional.empty(), false, false,
            Optional.empty(), false, "always");
        assertThatThrownBy(() -> new InertiaConfigValidator(config).onStart(null))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("root-template");
    }

    @Test
    void shouldRejectInvalidVersionStrategy() {
        var config = makeConfig("index.html", "invalid", Optional.empty(), false, false,
            Optional.empty(), false, "always");
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
        var config = makeConfig("index.html", "custom", Optional.of("v1.0.0"), false, false,
            Optional.empty(), false, "always");
        assertThatNoException().isThrownBy(() -> new InertiaConfigValidator(config).onStart(null));
    }

    @Test
    void shouldRejectInvalidCsrfRefreshPolicy() {
        var config = makeConfig("index.html", "sha256", Optional.empty(), false, false,
            Optional.empty(), false, "sometimes");
        assertThatThrownBy(() -> new InertiaConfigValidator(config).onStart(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("csrf-refresh-policy");
    }

    @Test
    void shouldAcceptLazyCsrfRefreshPolicy() {
        var config = makeConfig("index.html", "sha256", Optional.empty(), false, false,
            Optional.empty(), false, "lazy");
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
