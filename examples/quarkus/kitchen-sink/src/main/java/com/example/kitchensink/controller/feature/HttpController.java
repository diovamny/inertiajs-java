package com.example.kitchensink.controller.feature;

import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;

import com.example.kitchensink.service.Demo;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import io.github.dg.quarkus.inertia.api.Inertia;

@Path("/features/http")
public class HttpController {

    @Inject
    Inertia inertia;

    @GET
    @Path("use-http")
    @Blocking
    public Uni<Object> useHttp() {
        return inertia.render("Features/Http/UseHttp");
    }

    @POST
    @Path("use-http/api")
    @Consumes(MediaType.APPLICATION_JSON)
    public Map<String, Object> useHttpApi(Map<String, String> body) {
        var name = body != null && body.get("name") != null && !body.get("name").isBlank()
            ? body.get("name")
            : "World";
        return Map.of(
            "message", "Hello, " + name + "!",
            "timestamp", Demo.iso());
    }
}
