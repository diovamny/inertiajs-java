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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.pingcrm.dto.FormValidator;
import com.example.pingcrm.dto.OrganizationForm;
import com.example.pingcrm.entity.Organization;
import com.example.pingcrm.repository.OrganizationRepository;
import com.example.pingcrm.service.AuthService;
import com.example.pingcrm.service.OrganizationService;
import io.github.diovamny.spring.inertia.api.Inertia;

@RestController
@RequestMapping("/organizations")
public class OrganizationsController {

    private final Inertia inertia;
    private final AuthService auth;
    private final OrganizationService organizations;
    private final OrganizationRepository organizationRepository;
    private final Validator validator;

    public OrganizationsController(Inertia inertia, AuthService auth, OrganizationService organizations,
            OrganizationRepository organizationRepository, Validator validator) {
        this.inertia = inertia;
        this.auth = auth;
        this.organizations = organizations;
        this.organizationRepository = organizationRepository;
        this.validator = validator;
    }

    @GetMapping
    public Object index(@RequestParam(required = false) String search,
            @RequestParam(required = false) String trashed,
            @RequestParam(defaultValue = "1") int page) {
        var result = organizations.page(auth.accountId(), search, trashed, page, 10);
        return inertia.render("Organizations/Index", Map.of(
            "filters", filters(search, trashed),
            "organizations", result));
    }

    @GetMapping("/create")
    public Object create() {
        return inertia.render("Organizations/Create", Map.of());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object store(@RequestBody OrganizationForm form) {
        normalize(form);
        FormValidator.validate(validator, form);
        organizations.create(auth.accountId(), toValues(form));
        return inertia.redirect("/organizations").with("success", "Organization created.");
    }

    @GetMapping("/{id}/edit")
    public Object edit(@PathVariable long id) {
        var organization = findOwned(id);
        if (organization == null) {
            return notFound();
        }
        return inertia.render("Organizations/Edit",
            Map.of("organization", organizations.editData(organization)));
    }

    @PostMapping(value = "/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object updateViaPost(@PathVariable long id, @ModelAttribute OrganizationForm form) {
        if ("DELETE".equalsIgnoreCase(form._method)) {
            return destroy(id);
        }
        return doUpdate(id, form);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object update(@PathVariable long id, @RequestBody OrganizationForm form) {
        return doUpdate(id, form);
    }

    private Object doUpdate(long id, OrganizationForm form) {
        var organization = findOwned(id);
        if (organization == null) {
            return notFound();
        }
        normalize(form);
        FormValidator.validate(validator, form);
        organizations.update(organization, toValues(form));
        return inertia.back().with("success", "Organization updated.");
    }

    @DeleteMapping("/{id}")
    public Object destroy(@PathVariable long id) {
        var organization = findOwned(id);
        if (organization == null) {
            return notFound();
        }
        organizations.softDelete(organization);
        return inertia.back().with("success", "Organization deleted.");
    }

    @PutMapping("/{id}/restore")
    public Object restore(@PathVariable long id) {
        var organization = organizationRepository.findById(id).orElse(null);
        if (organization == null || !organization.accountId.equals(auth.accountId())) {
            return notFound();
        }
        organizations.restore(organization);
        return inertia.back().with("success", "Organization restored.");
    }

    private Organization findOwned(long id) {
        var organization = organizationRepository.findById(id).orElse(null);
        if (organization == null || !organization.accountId.equals(auth.accountId())) {
            return null;
        }
        return organization;
    }

    private Object notFound() {
        return inertia.redirect("/organizations").with("error", "Organization not found.");
    }

    private LinkedHashMap<String, Object> filters(String search, String trashed) {
        var filters = new LinkedHashMap<String, Object>();
        filters.put("search", search);
        filters.put("trashed", trashed);
        return filters;
    }

    private void normalize(OrganizationForm form) {
        form.name = FormValidator.blankToNull(form.name);
        form.email = FormValidator.blankToNull(form.email);
        form.phone = FormValidator.blankToNull(form.phone);
        form.address = FormValidator.blankToNull(form.address);
        form.city = FormValidator.blankToNull(form.city);
        form.region = FormValidator.blankToNull(form.region);
        form.country = FormValidator.blankToNull(form.country);
        form.postal_code = FormValidator.blankToNull(form.postal_code);
    }

    private Map<String, String> toValues(OrganizationForm form) {
        var values = new LinkedHashMap<String, String>();
        values.put("name", form.name);
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
