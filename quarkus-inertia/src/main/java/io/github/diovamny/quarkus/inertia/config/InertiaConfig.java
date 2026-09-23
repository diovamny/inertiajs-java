package io.github.diovamny.quarkus.inertia.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import io.smallrye.config.WithName;

/**
 * Runtime configuration of Quarkus Inertia, bound from the
 * {@code application.properties} prefix {@code inertia.}.
 *
 * <p>Example:</p>
 * <pre>{@code
 * inertia.ssr-enabled=true
 * inertia.ssr-url=http://localhost:13714
 * inertia.csrf-enabled=false
 * }</pre>
 */
@ConfigMapping(prefix = "inertia")
public interface InertiaConfig {

    /**
     * Name of the Qute template that bootstraps the frontend.
     * <p>Property: {@code inertia.root-template}.</p>
     *
     * @return the root template name, default {@code index.html}
     */
    @WithDefault("index.html")
    String rootTemplate();

    /**
     * Whether the root HTML template is cached in memory.
     * <p>Property: {@code inertia.template-cache-enabled}.</p>
     *
     * @return {@code true} to cache template, default {@code true}
     */
    @WithDefault("true")
    boolean templateCacheEnabled();

    /**
     * Whether server-side rendering is enabled for initial visits.
     * <p>Property: {@code inertia.ssr-enabled}.</p>
     *
     * @return {@code true} to enable SSR, default {@code false}
     */
    @WithDefault("false")
    boolean ssrEnabled();

    /**
     * Base URL of the SSR server (Vite SSR).
     * <p>Property: {@code inertia.ssr-url}.</p>
     *
     * @return the SSR URL, default {@code http://localhost:13714}
     */
    @WithDefault("http://localhost:13714")
    String ssrUrl();

    /**
     * Paths excluded from server-side rendering; those requests fall back
     * to the client-side root template.
     * <p>Property: {@code inertia.ssr-exclude-paths} (comma separated).</p>
     *
     * @return the excluded paths, empty when not configured
     */
    java.util.Optional<java.util.List<String>> ssrExcludePaths();

    /**
     * Connection timeout for SSR requests.
     * <p>Property: {@code inertia.ssr-connect-timeout}.</p>
     *
     * @return the timeout, default {@code 5s}
     */
    @WithDefault("5s")
    java.time.Duration ssrConnectTimeout();

    /**
     * Read timeout for SSR requests.
     * <p>Property: {@code inertia.ssr-read-timeout}.</p>
     *
     * @return the timeout, default {@code 10s}
     */
      @WithDefault("10s")
     java.time.Duration ssrReadTimeout();

    /**
     * Consecutive SSR sidecar failures before the circuit breaker opens
     * (fail-fast CSR fallback while open).
     * <p>Property: {@code inertia.ssr-breaker-failure-threshold}.</p>
     *
     * @return the threshold, default {@code 5}
     */
    @WithName("ssr-breaker-failure-threshold")
    @WithDefault("5")
    int ssrBreakerFailureThreshold();

    /**
     * Cooldown before a half-open probe is allowed through an open breaker.
     * <p>Property: {@code inertia.ssr-breaker-cooldown}.</p>
     *
     * @return the cooldown, default {@code 30s}
     */
    @WithName("ssr-breaker-cooldown")
    @WithDefault("30s")
    java.time.Duration ssrBreakerCooldown();

    /**
     * Cache SSR sidecar responses keyed by page hash (Rails
     * {@code ssr_cache} parity).
     * <p>Property: {@code inertia.ssr-cache-enabled}.</p>
     *
     * @return {@code true} to cache, default {@code false}
     */
    @WithName("ssr-cache-enabled")
    @WithDefault("false")
    boolean ssrCacheEnabled();

    /**
     * Default TTL for cached SSR responses (overridable per render via
     * {@code enableSsrCache}).
     * <p>Property: {@code inertia.ssr-cache-ttl}.</p>
     *
     * @return the TTL, default {@code 15m}
     */
    @WithName("ssr-cache-ttl")
    @WithDefault("15m")
    java.time.Duration ssrCacheTtl();

    /**
     * Supervise the Node.js SSR sidecar process (start, restart with
     * backoff, stop on shutdown).
     * <p>Property: {@code inertia.ssr-supervisor-enabled}.</p>
     *
     * @return {@code true} to supervise, default {@code false}
     */
    @WithName("ssr-supervisor-enabled")
    @WithDefault("false")
    boolean ssrSupervisorEnabled();

