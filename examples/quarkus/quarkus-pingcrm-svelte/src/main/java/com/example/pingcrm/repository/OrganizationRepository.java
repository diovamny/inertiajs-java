package com.example.pingcrm.repository;

import com.example.pingcrm.entity.Organization;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ApplicationScoped
public interface OrganizationRepository extends PanacheRepository<Organization> {
    
    default Uni<List<Organization>> findByAccountIdAndSearch(Long accountId, String search, int page, int size) {
        if (search != null && !search.isBlank()) {
            return find("accountId = ?1 and (lower(name) like ?2 or lower(email) like ?2 or lower(city) like ?2) and deletedAt is null",
                    Parameters.with("accountId", accountId).and("search", "%" + search.toLowerCase() + "%"))
                    .page(Page.of(page, size))
                    .list();
        }
        return find("accountId = ?1 and deletedAt is null", accountId)
                .page(Page.of(page, size))
                .list();
    }
    
    default Uni<List<Organization>> findByAccountId(Long accountId, int page, int size) {
        return find("accountId = ?1 and deletedAt is null", accountId)
                .page(Page.of(page, size))
                .list();
    }
    
    default Uni<Long> countByAccountIdAndSearch(Long accountId, String search) {
        if (search != null && !search.isBlank()) {
            return count("accountId = ?1 and (lower(name) like ?2 or lower(email) like ?2 or lower(city) like ?2) and deletedAt is null",
                    Parameters.with("accountId", accountId).and("search", "%" + search.toLowerCase() + "%"));
        }
        return count("accountId = ?1 and deletedAt is null", accountId);
    }
    
    default Uni<Long> countByAccountId(Long accountId) {
        return count("accountId = ?1 and deletedAt is null", accountId);
    }
    
    Uni<Organization> findByAccountIdAndId(Long accountId, Long id);
    
    default Uni<List<Map<String, Object>>> findOptionsByAccountId(Long accountId) {
        return find("accountId = ?1 and deletedAt is null order by name", accountId)
                .list()
                .map(list -> list.stream()
                        .map(o -> Map.<String, Object>of("id", o.id, "name", o.name))
                        .collect(Collectors.toList()));
    }
}