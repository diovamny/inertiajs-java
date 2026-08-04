package com.example.pingcrm.repository;

import com.example.pingcrm.entity.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public User findByEmail(String email) {
        return find("email", email).firstResult();
    }

    /** Find including soft-deleted rows (used by unique-email checks and edit pages). */
    public User findByIdWithTrashed(Long id) {
        return find("id = ?1", id).firstResult();
    }

    public boolean emailExistsForOtherUser(String email, Long excludeUserId) {
        return count("email = ?1 and (?2 is null or id <> ?2)", email, excludeUserId) > 0;
    }

    @Transactional
    public void softDelete(User user) {
        user.deletedAt = java.time.Instant.now();
        getEntityManager().merge(user);
    }

    @Transactional
    public void restore(User user) {
        user.deletedAt = null;
        getEntityManager().merge(user);
    }
}
