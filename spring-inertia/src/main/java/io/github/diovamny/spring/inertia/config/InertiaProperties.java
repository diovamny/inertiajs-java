package io.github.diovamny.spring.inertia.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration surface of the adapter, prefix {@code inertia}.
 *
 * <pre>{@code
 * inertia:
 *   root-template: index.html
 *   template-cache-enabled: true
 *   ssr-enabled: false
 *   ssr-url: http://localhost:13714/render
 *   ssr-exclude-paths: []
 *   ssr-connect-timeout: 5s
 *   ssr-read-timeout: 10s
 *   version-strategy: sha256
 *   version-custom: null
 *   encrypt-history: false
 *   camelize-props: false
 *   csrf-enabled: true (deprecated alias, see security.mode)
 *   security.mode: auto
 *   security.fail-on-fallback: false
 *   security.allow-disabled-in-production: false
 *   security.login-url: /login
 *   security.forbidden-component: Errors/Forbidden
 *   security.csrf-failure-path: /
 *   security.csrf-flash-key: error
 *   security.csrf-flash-message: ...
 *   security.cookie-same-site: Lax
 *   security.cookie-secure: false
 *   security.cookie-path: /
 *   security.cookie-domain: ""
 *   flash-keys: []
 *   always-include-errors: true
 *   error-status: 500
 *   error-component: ErrorPage
 *   max-page-bytes: 33554432
 *   lazy-etag-enabled: true
 *   root-view: null
 *   convention-routing-enabled: false
 *   convention-routing-prefix: ""
 * }</pre>
 */
@ConfigurationProperties(prefix = "inertia")
public class InertiaProperties {

    /** Name of the root HTML template resolved from the classpath. */
    private String rootTemplate = "index.html";

    /** Whether the root HTML template is cached in memory (disable in dev for live reload). */
    private boolean templateCacheEnabled = true;

    /** Whether server-side rendering (SSR) is enabled. */
    private boolean ssrEnabled = false;

    /** URL of the SSR server endpoint. */
    private String ssrUrl = "http://localhost:13714/render";

    /** Paths excluded from SSR. */
    private List<String> ssrExcludePaths = List.of();

    /** Connection timeout for SSR requests. */
    private Duration ssrConnectTimeout = Duration.ofSeconds(5);

    /** Read timeout for SSR requests. */
    private Duration ssrReadTimeout = Duration.ofSeconds(10);

    /** Consecutive SSR sidecar failures before the circuit breaker opens. */
    private int ssrBreakerFailureThreshold = 5;

    /** Cooldown before a half-open probe is allowed through an open breaker. */
    private Duration ssrBreakerCooldown = Duration.ofSeconds(30);

    /** Cache SSR sidecar responses keyed by page hash. */
    private boolean ssrCacheEnabled = false;

    /** Default TTL for cached SSR responses (overridable per render). */
    private Duration ssrCacheTtl = Duration.ofMinutes(15);

    /** Supervise the Node.js SSR sidecar process (start, restart, stop). */
    private boolean ssrSupervisorEnabled = false;

    /** Sidecar executable for the supervisor. */
    private String ssrSupervisorCommand = "node";

    /** Sidecar entry file for the supervisor. */
    private String ssrSupervisorEntry = "dist-ssr/ssr.mjs";

    /** Working directory for the supervised sidecar (blank = JVM directory). */
    private String ssrSupervisorWorkdir = "";

    /** Restart attempts after the initial start before giving up. */
    private int ssrSupervisorMaxRestarts = 5;

    /** Version strategy: {@code sha256} (default) or {@code vite-manifest}. */
    private String versionStrategy = "sha256";

    /** Fixed version; when set it wins over the strategy. */
    private String versionCustom;

    /** Whether the client must encrypt history state. */
    private boolean encryptHistory = false;

    /** Whether the client must clear history on the next visit. */
    private boolean clearHistory = false;

    /** Whether prop keys are converted from snake_case to camelCase. */
    private boolean camelizeProps = false;

    /**
     * XSRF-TOKEN cookie refresh policy (Rails {@code :always} / {@code :lazy}
     * parity): {@code always} re-emits {@code Set-Cookie} on every response;
     * {@code lazy} skips re-emission on idempotent requests that already
     * present a valid token, keeping responses cacheable by CDNs.
     */
    private String csrfRefreshPolicy = "always";

    /**
     * Legacy CSRF flag, kept as a migration alias for
     * {@code inertia.security.mode} ({@code true} maps to {@code adapter},
     * {@code false} maps to {@code disabled}) and only honored when
     * {@code inertia.security.mode} is unset.
     *
     * @deprecated use {@code inertia.security.mode} instead
     */
    @Deprecated
    private Boolean csrfEnabled;

