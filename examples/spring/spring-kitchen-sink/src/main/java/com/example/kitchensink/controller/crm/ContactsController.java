package com.example.kitchensink.controller.crm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.validation.Validator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kitchensink.dto.ContactForm;
import com.example.kitchensink.dto.FormValidator;
import com.example.kitchensink.entity.Contact;
import com.example.kitchensink.entity.Note;
import com.example.kitchensink.entity.User;
import com.example.kitchensink.repository.ContactRepository;
import com.example.kitchensink.repository.NoteRepository;
import com.example.kitchensink.repository.UserRepository;
import com.example.kitchensink.service.CrmQueryService;
import com.example.kitchensink.service.CrmWriteService;
import com.example.kitchensink.service.CursorPagination;
import com.example.kitchensink.service.Resources;
import io.github.diovamny.spring.inertia.api.Inertia;

@RestController
public class ContactsController {

    private static final int PAGE_SIZE = 15;

    private final Inertia inertia;
    private final ContactRepository contactRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final CrmQueryService query;
    private final CrmWriteService write;
    private final Validator validator;

    public ContactsController(Inertia inertia, ContactRepository contactRepository,
            NoteRepository noteRepository, UserRepository userRepository,
            CrmQueryService query, CrmWriteService write, Validator validator) {
        this.inertia = inertia;
        this.contactRepository = contactRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.query = query;
        this.write = write;
        this.validator = validator;
    }

    @GetMapping("/contacts")
    public Object index(@RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "favorite", defaultValue = "false") String favoriteRaw,
            @RequestParam(value = "cursor", required = false) String cursor) {
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

    @GetMapping("/contacts/create")
    public Object create() {
        return inertia.render("Contacts/Create", query.organizationOptions());
    }

    @PostMapping(value = "/contacts", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object store(@RequestBody ContactForm form) {
        normalize(form);
        FormValidator.validate(validator, form);
        var contact = write.create(form);
        return inertia.redirect("/contacts/" + contact.id).with("message", "Contact created.");
    }

    @GetMapping("/contacts/{id}")
    public Object show(@PathVariable("id") long id) {
        var contact = find(id);
        if (contact == null) {
            return inertia.redirect("/contacts").with("message", "Contact not found.");
        }
        var orgName = contact.organizationId != null
            ? query.organizationNames().get(contact.organizationId) : null;
        var contactResource = Resources.contact(contact,
            contact.organizationId != null && orgName != null
                ? Resources.organization(contact.organizationId, orgName, null) : null);

        inertia.deferred("notes", "notes", () -> notes(contact.id));

        return inertia.render("Contacts/Show", Map.of("contact", contactResource));
    }

    @GetMapping("/contacts/{id}/edit")
    public Object edit(@PathVariable("id") long id) {
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

    @PostMapping(value = "/contacts/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object updateViaPost(@PathVariable("id") long id, @ModelAttribute ContactForm form) {
        if ("DELETE".equalsIgnoreCase(form._method)) {
            return destroy(id);
        }
        return doUpdate(id, form);
    }

    @PutMapping(value = "/contacts/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object update(@PathVariable("id") long id, @RequestBody ContactForm form) {
        return doUpdate(id, form);
    }

    private Object doUpdate(long id, ContactForm form) {
        var contact = find(id);
        if (contact == null) {
            return inertia.redirect("/contacts").with("message", "Contact not found.");
        }
        normalize(form);
        FormValidator.validate(validator, form);
        write.update(contact, form);
        return inertia.redirect("/contacts/" + contact.id).with("message", "Contact updated.");
    }

    @DeleteMapping("/contacts/{id}")
    public Object destroy(@PathVariable("id") long id) {
        var contact = find(id);
        if (contact == null) {
            return inertia.redirect("/contacts").with("message", "Contact not found.");
        }
        write.delete(contact);
        return inertia.redirect("/contacts").with("message", "Contact deleted.");
    }

    @PostMapping("/contacts/{id}/favorite")
    public Object favorite(@PathVariable("id") long id) {
        var contact = find(id);
        if (contact == null) {
            return inertia.redirect("/contacts").with("message", "Contact not found.");
        }
        write.toggleFavorite(contact);
        return inertia.back().with("message",
            contact.isFavorite ? "Added to favorites." : "Removed from favorites.");
    }

    private List<Object> notes(long contactId) {
        var notes = noteRepository.findByContactIdOrderByCreatedAtDesc(contactId);
        var items = new ArrayList<Object>();
        for (var note : notes) {
            var user = note.userId != null ? userRepository.findById(note.userId).orElse(null) : null;
            items.add(Resources.note(note, null,
                user != null ? Resources.user(user.id, user.name, user.email) : null));
        }
        return items;
    }

    private Contact find(long id) {
        return contactRepository.findById(id).orElse(null);
    }

    private void normalize(ContactForm form) {
        form.first_name = FormValidator.blankToNull(form.first_name);
        form.last_name = FormValidator.blankToNull(form.last_name);
        form.email = FormValidator.blankToNull(form.email);
        form.phone = FormValidator.blankToNull(form.phone);
        form.organization_id = FormValidator.blankToNull(form.organization_id);
    }

    private boolean isTruthy(String value) {
        return value != null && !value.isBlank() && !"0".equals(value) && !"false".equalsIgnoreCase(value);
    }
}
