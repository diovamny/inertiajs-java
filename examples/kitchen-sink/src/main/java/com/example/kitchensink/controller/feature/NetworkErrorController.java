package com.example.kitchensink.controller.feature;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.WebApplicationException;

import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import com.quarkus.inertia.api.Inertia;

@Path("/features/errors")
public class NetworkErrorController {

    @Inject
    Inertia inertia;

    @GET
    @Path("http-exceptions")
    @Blocking
    public Uni<Object> httpExceptions() {
        return inertia.render("Features/Errors/HttpExceptions");
    }

@GET
    @Path("http-exceptions/403")
    public Object httpException403() {
        throw new WebApplicationException("Forbidden", 403);
    }

    @GET
    @Path("http-exceptions/404")
    @Blocking
    public Uni<Object> httpException404() {
        throw new WebApplicationException("Not Found", 404);
    }

    @GET
    @Path("http-exceptions/500")
    @Blocking
    public Uni<Object> httpException500() {
        throw new WebApplicationException("Server Error", 500);
    }

    @GET
    @Path("http-exceptions/unhandled")
    @Blocking
    public Uni<Object> httpExceptionUnhandled() {
        throw new WebApplicationException("I'm a teapot", 418);
    }

    @GET
    @Path("network-errors")
    @Blocking
    public Uni<Object> networkErrors() {
        return inertia.render("Features/Errors/NetworkErrors");
    }
}
