package com.example.kitchensink.service;

import java.time.Instant;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kitchensink.dto.ContactForm;
import com.example.kitchensink.entity.Contact;
import com.example.kitchensink.entity.Note;
import com.example.kitchensink.repository.ContactRepository;
import com.example.kitchensink.repository.NoteRepository;

@Service
public class CrmWriteService {

    private final ContactRepository contactRepository;
    private final NoteRepository noteRepository;

    public CrmWriteService(ContactRepository contactRepository, NoteRepository noteRepository) {
        this.contactRepository = contactRepository;
        this.noteRepository = noteRepository;
    }

    @Transactional
    public Contact create(ContactForm form) {
        var contact = new Contact();
        apply(form, contact);
        contact.createdAt = Instant.now();
        contact.updatedAt = contact.createdAt;
        contactRepository.save(contact);
        return contact;
    }

    @Transactional
    public void update(Contact contact, ContactForm form) {
        apply(form, contact);
        contact.updatedAt = Instant.now();
        contactRepository.save(contact);
    }

    @Transactional
    public void toggleFavorite(Contact contact) {
        contact.isFavorite = !contact.isFavorite;
        contact.updatedAt = Instant.now();
        contactRepository.save(contact);
    }

    @Transactional
    public void delete(Contact contact) {
        contactRepository.delete(contact);
    }

    @Transactional
    public Note createNote(Long contactId, Long userId, String body) {
        var note = new Note();
        note.contactId = contactId;
        note.userId = userId;
        note.body = body;
        note.createdAt = Instant.now();
        note.updatedAt = note.createdAt;
        noteRepository.save(note);
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