    /** Security integration settings (CSRF ownership, guardrails, cookies). */
    private final Security security = new Security();

    /**
     * Security integration settings, prefix {@code inertia.security}.
     */
    public static class Security {

        /**
         * CSRF ownership: {@code auto} (recommended), {@code framework},
         * {@code adapter} or {@code disabled}.
         */
        private String mode;

        /** Fail startup when {@code auto} resolves to the adapter fallback. */
        private boolean failOnFallback = false;

        /** Allow {@code disabled} in the production profile (default refuses). */
        private boolean allowDisabledInProduction = false;

        /** Login/OIDC URL used for {@code 409 + X-Inertia-Location} challenges. */
        private String loginUrl = "/login";

        /** Inertia component rendered for {@code 403} pages. */
        private String forbiddenComponent = "Errors/Forbidden";

        /** Safe fallback path for CSRF-failure redirects (default {@code /}). */
        private String csrfFailurePath = "/";

        /** Flash key carrying the CSRF-expiry message. */
        private String csrfFlashKey = "error";

        /** Generic CSRF-expiry message (no internals, safe to display). */
        private String csrfFlashMessage = "La página expiró. Vuelve a intentarlo.";

        /** {@code SameSite} attribute of the XSRF-TOKEN cookie. */
        private String cookieSameSite = "Lax";

        /** {@code Secure} attribute of the XSRF-TOKEN cookie (enable on HTTPS). */
        private boolean cookieSecure = false;

        /** {@code Path} attribute of the XSRF-TOKEN cookie. */
        private String cookiePath = "/";

        /** {@code Domain} attribute of the XSRF-TOKEN cookie (empty = host-only). */
        private String cookieDomain = "";

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public boolean isFailOnFallback() {
            return failOnFallback;
        }

        public void setFailOnFallback(boolean failOnFallback) {
            this.failOnFallback = failOnFallback;
        }

        public boolean isAllowDisabledInProduction() {
            return allowDisabledInProduction;
        }

        public void setAllowDisabledInProduction(boolean allowDisabledInProduction) {
            this.allowDisabledInProduction = allowDisabledInProduction;
        }

        public String getLoginUrl() {
            return loginUrl;
        }

        public void setLoginUrl(String loginUrl) {
            this.loginUrl = loginUrl;
        }

        public String getForbiddenComponent() {
            return forbiddenComponent;
        }

        public void setForbiddenComponent(String forbiddenComponent) {
            this.forbiddenComponent = forbiddenComponent;
        }

        public String getCsrfFailurePath() {
            return csrfFailurePath;
        }

        public void setCsrfFailurePath(String csrfFailurePath) {
            this.csrfFailurePath = csrfFailurePath;
        }

        public String getCsrfFlashKey() {
            return csrfFlashKey;
        }

        public void setCsrfFlashKey(String csrfFlashKey) {
            this.csrfFlashKey = csrfFlashKey;
        }

        public String getCsrfFlashMessage() {
            return csrfFlashMessage;
        }

        public void setCsrfFlashMessage(String csrfFlashMessage) {
            this.csrfFlashMessage = csrfFlashMessage;
        }

        public String getCookieSameSite() {
            return cookieSameSite;
        }

        public void setCookieSameSite(String cookieSameSite) {
            this.cookieSameSite = cookieSameSite;
        }

        public boolean isCookieSecure() {
            return cookieSecure;
        }

        public void setCookieSecure(boolean cookieSecure) {
            this.cookieSecure = cookieSecure;
        }

        public String getCookiePath() {
            return cookiePath;
        }

        public void setCookiePath(String cookiePath) {
            this.cookiePath = cookiePath;
        }

        public String getCookieDomain() {
            return cookieDomain;
        }

        public void setCookieDomain(String cookieDomain) {
            this.cookieDomain = cookieDomain;
        }
    }

    public Security getSecurity() {
        return security;
    }

    /** Additional session keys that must be flashed with every page. */
    private List<String> flashKeys = List.of();

    /** Whether validation errors are always injected into the props. */
    private boolean alwaysIncludeErrors = true;

    /** Validation semantics (prefix {@code inertia.validation}). */
    private Validation validation = new Validation();

    /** Validation semantics. */
    public static class Validation {
        /**
         * Emit arrays of messages per field ({@code Map<field, List<message>>}).
         * Default {@code false} preserves the legacy {@code Map<field, message>} wire.
         */
        private boolean allErrors = false;

        public boolean isAllErrors() {
            return allErrors;
        }

        public void setAllErrors(boolean allErrors) {
            this.allErrors = allErrors;
        }
    }

    public Validation getValidation() {
        return validation;
    }

    public void setValidation(Validation validation) {
        this.validation = validation;
    }