    /**
     * Sidecar executable for the supervisor.
     * <p>Property: {@code inertia.ssr-supervisor-command}.</p>
     *
     * @return the command, default {@code node}
     */
    @WithName("ssr-supervisor-command")
    @WithDefault("node")
    String ssrSupervisorCommand();

    /**
     * Sidecar entry file for the supervisor.
     * <p>Property: {@code inertia.ssr-supervisor-entry}.</p>
     *
     * @return the entry, default {@code dist-ssr/ssr.mjs}
     */
    @WithName("ssr-supervisor-entry")
    @WithDefault("dist-ssr/ssr.mjs")
    String ssrSupervisorEntry();

    /**
     * Working directory for the supervised sidecar (blank = JVM directory).
     * <p>Property: {@code inertia.ssr-supervisor-workdir}.</p>
     *
     * @return the directory, empty by default
     */
    @WithName("ssr-supervisor-workdir")
    java.util.Optional<String> ssrSupervisorWorkdir();

    /**
     * Restart attempts after the initial start before giving up.
     * <p>Property: {@code inertia.ssr-supervisor-max-restarts}.</p>
     *
     * @return the attempts, default {@code 5}
     */
    @WithName("ssr-supervisor-max-restarts")
    @WithDefault("5")
    int ssrSupervisorMaxRestarts();

    /**
     * Strategy used to compute the asset version for cache-busting.
     * <p>Property: {@code inertia.version-strategy}.</p>
     *
     * @return the strategy name, default {@code sha256}
     */
    @WithDefault("sha256")
    String versionStrategy();

    /**
     * Static asset version reported to the client, overriding any strategy.
     * <p>Property: {@code inertia.version-custom}.</p>
     *
     * @return the custom version, empty when not configured
     */
    java.util.Optional<String> versionCustom();

    /**
     * Whether the client must encrypt its history state.
     * <p>Property: {@code inertia.encrypt-history}.</p>
     *
     * @return {@code true} to enable, default {@code false}
     */
    @WithDefault("false")
    boolean encryptHistory();

    /**
     * Whether the client must clear its history state (Spring parity).
     * <p>Property: {@code inertia.clear-history}.</p>
     *
     * @return {@code true} to enable, default {@code false}
     */
    @WithDefault("false")
    boolean clearHistory();

    /**
     * XSRF-TOKEN cookie refresh policy (Rails {@code :always} / {@code :lazy}
     * parity). {@code always} re-emits {@code Set-Cookie} on every response;
     * {@code lazy} skips re-emission on idempotent requests that already
     * present a valid token, keeping responses cacheable by CDNs and reverse
     * proxies.
     * <p>Property: {@code inertia.csrf-refresh-policy}.</p>
     *
     * @return {@code always} or {@code lazy}, default {@code always}
     */
    @WithName("csrf-refresh-policy")
    @WithDefault("always")
    String csrfRefreshPolicy();

    /**
     * Publish collected server head tags as the {@code head} page prop.
     * <p>Property: {@code inertia.server-head}.</p>
     *
     * @return {@code true} to emit, default {@code false}
     */
    @WithName("server-head")
    @WithDefault("false")
    boolean serverHead();

    /**
     * Title template applied by the head builder ({@code %s} is the title).
     * <p>Property: {@code inertia.meta-title-template}.</p>
     *
     * @return the template, default {@code %s} (title as-is)
     */
    @WithName("meta-title-template")
    @WithDefault("%s")
    String metaTitleTemplate();

    /**
     * Whether prop keys are camelized (e.g. {@code first_name} ->
     * {@code firstName}) when building the page object.
     * <p>Property: {@code inertia.camelize-props}.</p>
     *
     * @return {@code true} to camelize, default {@code false}
     */
    @WithDefault("false")
    boolean camelizeProps();

    /**
     * Whether CSRF protection is enforced on non-GET Inertia requests.
     * Legacy flag: only honored when {@code inertia.security.mode} is unset
     * ({@code true} maps to {@code adapter}, {@code false} to
     * {@code disabled}).
     * <p>Property: {@code inertia.csrf-enabled}.</p>
     *
     * @return {@code true} to enforce, default {@code true}
     * @deprecated use {@code inertia.security.mode} instead
     */
    @Deprecated
    @WithDefault("true")
    boolean csrfEnabled();

