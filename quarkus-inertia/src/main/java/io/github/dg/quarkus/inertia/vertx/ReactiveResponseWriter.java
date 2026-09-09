package io.github.dg.quarkus.inertia.vertx;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import io.smallrye.mutiny.Uni;
import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import io.github.dg.quarkus.inertia.protocol.RedirectProcessor;
import io.github.dg.quarkus.inertia.protocol.ResponseProcessor;

/**
 * Writes a JAX-RS {@link Response} produced by the processors into the
 * Vert.x response of a reactive route.
 *
 * <p>{@link ResponseProcessor} and {@link RedirectProcessor} route their
 * final response through {@link #write(Response)}: for reactive routes the
 * response is written immediately (dispatched to the request event loop when
 * the current thread is a worker, i.e. a {@code @Blocking} route) and a
 * {@code null} item is returned so the generated reactive-routes handler ends
 * with a swallowed {@code NullPointerException}; for JAX-RS (or when there is
 * no routing context) the response passes through unchanged.</p>
 */
@ApplicationScoped
public class ReactiveResponseWriter {

    static final String ROUTING_CONTEXT_KEY = "inertia-routing-context";
    static final String REQUEST_CONTEXT_KEY = "inertia-request-context";
    static final String JAXRS_KEY = "inertia-jaxrs";

    @Inject
    InertiaResponseDecorator decorator;

    /**
     * Route the response through the reactive transport when applicable.
     *
     * @param response the response built by the processors
     * @return a {@code null} item when written reactively, the response
     *         itself otherwise
     */
    public Uni<Object> write(Response response) {
        var ctx = Vertx.currentContext();
        if (ctx == null) return Uni.createFrom().item(response);

        var rc = (RoutingContext) ctx.getLocal(ROUTING_CONTEXT_KEY);
        if (rc == null) return Uni.createFrom().item(response);
        if (Boolean.TRUE.equals(ctx.getLocal(JAXRS_KEY))) return Uni.createFrom().item(response);

        var requestContext = (Context) rc.get(REQUEST_CONTEXT_KEY);
        if (requestContext != null && requestContext != Vertx.currentContext()) {
            return Uni.createFrom().emitter(emitter -> requestContext.runOnContext(v -> {
                try {
                    write0(rc, response);
                    emitter.complete(null);
                } catch (Throwable t) {
                    emitter.fail(t);
                }
            }));
        }
        write0(rc, response);
        return Uni.createFrom().nullItem();
    }

    /**
     * Write the response into the Vert.x response, dispatching to the request
     * event loop when called from a worker thread. Used by the pre-handler
     * (CSRF rejection) and the failure handler.
     *
     * @param rc       the routing context
     * @param response the response to write
     */
    public void write(RoutingContext rc, Response response) {
        var requestContext = (Context) rc.get(REQUEST_CONTEXT_KEY);
        if (requestContext != null && requestContext != Vertx.currentContext()) {
            requestContext.runOnContext(v -> write0(rc, response));
        } else {
            write0(rc, response);
        }
    }

    private void write0(RoutingContext rc, Response response) {
        var decorated = decorator.decorate(rc, response);
        var vertxResponse = rc.response();
        vertxResponse.setStatusCode(decorated.status());
        vertxResponse.headers().setAll(decorated.headers());

        var entity = decorated.entity();
        if (entity != null) {
            if (entity instanceof String s) {
                vertxResponse.end(s);
            } else if (entity instanceof io.vertx.core.buffer.Buffer b) {
                vertxResponse.end(b);
            } else {
                if (vertxResponse.headers().get("Content-Type") == null) {
                    vertxResponse.headers().set("Content-Type", "application/json");
                }
                vertxResponse.end(io.vertx.core.json.Json.encode(entity));
            }
        } else {
            vertxResponse.end();
        }
    }
}