    /** HTTP status used for error pages. */
    private int errorStatus = 500;

    /** Component used for error pages. */
    private String errorComponent = "ErrorPage";

    /** Max serialized page size in bytes (413 beyond it; negative disables). */
    private long maxPageBytes = 33554432L;

    /** Whether to include exception details in error responses. */
    private boolean errorDetailsEnabled = false;

    /** Whether the ETag / 304 handling is active. */
    private boolean lazyEtagEnabled = true;

    /** Optional explicit view name (template) for the root view. */
    private String rootView;

    /** Whether auto-resolving component names by convention is enabled. */
    private boolean conventionRoutingEnabled = false;

    /** Optional prefix prepended to auto-resolved component names. */
    private String conventionRoutingPrefix = "";

    public String getRootTemplate() {
        return rootTemplate;
    }

    public void setRootTemplate(String rootTemplate) {
        this.rootTemplate = rootTemplate;
    }

    public boolean isTemplateCacheEnabled() {
        return templateCacheEnabled;
    }

    public void setTemplateCacheEnabled(boolean templateCacheEnabled) {
        this.templateCacheEnabled = templateCacheEnabled;
    }

    public boolean isSsrEnabled() {
        return ssrEnabled;
    }

    public void setSsrEnabled(boolean ssrEnabled) {
        this.ssrEnabled = ssrEnabled;
    }

    public String getSsrUrl() {
        return ssrUrl;
    }

    public void setSsrUrl(String ssrUrl) {
        this.ssrUrl = ssrUrl;
    }

    public List<String> getSsrExcludePaths() {
        return ssrExcludePaths;
    }

    public void setSsrExcludePaths(List<String> ssrExcludePaths) {
        this.ssrExcludePaths = ssrExcludePaths;
    }

    public Duration getSsrConnectTimeout() {
        return ssrConnectTimeout;
    }

    public void setSsrConnectTimeout(Duration ssrConnectTimeout) {
        this.ssrConnectTimeout = ssrConnectTimeout;
    }

    public Duration getSsrReadTimeout() {
        return ssrReadTimeout;
    }

    public void setSsrReadTimeout(Duration ssrReadTimeout) {
        this.ssrReadTimeout = ssrReadTimeout;
    }

    public int getSsrBreakerFailureThreshold() {
        return ssrBreakerFailureThreshold;
    }

    public void setSsrBreakerFailureThreshold(int ssrBreakerFailureThreshold) {
        this.ssrBreakerFailureThreshold = ssrBreakerFailureThreshold;
    }

    public Duration getSsrBreakerCooldown() {
        return ssrBreakerCooldown;
    }

    public void setSsrBreakerCooldown(Duration ssrBreakerCooldown) {
        this.ssrBreakerCooldown = ssrBreakerCooldown;
    }

    public boolean isSsrCacheEnabled() {
        return ssrCacheEnabled;
    }

    public void setSsrCacheEnabled(boolean ssrCacheEnabled) {
        this.ssrCacheEnabled = ssrCacheEnabled;
    }

    public Duration getSsrCacheTtl() {
        return ssrCacheTtl;
    }

    public void setSsrCacheTtl(Duration ssrCacheTtl) {
        this.ssrCacheTtl = ssrCacheTtl;
    }

    public boolean isSsrSupervisorEnabled() {
        return ssrSupervisorEnabled;
    }

    public void setSsrSupervisorEnabled(boolean ssrSupervisorEnabled) {
        this.ssrSupervisorEnabled = ssrSupervisorEnabled;
    }

    public String getSsrSupervisorCommand() {
        return ssrSupervisorCommand;
    }

    public void setSsrSupervisorCommand(String ssrSupervisorCommand) {
        this.ssrSupervisorCommand = ssrSupervisorCommand;
    }

    public String getSsrSupervisorEntry() {
        return ssrSupervisorEntry;
    }

    public void setSsrSupervisorEntry(String ssrSupervisorEntry) {
        this.ssrSupervisorEntry = ssrSupervisorEntry;
    }

    public String getSsrSupervisorWorkdir() {
        return ssrSupervisorWorkdir;
    }

    public void setSsrSupervisorWorkdir(String ssrSupervisorWorkdir) {
        this.ssrSupervisorWorkdir = ssrSupervisorWorkdir;
    }

    public int getSsrSupervisorMaxRestarts() {
        return ssrSupervisorMaxRestarts;
    }

    public void setSsrSupervisorMaxRestarts(int ssrSupervisorMaxRestarts) {
        this.ssrSupervisorMaxRestarts = ssrSupervisorMaxRestarts;
    }

    public String getVersionStrategy() {
        return versionStrategy;
    }

    public void setVersionStrategy(String versionStrategy) {
        this.versionStrategy = versionStrategy;
    }

