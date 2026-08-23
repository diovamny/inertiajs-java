package com.example.pingcrm.repository;

import com.example.pingcrm.entity.Contact;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public interface ContactRepository extends PanacheRepository<Contact> {
    
    // Paginated search
    default Uni<List<Contact>> findByAccountIdAndSearch(Long accountId, String search, int page, int size) {
        if (search != null && !search.isBlank()) {
            return find("accountId = ?1 and (lower(firstName) like ?2 or lower(lastName) like ?2 or lower(email) like ?2) and deletedAt is null", 
                    Parameters.with("accountId", accountId).and("search", "%" + search.toLowerCase() + "%"))
                    .page(Page.of(page, size))
                    .list();
        }
        return find("accountId = ?1 and deletedAt is null", accountId)
                .page(Page.of(page, size))
                .list();
    }
    
    default Uni<List<Contact>> findByAccountId(Long accountId, int page, int size) {
        return find("accountId = ?1 and deletedAt is null", accountId)
                .page(Page.of(page, size))
                .list();
    }
    
    default Uni<Long> countByAccountIdAndSearch(Long accountId, String search) {
        if (search != null && !search.isBlank()) {
            return count("accountId = ?1 and (lower(firstName) like ?2 or lower(lastName) like ?2 or lower(email) like ?2) and deletedAt is null",
                    Parameters.with("accountId", accountId).and("search", "%" + search.toLowerCase() + "%"));
        }
        return count("accountId = ?1 and deletedAt is null", accountId);
    }
    
    default Uni<Long> countByAccountId(Long accountId) {
        return count("accountId = ?1 and deletedAt is null", accountId);
    }
    
    default Uni<Boolean> existsByAccountIdAndEmail(Long accountId, String email) {
        return count("accountId = ?1 and email = ?2 and deletedAt is null", Parameters.with("accountId", accountId).and("email", email))
                .map(count -> count > 0);
    }
    
    Uni<Contact> findByAccountIdAndId(Long accountId, Long id);
}