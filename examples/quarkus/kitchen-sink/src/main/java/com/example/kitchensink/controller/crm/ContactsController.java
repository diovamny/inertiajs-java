package com.example.kitchensink.controller.crm;

import java.util.ArrayList;
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

import com.example.kitchensink.dto.ContactForm;
import com.example.kitchensink.dto.FormValidator;
import com.example.kitchensink.entity.Contact;
import com.example.kitchensink.repository.ContactRepository;
import com.example.kitchensink.service.CrmQueryService;
import com.example.kitchensink.service.CrmWriteService;
import com.example.kitchensink.service.CursorPagination;
import com.example.kitchensink.service.Resources;
import io.github.dg.quarkus.inertia.api.Inertia;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/contacts")
@Blocking
public class ContactsController {

    private static final int PAGE_SIZE = 15;

    @Inject
    Inertia inertia;

    @Inject
    ContactRepository contactRepository;

    @Inject
    CrmQueryService query;

    @Inject
    CrmWriteService write;

    @Inject
    Validator validator;

    @GET
    @Blocking
    public Uni<Object> index(@QueryParam("search") String search,
            @QueryParam("favorite") @DefaultValue("false") String favoriteRaw,
            @QueryParam("cursor") String cursor) {
        var favorite = isTruthy(favoriteRaw);
        var offset = CursorPagination.decode(cursor);
        var total = query.contactsCount(search, favorite);
        var contacts = query.contacts(search, favorite, offset, PAGE_SIZE);

        var names = query.organizationNames();
        var items = new ArrayList<Object>();
        for (var contact : contacts) {
            var orgName = contact.organizationId != null ? names.get(contact.organizationId) : null;
            items.add(Resources.contact(contact,
                contact.organizationId != null && orgName != null
                    ? Resources.organization(contact.organizationId, orgName, null) : null));
        }

        var filters = new LinkedHashMap<String, Object>();
        filters.put("search", search);
        filters.put("favorite", favorite);

        var payload = CursorPagination.payload("/contacts", items, offset, PAGE_SIZE, total,
            Map.of("search", search == null ? "" : search, "favorite", favorite ? "1" : ""));

        var metadata = new LinkedHashMap<String, Object>();
        metadata.put("pageName", "cursor");
        metadata.put("previousPage", payload.get("prev_cursor"));
        metadata.put("nextPage", payload.get("next_cursor"));
        metadata.put("currentPage", offset == 0 ? 1 : cursor);

        inertia.scroll("contacts", payload, metadata);

        return inertia.render("Contacts/Index", Map.of(
            "contacts", payload,
            "filters", filters));
    }

    @GET
    @Path("create")
    @Blocking
    public Uni<Object> create() {
        return inertia.render("Contacts/Create", query.organizationOptions());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> store(ContactForm form) {
        normalize(form);
        FormValidator.validate(validator, form);
        var contact = write.create(form);
        return inertia.redirect("/contacts/" + contact.id).with("message", "Contact created.");
    }

    @GET
    @Path("{id}")
    @Blocking
    public Uni<Object> show(@PathParam("id") long id) {
        var contact = find(id);
        if (contact == null) {
            return inertia.redirect("/contacts").with("message", "Contact not found.");
        }
        var orgName = contact.organizationId != null
            ? query.organizationNames().get(contact.organizationId) : null;
        var contactResource = Resources.contact(contact,
            contact.organizationId != null && orgName != null
                ? Resources.organization(contact.organizationId, orgName, null) : null);

        inertia.deferred("notes", () -> Uni.createFrom().item(notes(contact.id)));

        return inertia.render("Contacts/Show", Map.of("contact", contactResource));
    }

    @GET
    @Path("{id}/edit")
    @Blocking
    public Uni<Object> edit(@PathParam("id") long id) {
        var contact = find(id);
        if (contact == null) {
            return inertia.redirect("/contacts").with("message", "Contact not found.");
        }
        var orgName = contact.organizationId != null
            ? query.organizationNames().get(contact.organizationId) : null;
        var contactResource = Resources.contact(contact,
            contact.organizationId != null && orgName != null
                ? Resources.organization(contact.organizationId, orgName, null) : null);
        var props = new LinkedHashMap<String, Object>();
        props.put("contact", contactResource);
        props.putAll(query.organizationOptions());
        return inertia.render("Contacts/Edit", props);
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> update(@PathParam("id") long id, ContactForm form) {
        var contact = find(id);
        if (contact == null) {
            return inertia.redirect("/contacts").with("message", "Contact not found.");
        }
        normalize(form);
        FormValidator.validate(validator, form);
        write.update(contact, form);
        return inertia.redirect("/contacts/" + contact.id).with("message", "Contact updated.");
    }

    @DELETE
    @Path("{id}")
    @Blocking
    public Uni<Object> destroy(@PathParam("id") long id) {
        var contact = find(id);
        if (contact == null) {
            return inertia.redirect("/contacts").with("message", "Contact not found.");
        }
        write.delete(contact);
        return inertia.redirect("/contacts").with("message", "Contact deleted.");
    }

    @POST
    @Path("{id}/favorite")
    @Blocking
    public Uni<Object> favorite(@PathParam("id") long id) {
        var contact = find(id);
        if (contact == null) {
            return inertia.redirect("/contacts").with("message", "Contact not found.");
        }
        write.toggleFavorite(contact);
        return inertia.back().with("message",
            contact.isFavorite ? "Added to favorites." : "Removed from favorites.");
    }

    private java.util.List<Object> notes(long contactId) {
        var notes = com.example.kitchensink.entity.Note.find("contactId = ?1 order by createdAt desc", contactId).list();
        var items = new ArrayList<Object>();
        for (var note : notes) {
            com.example.kitchensink.entity.Note n = (com.example.kitchensink.entity.Note) note;
            var user = n.userId != null
                ? (com.example.kitchensink.entity.User) com.example.kitchensink.entity.User.findById(n.userId)
                : null;
            items.add(Resources.note(n, null,
                user != null ? Resources.user(user.id, user.name, user.email) : null));
        }
        return items;
    }

    private Contact find(long id) {
        return contactRepository.findById(id);
    }

    private void normalize(ContactForm form) {
        form.first_name = FormValidator.blankToNull(form.first_name);
        form.last_name = FormValidator.blankToNull(form.last_name);
        form.email = FormValidator.blankToNull(form.email);
        form.phone = FormValidator.blankToNull(form.phone);
        form.organization_id = FormValidator.blankToNull(form.organization_id);
    }

    private Long parseId(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isTruthy(String value) {
        return value != null && !value.isBlank() && !"0".equals(value) && !"false".equalsIgnoreCase(value);
    }
}