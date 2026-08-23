package com.example.pingcrm.controller;

import com.example.pingcrm.dto.ContactForm;
import com.example.pingcrm.dto.FormValidator;
import com.example.pingcrm.entity.Contact;
import com.example.pingcrm.service.AuthService;
import com.example.pingcrm.service.ContactService;
import com.example.pingcrm.service.OrganizationService;
import io.github.dg.spring.inertia.api.Inertia;
import jakarta.validation.Validator;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/contacts")
public class ContactsController {

    private final Inertia inertia;
    private final AuthService auth;
    private final ContactService contacts;
    private final OrganizationService organizations;
    private final Validator validator;

    public ContactsController(Inertia inertia, AuthService auth, ContactService contacts,
                              OrganizationService organizations, Validator validator) {
        this.inertia = inertia;
        this.auth = auth;
        this.contacts = contacts;
        this.organizations = organizations;
        this.validator = validator;
    }

    @GetMapping
    public Object index(@RequestParam(required = false) String search,
                        @RequestParam(required = false) String trashed,
                        @RequestParam(defaultValue = "1") int page) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        var result = contacts.page(accountId, search, PageRequest.of(page - 1, 10, Sort.by("id").descending()));
        var filters = new LinkedHashMap<String, Object>();
        filters.put("search", search);
        filters.put("trashed", trashed);
        return inertia.render("Contacts/Index", Map.of(
            "filters", filters,
            "contacts", result));
    }

    @GetMapping("/create")
    public Object create() {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        return inertia.render("Contacts/Create",
            Map.of("organizations", organizations.options(accountId)));
    }

    @PostMapping(value = "/contacts", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object store(@RequestBody ContactForm form) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        FormValidator.normalize(form);
        FormValidator.validate(validator, form);
        if (!contacts.organizationBelongsToAccount(accountId, FormValidator.parseId(form.organization_id))) {
            return invalidOrganization();
        }
        contacts.create(accountId, FormValidator.toValues(form));
        return inertia.redirect("/contacts").with("success", "Contact created.");
    }

    @GetMapping("/{id}/edit")
    public Object edit(@PathVariable long id) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        var contact = contacts.findById(id);
        if (contact == null || !contact.accountId.equals(accountId)) {
            return notFound();
        }
        return inertia.render("Contacts/Edit", Map.of(
            "contact", contact,
            "organizations", organizations.options(accountId)));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object update(@PathVariable long id, @RequestBody ContactForm form) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        var contact = contacts.findById(id);
        if (contact == null || !contact.accountId.equals(accountId)) {
            return notFound();
        }
        FormValidator.normalize(form);
        FormValidator.validate(validator, form);
        if (!contacts.organizationBelongsToAccount(accountId, FormValidator.parseId(form.organization_id))) {
            return invalidOrganization();
        }
        contacts.update(contact, FormValidator.toValues(form));
        return inertia.back().with("success", "Contact updated.");
    }

    @DeleteMapping("/{id}")
    public Object destroy(@PathVariable long id) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        var contact = contacts.findById(id);
        if (contact == null || !contact.accountId.equals(accountId)) {
            return notFound();
        }
        contacts.softDelete(contact);
        return inertia.back().with("success", "Contact deleted.");
    }

    @PutMapping("/{id}/restore")
    public Object restore(@PathVariable long id) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        var contact = contacts.findById(id);
        if (contact == null || !contact.accountId.equals(accountId)) {
            return notFound();
        }
        contacts.restore(contact);
        return inertia.back().with("success", "Contact restored.");
    }

    private Object notFound() {
        return inertia.redirect("/contacts").with("error", "Contact not found.");
    }

    private Object invalidOrganization() {
        return inertia.back()
            .withErrors(Map.of("organization_id", "The selected organization is invalid."));
    }
}