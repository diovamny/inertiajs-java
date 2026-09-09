package com.example.demo.resource;

import java.time.Duration;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

import com.example.demo.entity.Person;
import com.example.demo.service.PersonService;
import io.github.diovamny.quarkus.inertia.api.Inertia;

@Path("/persons")
public class PersonResource {

    @Inject
    Inertia inertia;

    @Inject
    PersonService personService;

    @GET
    @Blocking
    public Uni<Object> index(
            @QueryParam("search") String search,
            @QueryParam("active") String activeStr,
            @QueryParam("sort") String sort,
            @QueryParam("order") String order,
            @QueryParam("page") @DefaultValue("1") int page) {
        Boolean active = null;
        if ("true".equals(activeStr)) active = true;
        else if ("false".equals(activeStr)) active = false;

        var result = personService.findPaginated(search, active, sort, order, page - 1, 15);

        inertia.cache("total_persons", Duration.ofSeconds(30), () ->
            Uni.createFrom().item(personService.countAll()));

        return inertia.render("Persons/Index", Map.of(
            "persons", result.items(),
            "total", result.total(),
            "page", result.page() + 1,
            "size", result.size(),
            "search", search != null ? search : "",
            "active", activeStr != null ? activeStr : "",
            "sort", sort != null ? sort : "",
            "order", order != null ? order : ""
        ));
    }

    @GET
    @Path("/scroll")
    @Blocking
    public Uni<Object> scrollTest(@QueryParam("page") @DefaultValue("1") int page) {
        var size = 5;
        var result = personService.findPaginated(null, null, null, null, page - 1, size);
        var lastPage = (int) Math.max(1, Math.ceil(result.total() / (double) size));

        var metadata = new java.util.HashMap<String, Object>();
        metadata.put("previousPage", page > 1 ? page - 1 : null);
        metadata.put("nextPage", page < lastPage ? page + 1 : null);
        metadata.put("currentPage", page);
        metadata.put("pageName", "persons_page");
        inertia.scroll("persons", result.items(), metadata);

        return inertia.render("Persons/Index", Map.of(
            "total", result.total()
        ));
    }

    @GET
    @Path("/create")
    public Uni<Object> createForm() {
        return inertia.render("Persons/Form", Map.of("person", new Person(), "editing", false));
    }

    @POST
    @Path("/test-redirect")
    public Response testRedirect() {
        return Response.status(302).header("Location", "/persons").build();
    }

    @POST
    @Path("/test-blocking-redirect")
    @Blocking
    public Response testBlockingRedirect() {
        return Response.status(302).header("Location", "/persons").build();
    }

    @POST
    @Path("/test-uni-redirect")
    @Blocking
    public Uni<Response> testUniRedirect() {
        return Uni.createFrom().item(
            Response.status(302).header("Location", "/persons").build()
        );
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @Blocking
    public Response store(@Valid PersonForm form) {
        var person = new Person();
        person.name = form.name;
        person.lastName = form.lastName;
        person.email = form.email;
        person.phone = form.phone;
        person.active = form.active != null ? form.active : true;
        personService.create(person);

        inertia.flash("success", "Persona creada correctamente");
        return (Response) inertia.redirect("/persons").await().indefinitely();
    }

    @GET
    @Path("/{id}/edit")
    @Blocking
    public Uni<Object> editForm(@PathParam("id") Long id) {
        var person = personService.findById(id);
        if (person == null) {
            inertia.flash("error", "Persona no encontrada");
            return inertia.render("Persons/Index", Map.of(
                "persons", java.util.List.of(), "total", 0L, "page", 1, "size", 15,
                "search", "", "active", "", "sort", "", "order", ""));
        }
        return inertia.render("Persons/Form", Map.of("person", person, "editing", true));
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @Blocking
    public Response update(@PathParam("id") Long id, @Valid PersonForm form) {
        var existing = personService.findById(id);
        if (existing == null) {
            inertia.flash("error", "Persona no encontrada");
            return (Response) inertia.redirect("/persons").await().indefinitely();
        }

        existing.name = form.name;
        existing.lastName = form.lastName;
        existing.email = form.email;
        existing.phone = form.phone;
        existing.active = form.active != null ? form.active : true;
        personService.update(id, existing);

        inertia.flash("success", "Persona actualizada correctamente");
        return (Response) inertia.redirect("/persons").await().indefinitely();
    }

    @POST
    @Path("/{id}/delete")
    @Transactional
    @Blocking
    public Response delete(@PathParam("id") Long id) {
        personService.delete(id);
        inertia.flash("success", "Persona eliminada correctamente");
        return (Response) inertia.redirect("/persons").await().indefinitely();
    }

    public static class PersonForm {
        @jakarta.validation.constraints.NotBlank
        public String name;

        @jakarta.validation.constraints.NotBlank
        public String lastName;

        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Email
        public String email;

        public String phone;

        public Boolean active;
    }
}
