package com.example.pingcrm.repository;

import com.example.pingcrm.entity.Organization;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class OrganizationRepository implements PanacheRepository<Organization> {

    public Organization findByIdWithTrashed(Long id) {
        return find("id", id).firstResult();
    }

    public java.util.List<Organization> findByAccountOrderedByName(Long accountId) {
        return find("accountId = ?1 order by name", accountId).list();
    }

    @Transactional
    public void softDelete(Organization organization) {
        organization.deletedAt = java.time.Instant.now();
        getEntityManager().merge(organization);
    }

    @Transactional
    public void restore(Organization organization) {
        organization.deletedAt = null;
        getEntityManager().merge(organization);
    }
}
