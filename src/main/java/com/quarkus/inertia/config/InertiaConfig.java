package com.quarkus.inertia.config;

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
     * Path of the SSR bundle produced by the frontend build.
     * <p>Property: {@code inertia.ssr-bundle}.</p>
     *
     * @return the bundle path, empty when not configured
     */
    java.util.Optional<String> ssrBundle();

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
     * @return {@code true} to always include, default {@code false}
     */
    @WithDefault("false")
    boolean alwaysIncludeErrors();

    /**
     * Whether the ETag lazy-response optimization is enabled.
     * <p>Property: {@code inertia.lazy-etag-enabled}.</p>
     *
     * @return {@code true} to enable, default {@code true}
     */
    @WithDefault("true")
    boolean lazyEtagEnabled();
}