package com.example.pingcrm.repository;

import com.example.pingcrm.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public List<User> findByAccountIdAndSearch(Long accountId, String search, int page, int size) {
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

    public User findByAccountIdAndEmail(Long accountId, String email) {
        return find("accountId = ?1 and email = ?2 and deletedAt is null", accountId, email).firstResult();
    }

    public User findByEmail(String email) {
        return find("email = ?1 and deletedAt is null", email).firstResult();
    }

    public User findByIdWithTrashed(Long id) {
        return findById(id);
    }

    public boolean emailExistsForOtherUser(String email, Long excludeId) {
        if (excludeId != null) {
            return count("email = ?1 and id != ?2 and deletedAt is null", email, excludeId) > 0;
        }
        return count("email = ?1 and deletedAt is null", email) > 0;
    }

    public void softDelete(User user) {
        user.deletedAt = java.time.Instant.now();
        getEntityManager().merge(user);
    }

    public void restore(User user) {
        user.deletedAt = null;
        getEntityManager().merge(user);
    }
}
