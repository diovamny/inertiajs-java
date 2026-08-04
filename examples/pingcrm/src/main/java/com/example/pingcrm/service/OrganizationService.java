package com.example.pingcrm.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import com.example.pingcrm.entity.Contact;
import com.example.pingcrm.entity.Organization;
import com.example.pingcrm.repository.ContactRepository;
import com.example.pingcrm.repository.OrganizationRepository;

@ApplicationScoped
public class OrganizationService {

    @Inject
    OrganizationRepository organizationRepository;

    @Inject
    ContactRepository contactRepository;

    public Pagination.Result page(Long accountId, String search, String trashed, int page, int size) {
        var em = organizationRepository.getEntityManager();
        var cb = em.getCriteriaBuilder();

        var cq = cb.createQuery(Organization.class);
        var root = cq.from(Organization.class);
        var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
        predicates.add(cb.equal(root.get("accountId"), accountId));
        applyFilters(cb, root, predicates, search, trashed);
        cq.where(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        cq.orderBy(cb.asc(root.get("name")));

        var query = em.createQuery(cq);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);
        var items = query.getResultList();

        var countCq = cb.createQuery(Long.class);
        var countRoot = countCq.from(Organization.class);
        var countPredicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
        countPredicates.add(cb.equal(countRoot.get("accountId"), accountId));
        applyFilters(cb, countRoot, countPredicates, search, trashed);
        countCq.where(countPredicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        countCq.select(cb.count(countRoot));
        var total = em.createQuery(countCq).getSingleResult();

        var filters = new LinkedHashMap<String, Object>();
        filters.put("search", search);
        filters.put("trashed", trashed);
        return Pagination.of("/organizations", items.stream().map(this::toListItem).toList(),
            total, page, size, filters);
    }

    public List<Map<String, Object>> options(Long accountId) {
        return organizationRepository.findByAccountOrderedByName(accountId).stream()
            .map(org -> Map.<String, Object>of("id", org.id, "name", org.name))
            .toList();
    }

    public Map<String, Object> editData(Organization organization) {
        var contacts = contactRepository.findByOrganizationOrderedByName(
                organization.accountId, organization.id).stream()
            .map(contact -> {
                var item = new LinkedHashMap<String, Object>();
                item.put("id", contact.id);
                item.put("name", contact.getName());
                item.put("city", contact.city);
                item.put("phone", contact.phone);
                return item;
            })
            .toList();

        var map = new LinkedHashMap<String, Object>();
        map.put("id", organization.id);
        map.put("name", organization.name);
        map.put("email", organization.email);
        map.put("phone", organization.phone);
        map.put("address", organization.address);
        map.put("city", organization.city);
        map.put("region", organization.region);
        map.put("country", organization.country);
        map.put("postal_code", organization.postalCode);
        map.put("deleted_at", organization.deletedAt);
        map.put("contacts", contacts);
        return map;
    }

    @Transactional
    public Organization create(Long accountId, Map<String, String> values) {
        var organization = new Organization();
        organization.accountId = accountId;
        apply(organization, values);
        organization.createdAt = Instant.now();
        organization.updatedAt = organization.createdAt;
        organization.persist();
        return organization;
    }

    @Transactional
    public void update(Organization organization, Map<String, String> values) {
        apply(organization, values);
        organization.updatedAt = Instant.now();
        organizationRepository.getEntityManager().merge(organization);
    }

    public void softDelete(Organization organization) {
        organizationRepository.softDelete(organization);
    }

    public void restore(Organization organization) {
        organizationRepository.restore(organization);
    }

    private void apply(Organization organization, Map<String, String> values) {
        organization.name = values.get("name");
        organization.email = values.get("email");
        organization.phone = values.get("phone");
        organization.address = values.get("address");
        organization.city = values.get("city");
        organization.region = values.get("region");
        organization.country = values.get("country");
        organization.postalCode = values.get("postal_code");
    }

    private Map<String, Object> toListItem(Organization organization) {
        var item = new LinkedHashMap<String, Object>();
        item.put("id", organization.id);
        item.put("name", organization.name);
        item.put("phone", organization.phone);
        item.put("city", organization.city);
        item.put("deleted_at", organization.deletedAt);
        return item;
    }

    private void applyFilters(jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Root<?> root,
            List<jakarta.persistence.criteria.Predicate> predicates,
            String search, String trashed) {
        if (search != null && !search.isBlank()) {
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
        }
        if (trashed == null || trashed.isBlank()) {
            predicates.add(cb.isNull(root.get("deletedAt")));
        } else if ("only".equals(trashed)) {
            predicates.add(cb.isNotNull(root.get("deletedAt")));
        }
    }
}
