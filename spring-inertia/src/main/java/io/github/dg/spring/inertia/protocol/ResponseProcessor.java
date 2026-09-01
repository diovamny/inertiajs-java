package io.github.dg.spring.inertia.protocol;

import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.annotation.RequestScope;

import io.github.dg.spring.inertia.api.InertiaResponse;
import io.github.dg.spring.inertia.config.InertiaProperties;
import io.github.dg.spring.inertia.internal.InertiaImpl;
import io.github.dg.spring.inertia.internal.InertiaRequestContext;
import io.github.dg.spring.inertia.model.PageObject;
import io.github.dg.spring.inertia.renderer.HtmlRenderer;
import io.github.dg.spring.inertia.spi.JsonProvider;

/**
 * Turns a {@link PageObject} into the final HTTP response:
 *
 * <ul>
 *   <li>non-Inertia visits get the rendered HTML (SSR when enabled)</li>
 *   <li>precognition visits answer 204 (validate-only) or 422 (errors)</li>
 *   <li>asset version mismatches answer 409 + {@code X-Inertia-Location}</li>
 *   <li>regular Inertia visits get the page JSON with the protocol headers</li>
 *   <li>mutating requests that redirect use 303 See Other (handled by RedirectProcessor)</li>
 * </ul>
 */
@RequestScope
public class ResponseProcessor {

    private final PageObjectBuilder pageObjectBuilder;
    private final JsonProvider jsonProvider;
    private final HtmlRenderer htmlRenderer;
    private final InertiaProperties properties;

    public ResponseProcessor(PageObjectBuilder pageObjectBuilder, JsonProvider jsonProvider,
            HtmlRenderer htmlRenderer, InertiaProperties properties) {
        this.pageObjectBuilder = pageObjectBuilder;
        this.jsonProvider = jsonProvider;
        this.htmlRenderer = htmlRenderer;
        this.properties = properties;
    }

    /**
     * Build and serialize a page object for the given component and props.
     *
     * @param component the frontend component
     * @param props     the page props
     * @return the final response
     */
    public ResponseEntity<String> process(String component, Map<String, Object> props) {
        var page = pageObjectBuilder.build(component, props, properties.isAlwaysIncludeErrors());
        return process(page);
    }

    /**
     * Serialize a page object, honoring the visit type.
     *
     * @param page the page object
     * @return the final response
     */
    public ResponseEntity<String> process(PageObject page) {
        if (!InertiaRequestContext.isInertiaRequest()) {
            return renderHtml(page);
        }
        if (InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_PRECOGNITION) != null) {
            return precognition(page);
        }
        if (Boolean.TRUE.equals(InertiaRequestContext.get(PageObjectBuilder.CONTEXT_VERSION_MISMATCH))) {
            return versionMismatch(page);
        }
        var status = pageStatus();
        return serialize(page, status);
    }

    /**
     * Serialize a page object as an Inertia JSON response.
     *
     * @param page   the page object
     * @param status the HTTP status of the response
     * @return the JSON response
     */
    public InertiaResponse serialize(PageObject page, int status) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Inertia", "true");
        headers.set("X-Inertia-Component", page.component());
        if (page.version() != null) {
            headers.set("X-Inertia-Version", page.version());
        }
        var partialComponent = InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_PARTIAL_COMPONENT);
        if (partialComponent != null) {
            headers.set("X-Inertia-Partial-Component", String.valueOf(partialComponent));
        }
        // Vary by Inertia headers that affect the response
        headers.set("Vary", "X-Inertia, X-Inertia-Version, X-Inertia-Partial-Component, X-Inertia-Partial-Data, X-Inertia-Partial-Except");
        applyCustomHeaders(headers);
        var body = jsonProvider.toJson(page);
        return new InertiaResponse(HttpStatusCode.valueOf(status), headers, body);
    }

    @SuppressWarnings("unchecked")
    private ResponseEntity<String> renderHtml(PageObject page) {
        var headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "html", java.nio.charset.StandardCharsets.UTF_8));
        headers.set("Vary", "X-Inertia");
        applyCustomHeaders(headers);
        var stored = InertiaRequestContext.get(InertiaImpl.CONTEXT_VIEW_DATA);
        Map<String, Object> viewData = stored instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
        return ResponseEntity.status(pageStatus()).headers(headers).body(htmlRenderer.render(page, viewData));
    }

    private ResponseEntity<String> precognition(PageObject page) {
        if (InertiaRequestContext.get(InertiaHeaderExtractor.CONTEXT_PRECOGNITION_VALIDATE_FIELDS) != null) {
            return ResponseEntity.noContent()
                .header("Vary", "X-Inertia, Precognition")
                .build();
        }
        var errors = InertiaRequestContext.get(PageObjectBuilder.CONTEXT_ERRORS);
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Inertia", "true");
        headers.set("Vary", "X-Inertia, Precognition");
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).headers(headers)
            .body(jsonProvider.toJson(Map.of("errors", errors != null ? errors : Map.of())));
    }

    private ResponseEntity<String> versionMismatch(PageObject page) {
        var headers = new HttpHeaders();
        headers.set("X-Inertia-Location", page.url());
        headers.set("X-Inertia-Version", page.version() != null ? page.version() : "");
        headers.set("Vary", "X-Inertia, X-Inertia-Version");
        applyCustomHeaders(headers);
        return ResponseEntity.status(HttpStatus.CONFLICT).headers(headers).build();
    }

    private int pageStatus() {
        var stored = InertiaRequestContext.get(PageObjectBuilder.CONTEXT_PAGE_STATUS);
        return stored instanceof Integer status ? status : 200;
    }

    @SuppressWarnings("unchecked")
    private void applyCustomHeaders(HttpHeaders headers) {
        var stored = InertiaRequestContext.get(PageObjectBuilder.CONTEXT_CUSTOM_HEADERS);
        if (stored instanceof Map<?, ?> map) {
            for (var entry : ((Map<String, String>) map).entrySet()) {
                headers.set(entry.getKey(), entry.getValue());
            }
        }
    }
}