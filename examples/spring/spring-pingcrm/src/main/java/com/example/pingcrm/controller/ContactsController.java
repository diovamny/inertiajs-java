package com.example.pingcrm.controller;

import java.util.LinkedHashMap;
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

import com.example.pingcrm.dto.ContactForm;
import com.example.pingcrm.dto.FormValidator;
import com.example.pingcrm.entity.Contact;
import com.example.pingcrm.repository.ContactRepository;
import com.example.pingcrm.service.AuthService;
import com.example.pingcrm.service.ContactService;
import com.example.pingcrm.service.OrganizationService;
import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class ContactsController {

    private final Inertia inertia;
    private final AuthService auth;
    private final ContactService contacts;
    private final ContactRepository contactRepository;
    private final OrganizationService organizations;
    private final Validator validator;

    public ContactsController(Inertia inertia, AuthService auth, ContactService contacts,
            ContactRepository contactRepository, OrganizationService organizations, Validator validator) {
        this.inertia = inertia;
        this.auth = auth;
        this.contacts = contacts;
        this.contactRepository = contactRepository;
        this.organizations = organizations;
        this.validator = validator;
    }

    @GetMapping("/contacts")
    public Object index(@RequestParam(required = false) String search,
            @RequestParam(required = false) String trashed,
            @RequestParam(defaultValue = "1") int page) {
        var accountId = auth.accountId();
        var result = contacts.page(accountId, search, trashed, page, 10);
        var filters = new LinkedHashMap<String, Object>();
        filters.put("search", search);
        filters.put("trashed", trashed);
        return inertia.render("Contacts/Index", Map.of(
            "filters", filters,
            "contacts", result));
    }

    @GetMapping("/contacts/create")
    public Object create() {
        return inertia.render("Contacts/Create",
            Map.of("organizations", organizations.options(auth.accountId())));
    }

    @PostMapping(value = "/contacts", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object store(@RequestBody ContactForm form) {
        normalize(form);
        FormValidator.validate(validator, form);
        if (!contacts.organizationBelongsToAccount(auth.accountId(), parseId(form.organization_id))) {
            return invalidOrganization();
        }
        contacts.create(auth.accountId(), toValues(form));
        return inertia.redirect("/contacts").with("success", "Contact created.");
    }

    @GetMapping("/contacts/{id}/edit")
    public Object edit(@PathVariable long id) {
        var contact = findOwned(id);
        if (contact == null) {
            return notFound();
        }
        return inertia.render("Contacts/Edit", Map.of(
            "contact", contact,
            "organizations", organizations.options(auth.accountId())));
    }

    @PostMapping(value = "/contacts/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object updateViaPost(@PathVariable long id, @ModelAttribute ContactForm form) {
        if ("DELETE".equalsIgnoreCase(form._method)) {
            return destroy(id);
        }
        return doUpdate(id, form);
    }

    @PutMapping(value = "/contacts/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object update(@PathVariable long id, @RequestBody ContactForm form) {
        return doUpdate(id, form);
    }

    private Object doUpdate(@PathVariable long id, ContactForm form) {
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

    @DeleteMapping("/contacts/{id}")
    public Object destroy(@PathVariable long id) {
        var contact = findOwned(id);
        if (contact == null) {
            return notFound();
        }
        contacts.softDelete(contact);
        return inertia.back().with("success", "Contact deleted.");
    }

    @PutMapping("/contacts/{id}/restore")
    public Object restore(@PathVariable long id) {
        var contact = contactRepository.findById(id).orElse(null);
        if (contact == null || !contact.accountId.equals(auth.accountId())) {
            return notFound();
        }
        contacts.restore(contact);
        return inertia.back().with("success", "Contact restored.");
    }

    private Contact findOwned(long id) {
        var contact = contactRepository.findById(id).orElse(null);
        if (contact == null || !contact.accountId.equals(auth.accountId())) {
            return null;
        }
        return contact;
    }

    private Object notFound() {
        return inertia.redirect("/contacts").with("error", "Contact not found.");
    }

    private Object invalidOrganization() {
        return inertia.back()
            .withErrors(Map.of("organization_id", "The selected organization is invalid."));
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