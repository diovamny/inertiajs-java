package com.example.pingcrm.service;

import java.util.Map;

import com.example.pingcrm.entity.Contact;
import com.example.pingcrm.repository.ContactRepository;
import com.example.pingcrm.repository.OrganizationRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ContactService {

    private final ContactRepository contacts;
    private final OrganizationRepository organizations;

    public ContactService(ContactRepository contacts, OrganizationRepository organizations) {
        this.contacts = contacts;
        this.organizations = organizations;
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

    public Contact create(Long accountId, Map<String, Object> values) {
        var contact = new Contact();
        contact.accountId = accountId;
        apply(contact, values);
        contact.createdAt = java.time.Instant.now();
        contact.updatedAt = contact.createdAt;
        contacts.save(contact);
        return contact;
    }

    public void update(Contact contact) {
        contacts.save(contact);
    }

    public void update(Contact contact, Map<String, Object> values) {
        apply(contact, values);
        contact.updatedAt = java.time.Instant.now();
        contacts.save(contact);
    }

    public boolean organizationBelongsToAccount(Long accountId, Long organizationId) {
        if (organizationId == null) return true;
        var organization = organizations.findById(organizationId).orElse(null);
        return organization != null && organization.accountId.equals(accountId);
    }

    private void apply(Contact contact, Map<String, Object> values) {
        contact.firstName = (String) values.get("first_name");
        contact.lastName = (String) values.get("last_name");
        contact.organizationId = (Long) values.get("organization_id");
        contact.email = (String) values.get("email");
        contact.phone = (String) values.get("phone");
        contact.address = (String) values.get("address");
        contact.city = (String) values.get("city");
        contact.region = (String) values.get("region");
        contact.country = (String) values.get("country");
        contact.postalCode = (String) values.get("postal_code");
    }

    public void softDelete(Contact contact) {
        contact.deletedAt = java.time.Instant.now();
        contacts.save(contact);
    }

    public void restore(Contact contact) {
        contact.deletedAt = null;
        contacts.save(contact);
    }
}
