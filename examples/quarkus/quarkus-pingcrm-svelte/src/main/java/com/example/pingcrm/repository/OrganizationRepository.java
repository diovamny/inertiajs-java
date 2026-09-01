package com.example.pingcrm.repository;

import com.example.pingcrm.entity.Organization;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public class OrganizationRepository implements PanacheRepository<Organization> {

    public List<Organization> findByAccountIdAndSearch(Long accountId, String search, int page, int size) {
        if (search != null && !search.isBlank()) {
            return find("accountId = ?1 and (lower(name) like ?2 or lower(email) like ?2 or lower(city) like ?2) and deletedAt is null",
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
            return count("accountId = ?1 and (lower(name) like ?2 or lower(email) like ?2 or lower(city) like ?2) and deletedAt is null",
                    accountId, "%" + search.toLowerCase() + "%");
        }
        return count("accountId = ?1 and deletedAt is null", accountId);
    }

    public long countByAccountId(Long accountId) {
        return count("accountId = ?1 and deletedAt is null", accountId);
    }

    public Organization findByAccountIdAndId(Long accountId, Long id) {
        return find("accountId = ?1 and id = ?2", accountId, id).firstResult();
    }

    public Organization findByIdWithTrashed(Long id) {
        return findById(id);
    }

    public List<Map<String, Object>> findOptionsByAccountId(Long accountId) {
        return find("accountId = ?1 and deletedAt is null order by name", accountId)
                .list()
                .stream()
                .map(o -> Map.<String, Object>of("id", o.id, "name", o.name))
                .collect(Collectors.toList());
    }

    public List<Organization> findByAccountOrderedByName(Long accountId) {
        return find("accountId = ?1 and deletedAt is null order by name", accountId).list();
    }

    public void softDelete(Organization org) {
        org.deletedAt = java.time.Instant.now();
        getEntityManager().merge(org);
    }

    public void restore(Organization org) {
        org.deletedAt = null;
        getEntityManager().merge(org);
    }
}
