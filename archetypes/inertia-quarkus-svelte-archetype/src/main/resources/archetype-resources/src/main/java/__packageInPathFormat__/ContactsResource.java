package ${package};

import io.github.diovamny.quarkus.inertia.api.Inertia;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Contact CRUD demonstrating the Inertia starter contract: pages render
 * components, mutations redirect with flash data, and validation failures
 * go back with per-field errors and the submitted input.
 */
@Path("/contacts")
public class ContactsResource {

    @Inject
    Inertia inertia;

    @Inject
    ContactRepository repository;

    @Inject
    Validator validator;

    @GET
    public Uni<Object> index() {
        return inertia.render("Contacts/Index", Map.of("contacts", repository.findAll()));
    }

    @GET
    @Path("create")
    public Uni<Object> create() {
        return inertia.render("Contacts/Create", Map.of());
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Uni<Object> store(@FormParam("name") String name,
            @FormParam("email") String email,
            @FormParam("phone") String phone) {
        var form = new ContactForm(name, email, phone);
        var violations = validator.validate(form);
        if (!violations.isEmpty()) {
            return inertia.back()
                .withErrors(fieldErrors(violations))
                .withInput(inputOf(form));
        }
        repository.create(form);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contacto creado con éxito");
    }

    @GET
    @Path("{id}/edit")
    public Uni<Object> edit(@PathParam("id") long id) {
        var contact = repository.find(id);
        if (contact == null) {
            return inertia.back()
                .withFlash("error", "El contacto no existe");
        }
        return inertia.render("Contacts/Edit", Map.of("contact", contact));
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Uni<Object> update(@PathParam("id") long id,
            @FormParam("name") String name,
            @FormParam("email") String email,
            @FormParam("phone") String phone) {
        var form = new ContactForm(name, email, phone);
        var violations = validator.validate(form);
        if (!violations.isEmpty()) {
            return inertia.back()
                .withErrors(fieldErrors(violations))
                .withInput(inputOf(form));
        }
        if (!repository.update(id, form)) {
            return inertia.redirect("/contacts")
                .withFlash("error", "El contacto no existe");
        }
        return inertia.redirect("/contacts")
            .withFlash("success", "Contacto actualizado con éxito");
    }

    @DELETE
    @Path("{id}")
    public Uni<Object> destroy(@PathParam("id") long id) {
        if (!repository.delete(id)) {
            return inertia.redirect("/contacts")
                .withFlash("error", "El contacto no existe");
        }
        return inertia.redirect("/contacts")
            .withFlash("success", "Contacto eliminado con éxito");
    }

    private static Map<String, String> fieldErrors(Set<? extends ConstraintViolation<?>> violations) {
        var errors = new LinkedHashMap<String, String>();
        for (var violation : violations) {
            var path = violation.getPropertyPath().toString();
            var field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
            errors.putIfAbsent(field, violation.getMessage());
        }
        return errors;
    }

    private static Map<String, Object> inputOf(ContactForm form) {
        var input = new LinkedHashMap<String, Object>();
        input.put("name", form.name());
        input.put("email", form.email());
        input.put("phone", form.phone());
        return input;
    }
}
