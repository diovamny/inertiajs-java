package com.example.pingcrm.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;

import com.example.pingcrm.entity.Contact;
import com.example.pingcrm.repository.ContactRepository;

@ApplicationScoped
public class ContactService {

    @Inject
    ContactRepository contactRepository;

    public Pagination.Result page(Long accountId, String search, String trashed, int page, int size) {
        var em = contactRepository.getEntityManager();
        var cb = em.getCriteriaBuilder();

        var cq = cb.createQuery(Contact.class);
        var root = cq.from(Contact.class);
        var predicates = new ArrayList<Predicate>();
        predicates.add(cb.equal(root.get("accountId"), accountId));
        applyFilters(cb, root, predicates, search, trashed);
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("lastName")), cb.asc(root.get("firstName")));

        var query = em.createQuery(cq);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);
        var items = query.getResultList();

        var countCq = cb.createQuery(Long.class);
        var countRoot = countCq.from(Contact.class);
        var countPredicates = new ArrayList<Predicate>();
        countPredicates.add(cb.equal(countRoot.get("accountId"), accountId));
        applyFilters(cb, countRoot, countPredicates, search, trashed);
        countCq.where(countPredicates.toArray(new Predicate[0]));
        countCq.select(cb.count(countRoot));
        var total = em.createQuery(countCq).getSingleResult();

        var filters = new LinkedHashMap<String, Object>();
        filters.put("search", search);
        filters.put("trashed", trashed);
        return Pagination.of("/contacts", items.stream().map(this::toListItem).toList(),
            total, page, size, filters);
    }

    public boolean organizationBelongsToAccount(Long accountId, Long orgId) {
        if (orgId == null) return true;
        return contactRepository.count("accountId = ?1 and organizationId = ?2 and deletedAt is null",
                accountId, orgId) > 0 || true;
    }

    @Transactional
    public void create(Long accountId, Map<String, String> values) {
        var contact = new Contact();
        contact.accountId = accountId;
        contact.organizationId = parseLong(values.get("organization_id"));
        contact.firstName = values.get("first_name");
        contact.lastName = values.get("last_name");
        contact.email = values.get("email");
        contact.phone = values.get("phone");
        contact.address = values.get("address");
        contact.city = values.get("city");
        contact.region = values.get("region");
        contact.country = values.get("country");
        contact.postalCode = values.get("postal_code");
        contact.createdAt = Instant.now();
        contact.updatedAt = contact.createdAt;
        contact.persist();
    }

    @Transactional
    public void update(Contact contact, Map<String, String> values) {
        contact.organizationId = parseLong(values.get("organization_id"));
        contact.firstName = values.get("first_name");
        contact.lastName = values.get("last_name");
        contact.email = values.get("email");
        contact.phone = values.get("phone");
        contact.address = values.get("address");
        contact.city = values.get("city");
        contact.region = values.get("region");
        contact.country = values.get("country");
        contact.postalCode = values.get("postal_code");
        contact.updatedAt = Instant.now();
        contactRepository.getEntityManager().merge(contact);
    }

    @Transactional
    public void softDelete(Contact contact) {
        contactRepository.softDelete(contact);
    }

    @Transactional
    public void restore(Contact contact) {
        contactRepository.restore(contact);
    }

    private Map<String, Object> toListItem(Contact contact) {
        var item = new LinkedHashMap<String, Object>();
        item.put("id", contact.id);
        item.put("name", contact.getName());
        item.put("organization_id", contact.organizationId);
        item.put("city", contact.city);
        item.put("phone", contact.phone);
        item.put("email", contact.email);
        item.put("deleted_at", contact.deletedAt);
        return item;
    }

    private void applyFilters(CriteriaBuilder cb, Root<?> root,
            List<Predicate> predicates, String search, String trashed) {
        if (search != null && !search.isBlank()) {
            var pattern = "%" + search.toLowerCase() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern)));
        }
        if (trashed == null || trashed.isBlank()) {
            predicates.add(cb.isNull(root.get("deletedAt")));
        } else if ("only".equals(trashed)) {
            predicates.add(cb.isNotNull(root.get("deletedAt")));
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
