package com.example.kitchensink.controller.crm;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kitchensink.entity.Note;
import com.example.kitchensink.repository.ContactRepository;
import com.example.kitchensink.repository.NoteRepository;
import com.example.kitchensink.repository.OrganizationRepository;
import com.example.kitchensink.repository.UserRepository;
import com.example.kitchensink.service.Resources;
import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class DashboardController {

    private final Inertia inertia;
    private final ContactRepository contacts;
    private final OrganizationRepository organizations;
    private final NoteRepository notes;
    private final UserRepository users;

    public DashboardController(Inertia inertia, ContactRepository contacts,
            OrganizationRepository organizations, NoteRepository notes, UserRepository users) {
        this.inertia = inertia;
        this.contacts = contacts;
        this.organizations = organizations;
        this.notes = notes;
        this.users = users;
    }

    @GetMapping("/dashboard")
    public Object index() {
        inertia.deferred("totalContacts", "totalContacts", () -> contacts.count());
        inertia.deferred("totalOrganizations", "totalOrganizations", () -> organizations.count());
        inertia.deferred("recentNotesCount", "recentNotesCount",
            () -> notes.countByCreatedAtGreaterThanEqual(Instant.now().minus(Duration.ofDays(7))));

        var recent = notes.findTop10ByOrderByCreatedAtDesc();
        var activity = new ArrayList<Object>();
        for (var note : recent) {
            activity.add(toNote(note));
        }

        return inertia.render("Crm/Dashboard", Map.of("recentActivity", activity));
    }

    private Map<String, Object> toNote(Note note) {
        var contact = note.contactId != null
            ? contacts.findById(note.contactId).orElse(null) : null;
        var user = note.userId != null ? users.findById(note.userId).orElse(null) : null;
        return Resources.note(note,
            contact != null ? Resources.contact(contact, orgOf(contact.organizationId)) : null,
            user != null ? Resources.user(user.id, user.name, user.email) : null);
    }

    private Map<String, Object> orgOf(Long organizationId) {
        if (organizationId == null) return null;
        var org = organizations.findById(organizationId).orElse(null);
        if (org == null) return null;
        return Resources.organization(org.id, org.name, null);
    }
}