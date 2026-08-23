package com.example.pingcrm.repository;

import com.example.pingcrm.entity.User;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Parameters;
import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public interface UserRepository extends PanacheRepository<User> {
    
    default Uni<List<User>> findByAccountIdAndSearch(Long accountId, String search, int page, int size) {
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
    
    default Uni<List<User>> findByAccountId(Long accountId, int page, int size) {
        return find("accountId = ?1 and deletedAt is null", accountId)
                .page(Page.of(page, size))
                .list();
    }
    
    default Uni<Long> countByAccountId(Long accountId) {
        return count("accountId = ?1 and deletedAt is null", accountId);
    }
    
    Uni<User> findByAccountIdAndEmail(Long accountId, String email);
    
    Uni<User> findByEmail(String email);
    
    Uni<User> findByAccountIdAndId(Long accountId, Long id);
    
    default Uni<Boolean> existsByAccountIdAndEmail(Long accountId, String email) {
        return count("accountId = ?1 and email = ?2 and deletedAt is null", 
                Parameters.with("accountId", accountId).and("email", email))
                .map(count -> count > 0);
    }
}