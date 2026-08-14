package com.example.kitchensink.controller.feature;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import com.example.kitchensink.dto.DottedKeysRequest;
import com.example.kitchensink.dto.FormValidator;
import com.example.kitchensink.dto.PrecognitionRequest;
import com.example.kitchensink.dto.SimpleFormRequest;
import com.example.kitchensink.dto.ValidationErrorBagRequest;
import com.example.kitchensink.dto.ValidationRequest;
import com.example.kitchensink.entity.Contact;
import com.example.kitchensink.service.CrmQueryService;
import com.example.kitchensink.service.CrmWriteService;
import com.example.kitchensink.service.Demo;
import com.example.kitchensink.service.Resources;
import com.quarkus.inertia.api.Inertia;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

@Path("/features/forms")
@Blocking
public class FormController {

    @Inject
    Inertia inertia;

    @Inject
    Validator validator;

    @Inject
    CrmQueryService query;

    @Inject
    CrmWriteService write;

    @GET
    @Path("use-form")
    @Blocking
    public Uni<Object> useForm() {
        return inertia.render("Features/Forms/UseForm");
    }

    @POST
    @Path("use-form")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> submitUseForm(SimpleFormRequest form) {
        validateSimple(form);
        return inertia.back().with("message", "Form submitted successfully! Name: " + form.name);
    }

    @GET
    @Path("form-component")
    @Blocking
    public Uni<Object> formComponent() {
        return inertia.render("Features/Forms/FormComponent");
    }

    @POST
    @Path("form-component")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> submitFormComponent(SimpleFormRequest form) {
        validateSimple(form);
        return inertia.back().with("message", "Form submitted successfully! Name: " + form.name);
    }

    @GET
    @Path("file-uploads")
    @Blocking
    public Uni<Object> fileUploads() {
        return inertia.render("Features/Forms/FileUploads");
    }

    @POST
    @Path("file-uploads")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Blocking
    public Uni<Object> submitFileUploads(@RestForm("files") List<FileUpload> files,
        @RestForm("photo") FileUpload photo) {
        var fileList = files == null ? List.<FileUpload>of() : files;
        if (fileList.size() > 5) {
            return inertia.back().withErrors(Map.of("files",
                "The files field must not have more than 5 items."));
        }
        var count = fileList.size() + (photo != null ? 1 : 0);
        return inertia.back().with("message", "Uploaded " + count + " file(s) successfully!");
    }


    @GET
    @Path("validation")
    @Blocking
    public Uni<Object> validation() {
        return inertia.render("Features/Forms/Validation");
    }

    @POST
    @Path("validation")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> submitValidation(ValidationRequest form) {
        form.name = FormValidator.blankToNull(form.name);
        form.email = FormValidator.blankToNull(form.email);
        form.website = FormValidator.blankToNull(form.website);
        FormValidator.validate(validator, form);
        return inertia.back().with("message", "Primary form submitted successfully!");
    }

    @POST
    @Path("validation/secondary")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> submitValidationSecondary(ValidationErrorBagRequest form) {
        form.title = FormValidator.blankToNull(form.title);
        form.body = FormValidator.blankToNull(form.body);
        FormValidator.validate(validator, form);
        return inertia.back().with("message", "Secondary form submitted successfully!");
    }

    private static final Set<String> PRECOGNITION_FIELDS =
        Set.of("username", "email", "password", "password_confirmation");

    @GET
    @Path("precognition")
    @Blocking
    public Uni<Object> precognition() {
        return inertia.render("Features/Forms/Precognition");
    }

    @POST
    @Path("precognition")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> storeAccount(PrecognitionRequest form) {
        form.username = FormValidator.blankToNull(form.username);
        form.email = FormValidator.blankToNull(form.email);
        form.password = FormValidator.blankToNull(form.password);
        form.password_confirmation = FormValidator.blankToNull(form.password_confirmation);

        if (isPrecognition()) {
            return precognitionResponse(form);
        }

        FormValidator.validate(validator, form);
        if (form.password != null && !form.password.equals(form.password_confirmation)) {
            return inertia.back().withErrors(Map.of(
                "password_confirmation", "The password confirmation does not match."));
        }
        return inertia.back().with("message", "Account created for " + form.username + "!");
    }

