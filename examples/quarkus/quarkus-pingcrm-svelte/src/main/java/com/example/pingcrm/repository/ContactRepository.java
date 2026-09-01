package com.example.pingcrm.repository;

import com.example.pingcrm.entity.Contact;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class ContactRepository implements PanacheRepository<Contact> {

    public List<Contact> findByAccountIdAndSearch(Long accountId, String search, int page, int size) {
        if (search != null && !search.isBlank()) {
            return find("accountId = ?1 and (lower(firstName) like ?2 or lower(lastName) like ?2 or lower(email) like ?2) and deletedAt is null",
                    accountId, "%" + search.toLowerCase() + "%")
                    .page(page, size)
                    .list();
        }
        return find("accountId = ?1 and deletedAt is null", accountId)
                .page(page, size)
                .list();
    }

    public long countByAccountIdAndSearch(Long accountId, String search) {
        if (search != null && !search.isBlank()) {
            return count("accountId = ?1 and (lower(firstName) like ?2 or lower(lastName) like ?2 or lower(email) like ?2) and deletedAt is null",
                    accountId, "%" + search.toLowerCase() + "%");
        }
        return count("accountId = ?1 and deletedAt is null", accountId);
    }

    public long countByAccountId(Long accountId) {
        return count("accountId = ?1 and deletedAt is null", accountId);
    }

    public boolean existsByAccountIdAndEmail(Long accountId, String email) {
        return count("accountId = ?1 and email = ?2 and deletedAt is null", accountId, email) > 0;
    }

    public Contact findByAccountIdAndId(Long accountId, Long id) {
        return find("accountId = ?1 and id = ?2", accountId, id).firstResult();
    }

    public Contact findByIdWithTrashed(Long id) {
        return findById(id);
    }

    public List<Contact> findByOrganizationOrderedByName(Long accountId, Long orgId) {
        return find("accountId = ?1 and organizationId = ?2 and deletedAt is null order by firstName, lastName",
                accountId, orgId).list();
    }

    public void softDelete(Contact contact) {
        contact.deletedAt = java.time.Instant.now();
        getEntityManager().merge(contact);
    }

    public void restore(Contact contact) {
        contact.deletedAt = null;
        getEntityManager().merge(contact);
    }
}
