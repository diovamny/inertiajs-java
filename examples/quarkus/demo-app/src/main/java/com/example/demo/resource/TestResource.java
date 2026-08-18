package com.example.demo.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/test")
public class TestResource {

    @GET
    public String hello() {
        return "hello";
    }

    @GET
    @Path("/nowhere")
    public String nowhere() {
        return "nowhere";
    }

    @POST
    @Path("/noop")
    public String noop() {
        return "noop";
    }

    @POST
    @Path("/echo")
    @Consumes(MediaType.APPLICATION_JSON)
    public String echo(String body) {
        return "echo: " + body;
    }

    @POST
    @Path("/ok")
    public Response ok() {
        return Response.ok("ok").build();
    }

    @POST
    @Path("/nocontent")
    public Response nocontent() {
        return Response.noContent().build();
    }

    @POST
    @Path("/created")
    public Response created() {
        return Response.status(201).entity("created").build();
    }

    @POST
    @Path("/badrequest")
    public Response badrequest() {
        return Response.status(400).entity("bad").build();
    }

    @POST
    @Path("/empty-200")
    public Response empty200() {
        return Response.ok().build();
    }

    @POST
    @Path("/redirect-me")
    public Response redirectMe() {
        return Response.status(302).entity("redirecting").header("Location", "/test").build();
    }

    @POST
    @Path("/json-redirect")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response jsonRedirect() {
        return Response.status(302).header("Location", "/test").build();
    }

    @POST
    @Path("/redirect-301")
    public Response redirect301() {
        return Response.status(301).header("Location", "/test").build();
    }

    @POST
    @Path("/redirect-302")
    public Response redirect302() {
        return Response.status(302).header("Location", "/test").build();
    }

    @POST
    @Path("/redirect-307")
    public Response redirect307() {
        return Response.status(307).header("Location", "/test").build();
    }

    @GET
    @Path("/redirect-303-get")
    public Response redirect303Get() {
        return Response.status(302).header("Location", "/test").build();
    }
}
