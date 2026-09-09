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
 *   csrf-enabled: true
 *   flash-keys: []
 *   always-include-errors: true
 *   error-status: 500
 *   error-component: ErrorPage
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

    /** Whether the XSRF-TOKEN cookie synchronization is active. */
    private boolean csrfEnabled = true;

    /** Additional session keys that must be flashed with every page. */
    private List<String> flashKeys = List.of();

    /** Whether validation errors are always injected into the props. */
    private boolean alwaysIncludeErrors = true;

    /** HTTP status used for error pages. */
    private int errorStatus = 500;

    /** Component used for error pages. */
    private String errorComponent = "ErrorPage";

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

    public boolean isCamelizeProps() {
        return camelizeProps;
    }

    public void setCamelizeProps(boolean camelizeProps) {
        this.camelizeProps = camelizeProps;
    }

    public boolean isCsrfEnabled() {
        return csrfEnabled;
    }

    public void setCsrfEnabled(boolean csrfEnabled) {
        this.csrfEnabled = csrfEnabled;
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
