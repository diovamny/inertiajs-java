package com.example.pingcrm.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.example.pingcrm.dto.ContactForm;
import com.example.pingcrm.dto.FormValidator;
import com.example.pingcrm.entity.Contact;
import com.example.pingcrm.repository.ContactRepository;
import com.example.pingcrm.service.AuthService;
import com.example.pingcrm.service.ContactService;
import com.example.pingcrm.service.OrganizationService;
import com.quarkus.inertia.api.Inertia;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/contacts")

@Blocking
public class ContactsController {

    @Inject
    Inertia inertia;

    @Inject
    AuthService auth;

    @Inject
    ContactService contacts;

    @Inject
    ContactRepository contactRepository;

    @Inject
    OrganizationService organizations;

    @Inject
    Validator validator;

    @GET
    @Blocking
    public Uni<Object> index(@QueryParam("search") String search,
            @QueryParam("trashed") String trashed,
            @QueryParam("page") @DefaultValue("1") int page) {
        var accountId = auth.accountId();
        var result = contacts.page(accountId, search, trashed, page, 10);
        var filters = new LinkedHashMap<String, Object>();
        filters.put("search", search);
        filters.put("trashed", trashed);
        return inertia.render("Contacts/Index", Map.of(
            "filters", filters,
            "contacts", result));
    }

    @GET
    @Path("create")
    @Blocking
    public Uni<Object> create() {
        return inertia.render("Contacts/Create",
            Map.of("organizations", organizations.options(auth.accountId())));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> store(ContactForm form) {
        normalize(form);
        FormValidator.validate(validator, form);
        if (!contacts.organizationBelongsToAccount(auth.accountId(), parseId(form.organization_id))) {
            return invalidOrganization();
        }
        contacts.create(auth.accountId(), toValues(form));
        return inertia.redirect("/contacts").with("success", "Contact created.");
    }

    @GET
    @Path("{id}/edit")
    @Blocking
    public Uni<Object> edit(@PathParam("id") long id) {
        var contact = findOwned(id);
        if (contact == null) {
            return notFound();
        }
        return inertia.render("Contacts/Edit", Map.of(
            "contact", contact,
            "organizations", organizations.options(auth.accountId())));
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> update(@PathParam("id") long id, ContactForm form) {
        var contact = findOwned(id);
        if (contact == null) {
            return notFound();
        }
        normalize(form);
        FormValidator.validate(validator, form);
        if (!contacts.organizationBelongsToAccount(auth.accountId(), parseId(form.organization_id))) {
            return invalidOrganization();
        }
        contacts.update(contact, toValues(form));
        return inertia.back().with("success", "Contact updated.");
    }

    @DELETE
    @Path("{id}")
    @Blocking
    public Uni<Object> destroy(@PathParam("id") long id) {
        var contact = findOwned(id);
        if (contact == null) {
            return notFound();
        }
        contacts.softDelete(contact);
        return inertia.back().with("success", "Contact deleted.");
    }

    @PUT
    @Path("{id}/restore")
    @Blocking
    public Uni<Object> restore(@PathParam("id") long id) {
        var contact = contactRepository.findByIdWithTrashed(id);
        if (contact == null || !contact.accountId.equals(auth.accountId())) {
            return notFound();
        }
        contacts.restore(contact);
        return inertia.back().with("success", "Contact restored.");
    }

    private Contact findOwned(long id) {
        var contact = contactRepository.findByIdWithTrashed(id);
        if (contact == null || !contact.accountId.equals(auth.accountId())) {
            return null;
        }
        return contact;
    }

    private Uni<Object> notFound() {
        return inertia.redirect("/contacts").with("error", "Contact not found.");
    }

    private Uni<Object> invalidOrganization() {
        return inertia.back("/contacts").withErrors(Map.of("organization_id", "The selected organization is invalid."));
    }

    private Long parseId(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void normalize(ContactForm form) {
        form.first_name = FormValidator.blankToNull(form.first_name);
        form.last_name = FormValidator.blankToNull(form.last_name);
        form.organization_id = FormValidator.blankToNull(form.organization_id);
        form.email = FormValidator.blankToNull(form.email);
        form.phone = FormValidator.blankToNull(form.phone);
        form.address = FormValidator.blankToNull(form.address);
        form.city = FormValidator.blankToNull(form.city);
        form.region = FormValidator.blankToNull(form.region);
        form.country = FormValidator.blankToNull(form.country);
        form.postal_code = FormValidator.blankToNull(form.postal_code);
    }

    private Map<String, String> toValues(ContactForm form) {
        var values = new LinkedHashMap<String, String>();
        values.put("first_name", form.first_name);
        values.put("last_name", form.last_name);
        values.put("organization_id", form.organization_id);
        values.put("email", form.email);
        values.put("phone", form.phone);
        values.put("address", form.address);
        values.put("city", form.city);
        values.put("region", form.region);
        values.put("country", form.country);
        values.put("postal_code", form.postal_code);
        return values;
    }
}
