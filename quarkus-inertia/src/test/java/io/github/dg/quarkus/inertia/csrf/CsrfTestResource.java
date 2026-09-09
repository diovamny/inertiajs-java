package io.github.dg.quarkus.inertia.csrf;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/csrf-test")
public class CsrfTestResource {

    @GET
    public Response get() {
        return Response.ok("get").build();
    }

    @POST
    public Response post() {
        return Response.ok("post").build();
    }
}