    /**
     * CSRF ownership: {@code auto} (recommended), {@code framework},
     * {@code adapter} or {@code disabled}.
     * <p>Property: {@code inertia.security.mode}.</p>
     *
     * @return the mode, empty for {@code auto}
     */
    @WithName("security.mode")
    java.util.Optional<String> securityMode();

    /**
     * Fail startup when {@code auto} resolves to the adapter fallback.
     * <p>Property: {@code inertia.security.fail-on-fallback}.</p>
     *
     * @return {@code true} to refuse the fallback, default {@code false}
     */
    @WithName("security.fail-on-fallback")
    @WithDefault("false")
    boolean securityFailOnFallback();

    /**
     * Allow {@code disabled} with the prod profile.
     * <p>Property: {@code inertia.security.allow-disabled-in-production}.</p>
     *
     * @return {@code true} to allow, default {@code false}
     */
    @WithName("security.allow-disabled-in-production")
    @WithDefault("false")
    boolean securityAllowDisabledInProduction();

    /**
     * Login/OIDC URL used for {@code 409 + X-Inertia-Location} challenges.
     * <p>Property: {@code inertia.security.login-url}.</p>
     *
     * @return the login URL, default {@code /login}
     */
    @WithName("security.login-url")
    @WithDefault("/login")
    String securityLoginUrl();

    /**
     * Inertia component rendered for {@code 403} pages.
     * <p>Property: {@code inertia.security.forbidden-component}.</p>
     *
     * @return the component name, default {@code Errors/Forbidden}
     */
    @WithName("security.forbidden-component")
    @WithDefault("Errors/Forbidden")
    String securityForbiddenComponent();

    /**
     * Safe fallback path for CSRF-failure redirects.
     * <p>Property: {@code inertia.security.csrf-failure-path}.</p>
     *
     * @return the fallback path, default {@code /}
     */
    @WithName("security.csrf-failure-path")
    @WithDefault("/")
    String securityCsrfFailurePath();

    /**
     * Flash key carrying the CSRF-expiry message.
     * <p>Property: {@code inertia.security.csrf-flash-key}.</p>
     *
     * @return the flash key, default {@code error}
     */
    @WithName("security.csrf-flash-key")
    @WithDefault("error")
    String securityCsrfFlashKey();

    /**
     * Generic CSRF-expiry message (no internals, safe to display).
     * <p>Property: {@code inertia.security.csrf-flash-message}.</p>
     *
     * @return the message
     */
    @WithName("security.csrf-flash-message")
    @WithDefault("La página expiró. Vuelve a intentarlo.")
    String securityCsrfFlashMessage();

    /**
     * {@code SameSite} attribute of the XSRF-TOKEN cookie.
     * <p>Property: {@code inertia.security.cookie-same-site}.</p>
     *
     * @return the attribute, default {@code Lax}
     */
    @WithName("security.cookie-same-site")
    @WithDefault("Lax")
    String securityCookieSameSite();

    /**
     * {@code Secure} attribute of the XSRF-TOKEN cookie (enable on HTTPS).
     * <p>Property: {@code inertia.security.cookie-secure}.</p>
     *
     * @return {@code true} to force Secure, default {@code false}
     */
    @WithName("security.cookie-secure")
    @WithDefault("false")
    boolean securityCookieSecure();

    /**
     * {@code Path} attribute of the XSRF-TOKEN cookie.
     * <p>Property: {@code inertia.security.cookie-path}.</p>
     *
     * @return the path, default {@code /}
     */
    @WithName("security.cookie-path")
    @WithDefault("/")
    String securityCookiePath();

    /**
     * {@code Domain} attribute of the XSRF-TOKEN cookie (empty = host-only).
     * <p>Property: {@code inertia.security.cookie-domain}.</p>
     *
     * @return the domain, empty when unset
     */
    @WithName("security.cookie-domain")
    java.util.Optional<String> securityCookieDomain();

    /**
     * Reactive path prefixes owned by the adapter CSRF filter in
     * {@code framework} mode ({@code quarkus-rest-csrf} only sees JAX-RS).
     * Router-level filters run before route matching and cannot tell
     * transports apart, so the application declares its mutating reactive
     * prefixes here (exact or segment-prefix match).
     * <p>Property: {@code inertia.security.reactive-csrf-paths} (comma
     * separated).</p>
     *
     * @return the declared prefixes, empty when unset
     */
    @WithName("security.reactive-csrf-paths")
    java.util.Optional<java.util.List<String>> securityReactiveCsrfPaths();

