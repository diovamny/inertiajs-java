package io.github.dg.quarkus.inertia.api;

import java.util.Map;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;
import jakarta.ws.rs.core.Response;

/**
 * Specialized rendering interface for Inertia.js responses in Quarkus.
 *
 * <p>Provides reactive (Mutiny {@link Uni}), synchronous JAX-RS ({@link Response}),
 * and Vert.x web route ({@link RoutingContext}) rendering APIs.</p>
 */
public interface InertiaRenderer {

    // ---------------------------------------------------------------------
    // Reactive Rendering (Mutiny Uni)
    // ---------------------------------------------------------------------

    /**
     * Render an Inertia page auto-resolving the component name by convention.
     *
     * @return a Uni resolving to the response object
     */
    Uni<Object> render();

    /**
     * Render an Inertia page with props, auto-resolving the component name by convention.
     *
     * @param props the page props
     * @return a Uni resolving to the response object
     */
    Uni<Object> render(Map<String, Object> props);

    /**
     * Render an Inertia page with props from a {@link ProvidesInertiaProperties} instance,
     * auto-resolving the component name by convention.
     *
     * @param provider the provider whose inertia properties will be rendered
     * @return a Uni resolving to the response object
     */
    Uni<Object> render(ProvidesInertiaProperties provider);

    /**
     * Render an Inertia page for the given component with the given props.
     *
     * @param component the frontend component name, e.g. {@code "Contacts/Index"}
     * @param props     the page props; merged with shared and flash data
     * @return a Uni resolving to the response object (JSON page or HTML)
     */
    Uni<Object> render(String component, Map<String, Object> props);

    /**
     * Render an Inertia page for the given component with props from a {@link ProvidesInertiaProperties} instance.
     *
     * @param component the frontend component name
     * @param provider  the provider whose inertia properties will be rendered
     * @return a Uni resolving to the response object
     */
    Uni<Object> render(String component, ProvidesInertiaProperties provider);

    /**
     * Render an Inertia page with no props.
     *
     * @param component the frontend component name
     * @return a Uni resolving to the response object
     */
    Uni<Object> render(String component);

    /**
     * Render an Inertia page whose component name comes from an enum constant.
     *
     * @param component the enum constant
     * @return a Uni resolving to the response object
     */
    Uni<Object> render(Enum<?> component);

    /**
     * Render an Inertia page whose component name comes from an enum constant,
     * with the given props.
     *
     * @param component the enum constant
     * @param props     the page props
     * @return a Uni resolving to the response object
     */
    Uni<Object> render(Enum<?> component, Map<String, Object> props);

    /**
     * Render an Inertia page with the given props and an explicit HTTP status code.
     *
     * @param component the frontend component name
     * @param props     the page props
     * @param status    the HTTP status of the response
     * @return a Uni resolving to the response object
     */
    Uni<Object> render(String component, Map<String, Object> props, int status);

    /**
     * Render an Inertia page with no props and an explicit HTTP status.
     *
     * @param component the frontend component name
     * @param status    the HTTP status of the response
     * @return a Uni resolving to the response object
     */
    Uni<Object> render(String component, int status);

    /**
     * Render an Inertia page whose component name comes from an enum constant, with an explicit HTTP status.
     *
     * @param component the enum constant
     * @param status    the HTTP status of the response
     * @return a Uni resolving to the response object
     */
    Uni<Object> render(Enum<?> component, int status);

    /**
     * Render an Inertia page whose component name comes from an enum constant, with the given props and an explicit HTTP status.
     *
     * @param component the enum constant
     * @param props     the page props
     * @param status    the HTTP status of the response
     * @return a Uni resolving to the response object
     */
    Uni<Object> render(Enum<?> component, Map<String, Object> props, int status);

    // ---------------------------------------------------------------------
    // Synchronous JAX-RS Response API
    // ---------------------------------------------------------------------

    /**
     * Render an Inertia page auto-resolving the component name by convention,
     * returning a synchronous JAX-RS Response.
     *
     * @return a JAX-RS Response
     */
    Response renderSync();

    /**
     * Render an Inertia page with props auto-resolving the component name by convention,
     * returning a synchronous JAX-RS Response.
     *
     * @param props the page props
     * @return a JAX-RS Response
     */
    Response renderSync(Map<String, Object> props);

    /**
     * Render an Inertia page auto-resolving the component name by convention,
     * with props from a {@link ProvidesInertiaProperties} instance,
     * returning a synchronous JAX-RS Response.
     *
     * @param provider the provider whose inertia properties will be rendered
     * @return a JAX-RS Response
     */
    Response renderSync(ProvidesInertiaProperties provider);

    /**
     * Render an Inertia page and return a synchronous JAX-RS {@link Response}.
     *
     * @param component the frontend component name
     * @param props     the page props
     * @return a JAX-RS Response
     */
    Response renderSync(String component, Map<String, Object> props);

    /**
     * Render an Inertia page for the given component with props from a {@link ProvidesInertiaProperties} instance,
     * returning a synchronous JAX-RS {@link Response}.
     *
     * @param component the frontend component name
     * @param provider  the provider whose inertia properties will be rendered
     * @return a JAX-RS Response
     */
    Response renderSync(String component, ProvidesInertiaProperties provider);

    /**
     * Render an Inertia page with no props and return a synchronous JAX-RS Response.
     *
     * @param component the frontend component name
     * @return a JAX-RS Response
     */
    Response renderSync(String component);

    /**
     * Render an Inertia page whose component name comes from an enum constant.
     *
     * @param component the enum constant
     * @return a JAX-RS Response
     */
    Response renderSync(Enum<?> component);

    /**
     * Render an Inertia page whose component name comes from an enum constant, with the given props.
     *
     * @param component the enum constant
     * @param props     the page props
     * @return a JAX-RS Response
     */
    Response renderSync(Enum<?> component, Map<String, Object> props);

    /**
     * Render an Inertia page with the given props and an explicit HTTP status code, returning a synchronous JAX-RS Response.
     *
     * @param component the frontend component name
     * @param props     the page props
     * @param status    the HTTP status of the response
     * @return a JAX-RS Response
     */
    Response renderSync(String component, Map<String, Object> props, int status);

    /**
     * Render an Inertia page with no props and an explicit HTTP status.
     *
     * @param component the frontend component name
     * @param status    the HTTP status of the response
     * @return a JAX-RS Response
     */
    Response renderSync(String component, int status);

    /**
     * Render an Inertia page whose component name comes from an enum constant, with an explicit HTTP status.
     *
     * @param component the enum constant
     * @param status    the HTTP status of the response
     * @return a JAX-RS Response
     */
    Response renderSync(Enum<?> component, int status);

    /**
     * Render an Inertia page whose component name comes from an enum constant, with the given props and an explicit HTTP status.
     *
     * @param component the enum constant
     * @param props     the page props
     * @param status    the HTTP status of the response
     * @return a JAX-RS Response
     */
    Response renderSync(Enum<?> component, Map<String, Object> props, int status);

    // ---------------------------------------------------------------------
    // Vert.x Reactive Routes (synchronous API)
    // ---------------------------------------------------------------------

    /**
     * Render an Inertia page directly into a Vert.x {@link RoutingContext}.
     * For use in {@code @Route} handlers.
     *
     * @param rc        the routing context
     * @param component the frontend component name
     * @param props     the page props
     */
    void renderVertx(RoutingContext rc, String component, Map<String, Object> props);

    /**
     * Render an Inertia page directly into a Vert.x {@link RoutingContext} with no props.
     *
     * @param rc        the routing context
     * @param component the frontend component name
     */
    void renderVertx(RoutingContext rc, String component);
}