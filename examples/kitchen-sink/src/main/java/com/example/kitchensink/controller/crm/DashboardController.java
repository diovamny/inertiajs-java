package com.example.kitchensink.controller.crm;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

import com.example.kitchensink.entity.Note;
import com.example.kitchensink.repository.ContactRepository;
import com.example.kitchensink.repository.NoteRepository;
import com.example.kitchensink.repository.OrganizationRepository;
import com.example.kitchensink.repository.UserRepository;
import com.example.kitchensink.service.Resources;
import io.github.dg.quarkus.inertia.api.Inertia;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/dashboard")
@Blocking
public class DashboardController {

    @Inject
    Inertia inertia;

    @Inject
    ContactRepository contacts;

    @Inject
    OrganizationRepository organizations;

    @Inject
    NoteRepository notes;

    @Inject
    UserRepository users;

    @GET
    @Blocking
    public Uni<Object> index() {
        inertia.deferred("totalContacts", () -> Uni.createFrom().item(contacts.count()));
        inertia.deferred("totalOrganizations", () -> Uni.createFrom().item(organizations.count()));
        inertia.deferred("recentNotesCount", () -> Uni.createFrom()
            .item(notes.countSince(java.time.Instant.now().minus(java.time.Duration.ofDays(7)))));

        var recent = notes.find("order by createdAt desc").page(0, 10).list();
        var activity = new ArrayList<Object>();
        for (var note : recent) {
            activity.add(toNote(note));
        }

        return inertia.render("Crm/Dashboard", Map.of("recentActivity", activity));
    }

    private Map<String, Object> toNote(Note note) {
        var contact = note.contactId != null
            ? (com.example.kitchensink.entity.Contact) com.example.kitchensink.entity.Contact.findById(note.contactId)
            : null;
        var user = note.userId != null ? (com.example.kitchensink.entity.User) users.findById(note.userId) : null;
        return Resources.note(note,
            contact != null ? Resources.contact(contact, orgOf(contact.organizationId)) : null,
            user != null ? Resources.user(user.id, user.name, user.email) : null);
    }

    private Map<String, Object> orgOf(Long organizationId) {
        if (organizationId == null) return null;
        var org = (com.example.kitchensink.entity.Organization) com.example.kitchensink.entity.Organization
            .findById(organizationId);
        if (org == null) return null;
        return Resources.organization(org.id, org.name, null);
    }
}