    /**
     * Whether the security integration contributes the allowlisted identity
     * summary ({@code auth.user}) to every page. Presentation only; never
     * authorizes.
     * <p>Property: {@code inertia.security.auth-props-enabled}.</p>
     *
     * @return {@code true} to contribute, default {@code false}
     */
    @WithName("security.auth-props-enabled")
    @WithDefault("false")
    boolean authPropsEnabled();

    /**
     * Custom root view template name used for non-Inertia (full page)
     * requests.
     * <p>Property: {@code inertia.root-view}.</p>
     *
     * @return the root view name, empty to use {@code rootTemplate()}
     */
    java.util.Optional<String> rootView();

    /**
     * Keys whose flash values are surfaced as client-visible props (in
     * addition to the default handling).
     * <p>Property: {@code inertia.flash-keys} (comma separated).</p>
     *
     * @return the flash keys, empty when not configured
     */
    java.util.Optional<java.util.List<String>> flashKeys();

    /**
     * Whether the {@code errors} prop is always included in the page
     * object, even when empty (Laravel-compatible behavior).
     * <p>Property: {@code inertia.always-include-errors}.</p>
     *
     * @return {@code true} to always include, default {@code true}
     */
    @WithDefault("true")
    boolean alwaysIncludeErrors();

    /**
     * Emit arrays of messages per field ({@code Map<field, List<message>>}).
     * Default {@code false} preserves the legacy {@code Map<field, message>} wire.
     * <p>Property: {@code inertia.validation.all-errors}.</p>
     *
     * @return {@code true} for multi-message arrays, default {@code false}
     */
    @WithName("validation.all-errors")
    @WithDefault("false")
    boolean validationAllErrors();

    /**
     * HTTP status used for Inertia error pages when an unhandled exception
     * reaches the exception mapper.
     * <p>Property: {@code inertia.error-status}.</p>
     *
     * @return the error status, default {@code 500}
     */
    @WithDefault("500")
    int errorStatus();

    /**
     * Frontend component rendered when an unhandled exception reaches the
     * exception mapper.
     * <p>Property: {@code inertia.error-component}.</p>
     *
     * @return the error component name, default {@code ErrorPage}
     */
    @WithDefault("ErrorPage")
    String errorComponent();

    /**
     * Whether to include exception details in error responses (development
     * mode). In production, a generic message is returned.
     * <p>Property: {@code inertia.error-details-enabled}.</p>
     *
     * @return {@code true} to include details, default {@code false}
     */
    @WithDefault("false")
    boolean errorDetailsEnabled();

    /**
     * Max serialized page size in bytes; larger pages fail with {@code 413}.
     * Negative disables the check.
     * <p>Property: {@code inertia.max-page-bytes}.</p>
     *
     * @return the limit in bytes, default {@code 33554432} (32 MiB)
     */
    @WithName("max-page-bytes")
    @WithDefault("33554432")
    long maxPageBytes();

    /**
     * Whether the ETag lazy-response optimization is enabled.
     * <p>Property: {@code inertia.lazy-etag-enabled}.</p>
     *
     * @return {@code true} to enable, default {@code true}
     */
    @WithDefault("true")
    boolean lazyEtagEnabled();

    /**
     * Whether to use Qute for rendering the root HTML template instead of
     * the fast placeholder-based renderer. Requires the {@code quarkus-qute}
     * dependency.
     * <p>Property: {@code inertia.use-qute}.</p>
     *
     * @return {@code true} to use Qute, default {@code false}
     */
    @WithDefault("false")
    boolean useQute();

    /**
     * Whether auto-resolving component names by convention is enabled.
     * <p>Property: {@code inertia.convention-routing-enabled}.</p>
     *
     * @return {@code true} to enable convention routing, default {@code false}
     */
    @WithDefault("false")
    boolean conventionRoutingEnabled();

    /**
     * Optional prefix prepended to auto-resolved component names.
     * <p>Property: {@code inertia.convention-routing-prefix}.</p>
     *
     * @return the prefix, empty when not configured
     */
    java.util.Optional<String> conventionRoutingPrefix();
}
