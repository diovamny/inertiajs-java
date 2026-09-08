package io.github.dg.quarkus.inertia.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

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
     * <p>Property: {@code inertia.csrf-enabled}.</p>
     *
     * @return {@code true} to enforce, default {@code true}
     */
    @WithDefault("true")
    boolean csrfEnabled();

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
