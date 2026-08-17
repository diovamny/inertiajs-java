package io.github.dg.quarkus.inertia.protocol;

import jakarta.inject.Inject;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import io.smallrye.mutiny.Uni;

import io.github.dg.quarkus.inertia.api.Inertia;

@Path("/validation-test")
public class ValidationTestResource {

    public static class Form {
        @NotBlank(message = "required")
        public String name;
    }

    @Inject
    Inertia inertia;

    @GET
    public Uni<Object> form() {
        return inertia.render("Form", java.util.Map.of());
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Uni<Object> submit(@FormParam("name") String name) {
        var form = new Form();
        form.name = name;
        var violations = jakarta.validation.Validation.buildDefaultValidatorFactory().getValidator().validate(form);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return inertia.redirect("/validation-test");
    }
}
