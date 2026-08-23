package com.example.pingcrm.service;

import com.example.pingcrm.entity.Contact;
import com.example.pingcrm.repository.ContactRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ContactService {

    private final ContactRepository contacts;

    public ContactService(ContactRepository contacts) {
        this.contacts = contacts;
    }

    public Page<Contact> page(Long accountId, String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return contacts.findByAccountIdAndSearch(accountId, search, pageable);
        }
        return contacts.findByAccountId(accountId, pageable);
    }

    public Contact findById(Long id) {
        return contacts.findById(id).orElse(null);
    }

    public void create(Contact contact) {
        contacts.save(contact);
    }

    public void update(Contact contact) {
        contacts.save(contact);
    }

    public void softDelete(Contact contact) {
        contact.deletedAt = java.time.LocalDateTime.now();
        contacts.save(contact);
    }

    public void restore(Contact contact) {
        contact.deletedAt = null;
        contacts.save(contact);
    }
}