    public String getVersionCustom() {
        return versionCustom;
    }

    public void setVersionCustom(String versionCustom) {
        this.versionCustom = versionCustom;
    }

    public boolean isEncryptHistory() {
        return encryptHistory;
    }

    public void setEncryptHistory(boolean encryptHistory) {
        this.encryptHistory = encryptHistory;
    }

    public boolean isClearHistory() {
        return clearHistory;
    }

    public void setClearHistory(boolean clearHistory) {
        this.clearHistory = clearHistory;
    }

    /**
     * Publish collected server head tags as the {@code head} page prop.
     */
    private boolean serverHead = false;

    /**
     * Title template applied by the head builder ({@code %s} is the title).
     */
    private String metaTitleTemplate = "%s";

    public String getCsrfRefreshPolicy() {
        return csrfRefreshPolicy;
    }

    public void setCsrfRefreshPolicy(String csrfRefreshPolicy) {
        this.csrfRefreshPolicy = csrfRefreshPolicy;
    }

    public boolean isServerHead() {
        return serverHead;
    }

    public void setServerHead(boolean serverHead) {
        this.serverHead = serverHead;
    }

    public String getMetaTitleTemplate() {
        return metaTitleTemplate;
    }

    public void setMetaTitleTemplate(String metaTitleTemplate) {
        this.metaTitleTemplate = metaTitleTemplate;
    }

    public boolean isCamelizeProps() {
        return camelizeProps;
    }

    public void setCamelizeProps(boolean camelizeProps) {
        this.camelizeProps = camelizeProps;
    }

    /**
     * Legacy CSRF flag (migration alias for {@code inertia.security.mode}).
     *
     * @return the legacy flag, or {@code null} when unset
     * @deprecated use {@code inertia.security.mode} instead
     */
    @Deprecated
    public Boolean getCsrfEnabled() {
        return csrfEnabled;
    }

    /**
     * Whether the adapter CSRF filter is active under the legacy flag.
     * Only honored when {@code inertia.security.mode} is unset.
     *
     * @return {@code true} unless explicitly disabled via the legacy flag
     * @deprecated use {@code inertia.security.mode} instead
     */
    @Deprecated
    public boolean isCsrfEnabled() {
        return csrfEnabled == null || csrfEnabled;
    }

    @Deprecated
    public void setCsrfEnabled(Boolean csrfEnabled) {
        this.csrfEnabled = csrfEnabled;
    }

    /**
     * Whether the legacy {@code inertia.csrf-enabled} property was set.
     *
     * @return {@code true} when migration warnings apply
     */
    public boolean isCsrfEnabledSet() {
        return csrfEnabled != null;
    }

    public List<String> getFlashKeys() {
        return flashKeys;
    }

    public void setFlashKeys(List<String> flashKeys) {
        this.flashKeys = flashKeys;
    }

    public boolean isAlwaysIncludeErrors() {
        return alwaysIncludeErrors;
    }

    public void setAlwaysIncludeErrors(boolean alwaysIncludeErrors) {
        this.alwaysIncludeErrors = alwaysIncludeErrors;
    }

    public int getErrorStatus() {
        return errorStatus;
    }

    public void setErrorStatus(int errorStatus) {
        this.errorStatus = errorStatus;
    }

    public String getErrorComponent() {
        return errorComponent;
    }

    public void setErrorComponent(String errorComponent) {
        this.errorComponent = errorComponent;
    }

    public long getMaxPageBytes() {
        return maxPageBytes;
    }

    public void setMaxPageBytes(long maxPageBytes) {
        this.maxPageBytes = maxPageBytes;
    }

    public boolean isErrorDetailsEnabled() {
        return errorDetailsEnabled;
    }

    public void setErrorDetailsEnabled(boolean errorDetailsEnabled) {
        this.errorDetailsEnabled = errorDetailsEnabled;
    }

    public boolean isLazyEtagEnabled() {
        return lazyEtagEnabled;
    }

    public void setLazyEtagEnabled(boolean lazyEtagEnabled) {
        this.lazyEtagEnabled = lazyEtagEnabled;
    }

    public String getRootView() {
        return rootView;
    }

    public void setRootView(String rootView) {
        this.rootView = rootView;
    }

    public boolean isConventionRoutingEnabled() {
        return conventionRoutingEnabled;
    }

    public void setConventionRoutingEnabled(boolean conventionRoutingEnabled) {
        this.conventionRoutingEnabled = conventionRoutingEnabled;
    }

    public String getConventionRoutingPrefix() {
        return conventionRoutingPrefix;
    }

    public void setConventionRoutingPrefix(String conventionRoutingPrefix) {
        this.conventionRoutingPrefix = conventionRoutingPrefix;
    }
}
