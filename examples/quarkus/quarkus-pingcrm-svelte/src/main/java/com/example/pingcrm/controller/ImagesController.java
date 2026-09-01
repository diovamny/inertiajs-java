package com.example.pingcrm.controller;

import java.nio.file.Files;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import io.smallrye.common.annotation.Blocking;

@jakarta.ws.rs.Path("/img")
public class ImagesController {

    @GET
    @jakarta.ws.rs.Path("{path:.*}")
    @Blocking
    public Response show(@jakarta.ws.rs.PathParam("path") String path,
            @jakarta.ws.rs.QueryParam("w") Integer width,
            @jakarta.ws.rs.QueryParam("h") Integer height,
            @jakarta.ws.rs.QueryParam("fit") String fit) {
        try {
            var base = java.nio.file.Path.of("./data/images").toAbsolutePath().normalize();
            var target = base.resolve(path).normalize();
            if (!target.startsWith(base) || !Files.isRegularFile(target)) {
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            var bytes = Files.readAllBytes(target);
            var mediaType = Files.probeContentType(target);
            if (mediaType == null) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
            return Response.ok(bytes, mediaType)
                .header("Cache-Control", "public, max-age=31536000, immutable")
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }
}
