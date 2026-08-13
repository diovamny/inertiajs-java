package com.example.kitchensink.controller.crm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.example.kitchensink.dto.FormValidator;
import com.example.kitchensink.dto.OrganizationForm;
import com.example.kitchensink.entity.Organization;
import com.example.kitchensink.repository.OrganizationRepository;
import com.example.kitchensink.service.CrmQueryService;
import com.example.kitchensink.service.CursorPagination;
import com.example.kitchensink.service.LaravelPagination;
import com.example.kitchensink.service.Resources;
import com.quarkus.inertia.api.Inertia;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;
import jakarta.transaction.Transactional;

@Path("/organizations")
@Blocking
public class OrganizationsController {

    private static final int PAGE_SIZE = 20;
    private static final int CONTACTS_PAGE_SIZE = 15;

    @Inject
    Inertia inertia;

    @Inject
    OrganizationRepository organizationRepository;

    @Inject
    CrmQueryService query;

    @Inject
    Validator validator;

    @GET
    @Blocking
    public Uni<Object> index(@QueryParam("search") String search,
            @QueryParam("page") @DefaultValue("1") int page) {
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

    @GET
    @Path("{id}")
    @Blocking
    public Uni<Object> show(@PathParam("id") long id,
            @QueryParam("cursor") String cursor) {
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

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @Blocking
    public Uni<Object> update(@PathParam("id") long id, OrganizationForm form) {
        var organization = find(id);
        if (organization == null) {
            return inertia.redirect("/organizations").with("message", "Organization not found.");
        }
        form.name = FormValidator.blankToNull(form.name);
        FormValidator.validate(validator, form);
        organization.name = form.name;
        organization.updatedAt = java.time.Instant.now();
        organizationRepository.getEntityManager().merge(organization);
        return inertia.back().with("message", "Organization updated.");
    }

    private Organization find(long id) {
        return organizationRepository.findById(id);
    }
}