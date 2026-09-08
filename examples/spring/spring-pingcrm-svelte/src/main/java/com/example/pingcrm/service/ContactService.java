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
import jakarta.persistence.criteria.Subquery;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.pingcrm.entity.Contact;
import com.example.pingcrm.entity.Organization;
import com.example.pingcrm.repository.ContactRepository;
import com.example.pingcrm.repository.OrganizationRepository;

@Service
public class ContactService {

    private final ContactRepository contactRepository;
    private final OrganizationRepository organizationRepository;

    @PersistenceContext
    private EntityManager em;

    public ContactService(ContactRepository contactRepository, OrganizationRepository organizationRepository) {
        this.contactRepository = contactRepository;
        this.organizationRepository = organizationRepository;
    }

    public Pagination.Result page(Long accountId, String search, String trashed, int page, int size) {
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

        var orgNames = orgNames(accountId, items);
        var data = items.stream().map(contact -> toListItem(contact, orgNames)).toList();
        var filters = new LinkedHashMap<String, Object>();
        filters.put("search", search);
        filters.put("trashed", trashed);
        return Pagination.of("/contacts", data, total, page, size, filters);
    }

    @Transactional
    public Contact create(Long accountId, Map<String, String> values) {
        var contact = new Contact();
        contact.accountId = accountId;
        apply(contact, values);
        contact.createdAt = Instant.now();
        contact.updatedAt = contact.createdAt;
        em.persist(contact);
        return contact;
    }

    @Transactional
    public void update(Contact contact, Map<String, String> values) {
        apply(contact, values);
        contact.updatedAt = Instant.now();
        em.merge(contact);
    }

    @Transactional
    public void softDelete(Contact contact) {
        contact.deletedAt = Instant.now();
        em.merge(contact);
    }

    @Transactional
    public void restore(Contact contact) {
        contact.deletedAt = null;
        em.merge(contact);
    }

    public boolean organizationBelongsToAccount(Long accountId, Long organizationId) {
        if (organizationId == null) return true;
        var organization = organizationRepository.findById(organizationId).orElse(null);
        return organization != null && organization.accountId.equals(accountId);
    }

    private void apply(Contact contact, Map<String, String> values) {
        contact.firstName = values.get("first_name");
        contact.lastName = values.get("last_name");
        contact.organizationId = parseId(values.get("organization_id"));
        contact.email = values.get("email");
        contact.phone = values.get("phone");
        contact.address = values.get("address");
        contact.city = values.get("city");
        contact.region = values.get("region");
        contact.country = values.get("country");
        contact.postalCode = values.get("postal_code");
    }

    private Long parseId(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Map<String, String> orgNames(Long accountId, List<Contact> contacts) {
        var ids = contacts.stream()
            .map(c -> c.organizationId)
            .filter(id -> id != null)
            .distinct()
            .toList();
        if (ids.isEmpty()) return Map.of();
        var orgs = em.createQuery(
                "select o from Organization o where o.accountId = :accountId and o.id in :ids",
                Organization.class)
            .setParameter("accountId", accountId)
            .setParameter("ids", ids)
            .getResultList();
        var names = new java.util.HashMap<String, String>();
        for (var org : orgs) {
            names.put(String.valueOf(org.id), org.name);
        }
        return names;
    }

    private Map<String, Object> toListItem(Contact contact, Map<String, String> orgNames) {
        var item = new LinkedHashMap<String, Object>();
        item.put("id", contact.id);
        item.put("name", contact.getName());
        item.put("phone", contact.phone);
        item.put("city", contact.city);
        item.put("deleted_at", contact.deletedAt);
        var orgName = contact.organizationId != null
            ? orgNames.get(String.valueOf(contact.organizationId)) : null;
        item.put("organization", orgName != null ? Map.of("name", orgName) : null);
        return item;
    }

    private void applyFilters(CriteriaBuilder cb, Root<?> root,
            List<Predicate> predicates, String search, String trashed) {
        if (search != null && !search.isBlank()) {
            var pattern = "%" + search.toLowerCase() + "%";
            Subquery<Long> orgSub = cb.createQuery().subquery(Long.class);
            var orgRoot = orgSub.from(Organization.class);
            orgSub.select(orgRoot.get("id"));
            orgSub.where(cb.and(
                cb.equal(orgRoot.get("accountId"), root.get("accountId")),
                cb.like(cb.lower(orgRoot.get("name")), pattern)));
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.exists(orgSub)));
        }
        if (trashed == null || trashed.isBlank()) {
            predicates.add(cb.isNull(root.get("deletedAt")));
        } else if ("only".equals(trashed)) {
            predicates.add(cb.isNotNull(root.get("deletedAt")));
        }
    }
}
