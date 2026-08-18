package com.example.kitchensink.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.example.kitchensink.dto.ContactForm;
import com.example.kitchensink.entity.Contact;
import com.example.kitchensink.entity.Note;
import com.example.kitchensink.repository.ContactRepository;

@ApplicationScoped
public class CrmWriteService {

    @Inject
    ContactRepository contactRepository;

    @Transactional
    public Contact create(ContactForm form) {
        var contact = new Contact();
        apply(form, contact);
        contact.createdAt = java.time.Instant.now();
        contact.updatedAt = contact.createdAt;
        contact.persist();
        return contact;
    }

    @Transactional
    public void update(Contact contact, ContactForm form) {
        apply(form, contact);
        contact.updatedAt = java.time.Instant.now();
        contactRepository.getEntityManager().merge(contact);
    }

    @Transactional
    public void toggleFavorite(Contact contact) {
        contact.isFavorite = !contact.isFavorite;
        contact.updatedAt = java.time.Instant.now();
        contactRepository.getEntityManager().merge(contact);
    }

    @Transactional
    public void delete(Contact contact) {
        contact.delete();
    }

    @Transactional
    public Note createNote(Long contactId, Long userId, String body) {
        var note = new Note();
        note.contactId = contactId;
        note.userId = userId;
        note.body = body;
        note.createdAt = java.time.Instant.now();
        note.updatedAt = note.createdAt;
        note.persist();
        return note;
    }

    private void apply(ContactForm form, Contact contact) {
        contact.firstName = form.first_name;
        contact.lastName = form.last_name;
        contact.email = form.email;
        contact.phone = form.phone;
        contact.organizationId = parseId(form.organization_id);
    }

    private Long parseId(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}