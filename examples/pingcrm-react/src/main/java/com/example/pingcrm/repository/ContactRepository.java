package com.example.pingcrm.repository;

import com.example.pingcrm.entity.Contact;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ContactRepository implements PanacheRepository<Contact> {

    public Contact findByIdWithTrashed(Long id) {
        return find("id", id).firstResult();
    }

    public java.util.List<Contact> findByOrganizationOrderedByName(Long accountId, Long organizationId) {
        return find("accountId = ?1 and organizationId = ?2 and deletedAt is null order by lastName, firstName",
                accountId, organizationId).list();
    }

    @Transactional
    public void softDelete(Contact contact) {
        contact.deletedAt = java.time.Instant.now();
        getEntityManager().merge(contact);
    }

    @Transactional
    public void restore(Contact contact) {
        contact.deletedAt = null;
        getEntityManager().merge(contact);
    }
}
