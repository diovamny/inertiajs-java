package com.example.pingcrm.controller;

import com.example.pingcrm.dto.ContactForm;
import com.example.pingcrm.dto.FormValidator;
import com.example.pingcrm.repository.ContactRepository;
import com.example.pingcrm.repository.OrganizationRepository;
import com.example.pingcrm.service.AuthService;
import io.github.dg.quarkus.inertia.api.Inertia;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.quarkus.vertx.web.Param;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import io.vertx.ext.web.RoutingContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@RouteBase(path = "/contacts")
public class ContactsController {

    @Inject
    Inertia inertia;

    @Inject
    AuthService auth;

    @Inject
    ContactRepository contacts;

    @Inject
    OrganizationRepository organizations;

    @Inject
    Validator validator;

    @Route(path = "", methods = Route.HttpMethod.GET)
    public Uni<Object> index(@Param("search") String search, @Param("page") int page) {
        return auth.accountId()
            .onItem().transformToUni(accId -> {
                Long accountId = accId != null ? accId : 0L;
                int pageNum = page > 0 ? page : 1;
                return contacts.findByAccountIdAndSearch(accountId, search, pageNum - 1, 10)
                    .onItem().transformToUni(contactList -> 
                        contacts.countByAccountIdAndSearch(accountId, search)
                            .onItem().transform(total -> 
                                inertia.render("Contacts/Index", Map.of(
                                    "contacts", contactList,
                                    "total", total,
                                    "page", pageNum,
                                    "filters", Map.of("search", search != null ? search : "")
                                ))
                        )
                );
            });
    }

    @Route(path = "/create", methods = Route.HttpMethod.GET)
    public Uni<Object> create() {
        return auth.accountId()
            .onItem().transformToUni(accId -> {
                Long accountId = accId != null ? accId : 0L;
                return organizations.findOptionsByAccountId(accountId)
                    .onItem().transform(orgs -> 
                        inertia.render("Contacts/Create", Map.of("organizations", orgs))
                    );
            });
    }

    @Route(path = "", methods = Route.HttpMethod.POST)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Object> store(ContactForm form, RoutingContext rc) {
        FormValidator.normalize(form);
        
        var violations = validator.validate(form);
        if (!violations.isEmpty()) {
            var errors = new LinkedHashMap<String, String>();
            for (var v : violations) {
                errors.put(v.getPropertyPath().toString(), v.getMessage());
            }
            return Uni.createFrom().item(
                inertia.back().withErrors(errors).toResponse()
            );
        }
        
        var orgId = FormValidator.parseId(form.organization_id);
        if (orgId == null) {
            return Uni.createFrom().item(
                inertia.back().withErrors(Map.of("organization_id", "The selected organization is invalid.")).toResponse()
            );
        }
        
        return auth.accountId()
            .onItem().transformToUni(accountId -> {
                Long accId = accountId != null ? accountId : 0L;
                
                return organizations.count("id = ?1 and accountId = ?2 and deletedAt is null", orgId, accId)
                    .onItem().transformToUni(count -> {
                        if (count == 0) {
                            return Uni.createFrom().item(
                                inertia.back().withErrors(Map.of("organization_id", "The selected organization is invalid.")).toResponse()
                            );
                        }
            
                        var contact = new com.example.pingcrm.entity.Contact();
                        contact.firstName = form.first_name;
                        contact.lastName = form.last_name;
                        contact.email = form.email;
                        contact.phone = form.phone;
                        contact.address = form.address;
                        contact.city = form.city;
                        contact.region = form.region;
                        contact.country = form.country;
                        contact.postalCode = form.postal_code;
                        contact.organizationId = orgId;
                        contact.accountId = accountId != null ? accountId : 0L;
            
                        return contacts.persist(contact)
                            .replaceWith(inertia.redirect("/contacts").with("success", "Contact created.").toResponse());
                    });
            });
    }
}