package com.example.pingcrm.controller;

import com.example.pingcrm.dto.OrganizationForm;
import com.example.pingcrm.dto.FormValidator;
import com.example.pingcrm.entity.Organization;
import com.example.pingcrm.service.AuthService;
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
@RequestMapping("/organizations")
public class OrganizationsController {

    private final Inertia inertia;
    private final AuthService auth;
    private final OrganizationService organizations;
    private final Validator validator;

    public OrganizationsController(Inertia inertia, AuthService auth, OrganizationService organizations, Validator validator) {
        this.inertia = inertia;
        this.auth = auth;
        this.organizations = organizations;
        this.validator = validator;
    }

    @GetMapping
    public Object index(@RequestParam(required = false) String search,
                        @RequestParam(required = false) String trashed,
                        @RequestParam(defaultValue = "1") int page) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        var result = organizations.page(accountId, search, PageRequest.of(page - 1, 10, Sort.by("id").descending()));
        var filters = new LinkedHashMap<String, Object>();
        filters.put("search", search);
        filters.put("trashed", trashed);
        return inertia.render("Organizations/Index", Map.of(
            "filters", filters,
            "organizations", result));
    }

    @GetMapping("/create")
    public Object create() {
        return inertia.render("Organizations/Create");
    }

    @PostMapping(value = "/organizations", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object store(@RequestBody OrganizationForm form) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        FormValidator.normalize(form);
        FormValidator.validate(validator, form);
        organizations.create(accountId, FormValidator.toValues(form));
        return inertia.redirect("/organizations").with("success", "Organization created.");
    }

    @GetMapping("/{id}/edit")
    public Object edit(@PathVariable long id) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        var organization = organizations.findById(id);
        if (organization == null || !organization.accountId.equals(accountId)) {
            return notFound();
        }
        return inertia.render("Organizations/Edit", Map.of("organization", organization));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object update(@PathVariable long id, @RequestBody OrganizationForm form) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        var organization = organizations.findById(id);
        if (organization == null || !organization.accountId.equals(accountId)) {
            return notFound();
        }
        FormValidator.normalize(form);
        FormValidator.validate(validator, form);
        organizations.update(organization, FormValidator.toValues(form));
        return inertia.back().with("success", "Organization updated.");
    }

    @DeleteMapping("/{id}")
    public Object destroy(@PathVariable long id) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        var organization = organizations.findById(id);
        if (organization == null || !organization.accountId.equals(accountId)) {
            return notFound();
        }
        organizations.softDelete(organization);
        return inertia.back().with("success", "Organization deleted.");
    }

    @PutMapping("/{id}/restore")
    public Object restore(@PathVariable long id) {
        var accountId = auth.currentUser().map(u -> u.accountId).orElse(0L);
        var organization = organizations.findById(id);
        if (organization == null || !organization.accountId.equals(accountId)) {
            return notFound();
        }
        organizations.restore(organization);
        return inertia.back().with("success", "Organization restored.");
    }

    private Object notFound() {
        return inertia.redirect("/organizations").with("error", "Organization not found.");
    }
}