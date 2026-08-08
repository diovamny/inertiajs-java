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
import jakarta.ws.rs.core.Response;

import com.example.pingcrm.dto.FormValidator;
import com.example.pingcrm.dto.OrganizationForm;
import com.example.pingcrm.repository.OrganizationRepository;
import com.example.pingcrm.service.AuthService;
import com.example.pingcrm.service.OrganizationService;
import com.quarkus.inertia.api.Inertia;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/organizations")

@Blocking
public class OrganizationsController {

    @Inject
    Inertia inertia;

    @Inject
    AuthService auth;

    @Inject
    OrganizationService organizations;

    @Inject
    OrganizationRepository organizationRepository;

    @Inject
    Validator validator;

    @GET
    @Blocking
    public Uni<Object> index(@QueryParam("search") String search,
            @QueryParam("trashed") String trashed,
            @QueryParam("page") @DefaultValue("1") int page) {
        var accountId = auth.accountId();
        var result = organizations.page(accountId, search, trashed, page, 10);
        return inertia.render("Organizations/Index", Map.of(
            "filters", filters(search, trashed),
            "organizations", result));
    }

    @GET
    @Path("create")
    public Uni<Object> create() {
        return inertia.render("Organizations/Create");
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> store(OrganizationForm form) {
        normalize(form);
        FormValidator.validate(validator, form);
        organizations.create(auth.accountId(), toValues(form));
        return inertia.redirect("/organizations").with("success", "Organization created.");
    }

    @GET
    @Path("{id}/edit")
    @Blocking
    public Uni<Object> edit(@PathParam("id") long id) {
        var organization = findOwned(id);
        if (organization == null) {
            return notFound();
        }
        return inertia.render("Organizations/Edit",
            Map.of("organization", organizations.editData(organization)));
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> update(@PathParam("id") long id, OrganizationForm form) {
        var organization = findOwned(id);
        if (organization == null) {
            return notFound();
        }
        normalize(form);
        FormValidator.validate(validator, form);
        organizations.update(organization, toValues(form));
        return inertia.back().with("success", "Organization updated.");
    }

    @DELETE
    @Path("{id}")
    @Blocking
    public Uni<Object> destroy(@PathParam("id") long id) {
        var organization = findOwned(id);
        if (organization == null) {
            return notFound();
        }
        organizations.softDelete(organization);
        return inertia.back().with("success", "Organization deleted.");
    }

    @PUT
    @Path("{id}/restore")
    @Blocking
    public Uni<Object> restore(@PathParam("id") long id) {
        var organization = organizationRepository.findByIdWithTrashed(id);
        if (organization == null || !organization.accountId.equals(auth.accountId())) {
            return notFound();
        }
        organizations.restore(organization);
        return inertia.back().with("success", "Organization restored.");
    }

    private com.example.pingcrm.entity.Organization findOwned(long id) {
        var organization = organizationRepository.findByIdWithTrashed(id);
        if (organization == null || !organization.accountId.equals(auth.accountId())) {
            return null;
        }
        return organization;
    }

    private Uni<Object> notFound() {
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
