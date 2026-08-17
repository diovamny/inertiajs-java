package io.github.dg.spring.inertia.protocol;

import jakarta.servlet.http.HttpServletRequest;

import io.github.dg.spring.inertia.internal.InertiaRequestContext;

/**
 * Extracts the Inertia protocol headers of a visit and stores them as
 * request attributes so the whole request pipeline shares one view:
 *
 * <ul>
 *   <li>{@code X-Inertia}             - marks an Inertia visit</li>
 *   <li>{@code X-Inertia-Version}     - the client's asset version</li>
 *   <li>{@code X-Inertia-Partial-Component} - partial reload target</li>
 *   <li>{@code X-Inertia-Partial-Data}      - props to include</li>
 *   <li>{@code X-Inertia-Partial-Except}    - props to exclude</li>
 *   <li>{@code X-Inertia-Partial-Reset}     - ignore merged props</li>
 *   <li>{@code X-Inertia-Precognition}      - precognition visit</li>
 *   <li>{@code X-Inertia-Precognition-Validate-Fields} - validate-only list</li>
 *   <li>{@code X-Inertia-Error-Bag}         - target errors bag</li>
 * </ul>
 */
public class InertiaHeaderExtractor {

    public static final String CONTEXT_INERTIA = "inertia-request";
    public static final String CONTEXT_VERSION = "inertia-version";
    public static final String CONTEXT_PARTIAL_COMPONENT = "inertia-partial-component";
    public static final String CONTEXT_PARTIAL_DATA = "inertia-partial-data";
    public static final String CONTEXT_PARTIAL_EXCEPT = "inertia-partial-except";
    public static final String CONTEXT_PARTIAL_RESET = "inertia-partial-reset";
    public static final String CONTEXT_PRECOGNITION = "inertia-precognition";
    public static final String CONTEXT_PRECOGNITION_VALIDATE_FIELDS = "inertia-precognition-validate-fields";
    public static final String CONTEXT_ERROR_BAG = "inertia-error-bag";
    public static final String CONTEXT_METHOD = "request-method";
    public static final String CONTEXT_URI = "request-uri";
    public static final String CONTEXT_HEADERS_EXTRACTED = "inertia-headers-extracted";

    /**
     * Extract the protocol headers into request attributes. Runs once per
     * request.
     *
     * @param request the current servlet request
     */
    public void extract(HttpServletRequest request) {
        if (Boolean.TRUE.equals(request.getAttribute(CONTEXT_HEADERS_EXTRACTED))) {
            return;
        }
        request.setAttribute(CONTEXT_HEADERS_EXTRACTED, Boolean.TRUE);
        request.setAttribute(CONTEXT_METHOD, request.getMethod());

        var uri = request.getRequestURI();
        var query = request.getQueryString();
        request.setAttribute(CONTEXT_URI, query != null && !query.isBlank() ? uri + "?" + query : uri);

        set(request, CONTEXT_INERTIA, header(request, "X-Inertia"));
        set(request, CONTEXT_VERSION, header(request, "X-Inertia-Version"));
        set(request, CONTEXT_PARTIAL_COMPONENT, header(request, "X-Inertia-Partial-Component"));
        set(request, CONTEXT_PARTIAL_DATA, header(request, "X-Inertia-Partial-Data"));
        set(request, CONTEXT_PARTIAL_EXCEPT, header(request, "X-Inertia-Partial-Except"));
        set(request, CONTEXT_PARTIAL_RESET, header(request, "X-Inertia-Partial-Reset"));
        set(request, CONTEXT_PRECOGNITION, header(request, "X-Inertia-Precognition"));
        set(request, CONTEXT_PRECOGNITION_VALIDATE_FIELDS, header(request, "X-Inertia-Precognition-Validate-Fields"));
        set(request, CONTEXT_ERROR_BAG, header(request, "X-Inertia-Error-Bag"));
    }

    private static void set(HttpServletRequest request, String name, String value) {
        if (value != null && !value.isBlank()) {
            request.setAttribute(name, value);
        }
    }

    private static String header(HttpServletRequest request, String name) {
        return request.getHeader(name);
    }

    /**
     * The page component of the current visit.
     *
     * @return the component, or {@code null}
     */
    public String component() {
        return attr(CONTEXT_PARTIAL_COMPONENT);
    }

    /**
     * Whether the current visit is an Inertia request.
     *
     * @return {@code true} when {@code X-Inertia: true}
     */
    public boolean isInertiaRequest() {
        var value = attr(CONTEXT_INERTIA);
        return value != null && ("true".equalsIgnoreCase(value) || Boolean.parseBoolean(value));
    }

    private String attr(String name) {
        var value = InertiaRequestContext.get(name);
        return value != null ? String.valueOf(value) : null;
    }
}