    /**
     * Field-by-field validation for precognition requests: only the fields
     * listed in {@code Precognition-Validate-Only} are validated (the
     * {@code password}/{@code password_confirmation} pair always together).
     * Responds 204 when every validated field is valid, or 422 with the
     * errors wrapped as {@code {"errors": {...}}} — the laravel-precognition
     * client contract.
     */
    private Uni<Object> precognitionResponse(PrecognitionRequest form) {
        var ctx = Vertx.currentContext();
        var validateOnly = ctx != null
            ? (String) ctx.getLocal("inertia-precognition-validate-fields")
            : null;
        var fields = FormValidator.precognitionFields(validateOnly, PRECOGNITION_FIELDS);
        var errors = FormValidator.fieldErrors(validator, form, fields);
        if (fields.contains("password_confirmation")
                && form.password != null && !form.password.equals(form.password_confirmation)) {
            errors.put("password_confirmation", "The password confirmation does not match.");
        }
        if (errors.isEmpty()) {
            return Uni.createFrom().item(Response.noContent()
                .header("Precognition", "true")
                .header("Precognition-Success", "true")
                .build());
        }
        return Uni.createFrom().item(Response.status(422)
            .entity(Map.of("errors", errors))
            .type(MediaType.APPLICATION_JSON_TYPE)
            .header("Precognition", "true")
            .header("Vary", "Precognition")
            .build());
    }

    private boolean isPrecognition() {
        var ctx = Vertx.currentContext();
        return ctx != null && Boolean.TRUE.equals(ctx.getLocal("inertia-precognition"));
    }

    @GET
    @Path("optimistic-updates")
    @Blocking
    public Uni<Object> optimisticUpdates() {
        return inertia.render("Features/Forms/OptimisticUpdates", Map.of("contacts", latestContacts()));
    }

    @POST
    @Path("optimistic-toggle/{contact}")
    @Blocking
    public Uni<Object> toggleFavorite(@PathParam("contact") long contactId,
            @QueryParam("simulate_error") boolean simulateError) {
        Demo.sleepSeconds(1);
        if (simulateError) {
            return inertia.back().withErrors(Map.of(
                "contact", "Simulated validation error for optimistic update rollback demo."));
        }
        var contact = (Contact) Contact.findById(contactId);
        if (contact == null) {
            return inertia.back().withErrors(Map.of(
                "contact", "The selected contact is invalid."));
        }
        write.toggleFavorite(contact);
        return inertia.back().with("message",
            contact.isFavorite ? "Added to favorites." : "Removed from favorites.");
    }

    @GET
    @Path("use-form-context")
    @Blocking
    public Uni<Object> useFormContext() {
        return inertia.render("Features/Forms/UseFormContext");
    }

    @GET
    @Path("dotted-keys")
    @Blocking
    public Uni<Object> dottedKeys() {
        return inertia.render("Features/Forms/DottedKeys");
    }

    @POST
    @Path("dotted-keys")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> submitDottedKeys(DottedKeysRequest form) {
        FormValidator.validate(validator, form);
        var parsed = new LinkedHashMap<String, Object>();
        if (form.user != null) {
            parsed.put("user", Map.of(
                "name", form.user.name,
                "email", form.user.email));
        }
        if (form.address != null) {
            parsed.put("address", Map.of(
                "street", form.address.street,
                "city", form.address.city));
        }
        parsed.put("tags", form.tags != null ? form.tags : java.util.List.of());
        return inertia.back().with("message", "Form submitted successfully!").with("parsedData", parsed);
    }

    private void validateSimple(SimpleFormRequest form) {
        form.name = FormValidator.blankToNull(form.name);
        form.email = FormValidator.blankToNull(form.email);
        form.bio = FormValidator.blankToNull(form.bio);
        form.role = FormValidator.blankToNull(form.role);
        FormValidator.validate(validator, form);
    }

    private Object latestContacts() {
        var contacts = Contact.find("order by id desc").page(0, 10).list();
        var names = query.organizationNames();
        var items = new ArrayList<Object>();
        for (var c : contacts) {
            Contact contact = (Contact) c;
            var orgName = contact.organizationId != null ? names.get(contact.organizationId) : null;
            items.add(Resources.contact(contact,
                contact.organizationId != null && orgName != null
                    ? Resources.organization(contact.organizationId, orgName, null) : null));
        }
        return items;
    }
}