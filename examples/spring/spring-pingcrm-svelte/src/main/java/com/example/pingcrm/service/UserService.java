package com.example.pingcrm.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.pingcrm.entity.User;
import com.example.pingcrm.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    @PersistenceContext
    private EntityManager em;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<Map<String, Object>> list(Long accountId, String search, String role, String trashed) {
        var cb = em.getCriteriaBuilder();

        var cq = cb.createQuery(User.class);
        var root = cq.from(User.class);
        var predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get("accountId"), accountId));
        applyFilters(cb, root, predicates, search, role, trashed);
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("lastName")), cb.asc(root.get("firstName")));

        return em.createQuery(cq).getResultList().stream()
            .map(this::toListItem)
            .toList();
    }

    @Transactional
    public User create(Long accountId, String firstName, String lastName, String email,
            String password, boolean owner, String photoPath) {
        var user = new User();
        user.accountId = accountId;
        user.firstName = firstName;
        user.lastName = lastName;
        user.email = email;
        user.password = password != null && !password.isBlank() ? AuthService.hash(password) : null;
        user.owner = owner;
        user.photoPath = photoPath;
        user.createdAt = Instant.now();
        user.updatedAt = user.createdAt;
        em.persist(user);
        return user;
    }

    @Transactional
    public void update(User user, String firstName, String lastName, String email,
            String password, boolean owner, String photoPath) {
        user.firstName = firstName;
        user.lastName = lastName;
        user.email = email;
        user.owner = owner;
        if (password != null && !password.isBlank()) {
            user.password = AuthService.hash(password);
        }
        if (photoPath != null) {
            user.photoPath = photoPath;
        }
        user.updatedAt = Instant.now();
        em.merge(user);
    }

    @Transactional
    public void softDelete(User user) {
        user.deletedAt = Instant.now();
        em.merge(user);
    }

    @Transactional
    public void restore(User user) {
        user.deletedAt = null;
        em.merge(user);
    }

    private Map<String, Object> toListItem(User user) {
        var item = new LinkedHashMap<String, Object>();
        item.put("id", user.id);
        item.put("name", user.getName());
        item.put("email", user.email);
        item.put("owner", user.owner);
        item.put("photo", user.photoPath != null
            ? "/img/" + user.photoPath + "?w=40&h=40&fit=crop" : null);
        item.put("deleted_at", user.deletedAt);
        return item;
    }

    private void applyFilters(CriteriaBuilder cb, Root<?> root,
            List<Predicate> predicates, String search, String role, String trashed) {
        if (search != null && !search.isBlank()) {
            var pattern = "%" + search.toLowerCase() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern)));
        }
        if ("user".equals(role)) {
            predicates.add(cb.isFalse(root.get("owner")));
        } else if ("owner".equals(role)) {
            predicates.add(cb.isTrue(root.get("owner")));
        }
        if (trashed == null || trashed.isBlank()) {
            predicates.add(cb.isNull(root.get("deletedAt")));
        } else if ("only".equals(trashed)) {
            predicates.add(cb.isNotNull(root.get("deletedAt")));
        }
    }
}