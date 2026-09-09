package io.github.diovamny.spring.inertia.internal;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Accessor for the request-scoped state of the current Inertia request.
 *
 * <p>Mirrors the Vert.x context-local storage of the Quarkus adapter: every
 * value the protocol layer needs (request method, URI, Inertia headers,
 * page status, validation errors, ...) is stored as an
 * {@code HttpServletRequest} attribute so controllers, processors and
 * filters share one coherent view of the visit.</p>
 *
 * <p>When no request is active (e.g. pure unit tests), all accessors return
 * safe defaults.</p>
 */
public final class InertiaRequestContext {

    private InertiaRequestContext() {
    }

    /**
     * The current request, or {@code null} when none is bound.
     *
     * @return the request or {@code null}
     */
    public static HttpServletRequest request() {
        var attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servlet) {
            return servlet.getRequest();
        }
        return null;
    }

    /**
     * Read a request attribute.
     *
     * @param name the attribute name
     * @return the value or {@code null}
     */
    public static Object get(String name) {
        var request = request();
        return request != null ? request.getAttribute(name) : null;
    }

    /**
     * Store a request attribute.
     *
     * @param name  the attribute name
     * @param value the value; {@code null} removes the attribute
     */
    public static void set(String name, Object value) {
        var request = request();
        if (request == null) {
            return;
        }
        if (value == null) {
            request.removeAttribute(name);
        } else {
            request.setAttribute(name, value);
        }
    }

    /**
     * The HTTP method of the current request.
     *
     * @return the method, or {@code null}
     */
    public static String method() {
        var request = request();
        if (request != null) {
            return request.getMethod();
        }
        return null;
    }

    /**
     * The request URI with its query string.
     *
     * @return the URI, or {@code "/"}
     */
    public static String uri() {
        var request = request();
        if (request != null) {
            var uri = request.getRequestURI();
            var query = request.getQueryString();
            return query != null && !query.isBlank() ? uri + "?" + query : uri;
        }
        return "/";
    }

    /**
     * The raw request header, or {@code null}.
     *
     * @param name the header name
     * @return the header value or {@code null}
     */
    public static String header(String name) {
        var request = request();
        return request != null ? request.getHeader(name) : null;
    }

    /**
     * Whether the current request is an Inertia visit.
     *
     * @return {@code true} when {@code X-Inertia: true}
     */
    public static boolean isInertiaRequest() {
        var flag = get("inertia-request");
        if (flag != null) {
            return "true".equalsIgnoreCase(String.valueOf(flag))
                || Boolean.parseBoolean(String.valueOf(flag));
        }
        var header = header("X-Inertia");
        return header != null && ("true".equalsIgnoreCase(header) || Boolean.parseBoolean(header));
    }
}
