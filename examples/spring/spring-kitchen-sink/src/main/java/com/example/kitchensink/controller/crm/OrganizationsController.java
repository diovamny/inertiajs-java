package com.example.kitchensink.controller.crm;

import java.util.ArrayList;
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

import com.example.kitchensink.dto.FormValidator;
import com.example.kitchensink.dto.OrganizationForm;
import com.example.kitchensink.entity.Organization;
import com.example.kitchensink.repository.OrganizationRepository;
import com.example.kitchensink.service.CrmQueryService;
import com.example.kitchensink.service.CursorPagination;
import com.example.kitchensink.service.LaravelPagination;
import com.example.kitchensink.service.Resources;
import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class OrganizationsController {

    private static final int PAGE_SIZE = 20;
    private static final int CONTACTS_PAGE_SIZE = 15;

    private final Inertia inertia;
    private final OrganizationRepository organizationRepository;
    private final CrmQueryService query;
    private final Validator validator;

    public OrganizationsController(Inertia inertia, OrganizationRepository organizationRepository,
            CrmQueryService query, Validator validator) {
        this.inertia = inertia;
        this.organizationRepository = organizationRepository;
        this.query = query;
        this.validator = validator;
    }

    @GetMapping("/organizations")
    public Object index(@RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        var offset = (long) (page - 1) * PAGE_SIZE;
        var total = query.organizationsCount(search);
        var orgs = query.organizations(search, offset, PAGE_SIZE);

        var items = new ArrayList<Object>();
        for (var org : orgs) {
            items.add(Resources.organization(org.id, org.name,
                (long) query.organizationContactsCount(org.id)));
        }

        var pagination = LaravelPagination.of("/organizations", items, total, page, PAGE_SIZE,
            Map.of("search", search == null ? "" : search));

        var filters = new LinkedHashMap<String, Object>();
        filters.put("search", search);

        return inertia.render("Organizations/Index", Map.of(
            "organizations", LaravelPagination.payload(pagination),
            "filters", filters));
    }

    @GetMapping("/organizations/{id}")
    public Object show(@PathVariable("id") long id,
            @RequestParam(value = "cursor", required = false) String cursor) {
        var organization = find(id);
        if (organization == null) {
            return inertia.redirect("/organizations").with("message", "Organization not found.");
        }

        var offset = CursorPagination.decode(cursor);
        var total = query.organizationContactsCount(id);
        var contacts = query.organizationContacts(id, offset, CONTACTS_PAGE_SIZE);

        var names = query.organizationNames();
        var items = new ArrayList<Object>();
        for (var contact : contacts) {
            var orgName = contact.organizationId != null ? names.get(contact.organizationId) : null;
            items.add(Resources.contact(contact,
                contact.organizationId != null && orgName != null
                    ? Resources.organization(contact.organizationId, orgName, null) : null));
        }

        var payload = CursorPagination.payload("/organizations/" + id, items, offset,
            CONTACTS_PAGE_SIZE, total, Map.of());

        var metadata = new LinkedHashMap<String, Object>();
        metadata.put("pageName", "cursor");
        metadata.put("previousPage", payload.get("prev_cursor"));
        metadata.put("nextPage", payload.get("next_cursor"));
        metadata.put("currentPage", offset == 0 ? 1 : cursor);

        inertia.scroll("contacts", payload, metadata);

        return inertia.render("Organizations/Show", Map.of(
            "organization", Resources.organization(organization.id, organization.name,
                (long) query.organizationContactsCount(id)),
            "contacts", payload));
    }

    @PostMapping(value = "/organizations/{id}", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public Object updateViaPost(@PathVariable("id") long id, @ModelAttribute OrganizationForm form) {
        if ("DELETE".equalsIgnoreCase(form._method)) {
            return destroy(id);
        }
        return doUpdate(id, form);
    }

    @PutMapping(value = "/organizations/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object update(@PathVariable("id") long id, @RequestBody OrganizationForm form) {
        return doUpdate(id, form);
    }

    private Object doUpdate(long id, OrganizationForm form) {
        var organization = find(id);
        if (organization == null) {
            return inertia.redirect("/organizations").with("message", "Organization not found.");
        }
        form.name = FormValidator.blankToNull(form.name);
        FormValidator.validate(validator, form);
        organization.name = form.name;
        organization.updatedAt = java.time.Instant.now();
        organizationRepository.save(organization);
        return inertia.back().with("message", "Organization updated.");
    }

    @DeleteMapping("/organizations/{id}")
    public Object destroy(@PathVariable("id") long id) {
        var organization = find(id);
        if (organization == null) {
            return inertia.redirect("/organizations").with("message", "Organization not found.");
        }
        organizationRepository.delete(organization);
        return inertia.redirect("/organizations").with("message", "Organization deleted.");
    }

    private Organization find(long id) {
        return organizationRepository.findById(id).orElse(null);
    }
}
