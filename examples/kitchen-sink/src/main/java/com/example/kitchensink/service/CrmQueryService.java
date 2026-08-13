package com.example.kitchensink.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.criteria.Predicate;

import com.example.kitchensink.entity.Contact;
import com.example.kitchensink.entity.Organization;
import com.example.kitchensink.repository.ContactRepository;
import com.example.kitchensink.repository.OrganizationRepository;

@ApplicationScoped
public class CrmQueryService {

    @Inject
    ContactRepository contactRepository;

    @Inject
    OrganizationRepository organizationRepository;

    /** Contacts with optional search and favorite filters, ordered like the demo. */
    public List<Contact> contacts(String search, boolean favorite, long offset, long limit) {
        var em = contactRepository.getEntityManager();
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Contact.class);
        var root = cq.from(Contact.class);
        var predicates = new ArrayList<Predicate>();
        applyContactFilters(cb, root, predicates, search, favorite);
        cq.where(predicates.toArray(new Predicate[0]));
        cq.orderBy(cb.asc(root.get("firstName")), cb.asc(root.get("lastName")), cb.asc(root.get("id")));
        var query = em.createQuery(cq);
        query.setFirstResult((int) offset);
        query.setMaxResults((int) limit);
        return query.getResultList();
    }

    public long contactsCount(String search, boolean favorite) {
        var em = contactRepository.getEntityManager();
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Long.class);
        var root = cq.from(Contact.class);
        var predicates = new ArrayList<Predicate>();
        applyContactFilters(cb, root, predicates, search, favorite);
        cq.where(predicates.toArray(new Predicate[0]));
        cq.select(cb.count(root));
        return em.createQuery(cq).getSingleResult();
    }

    /** Organizations with optional search, ordered by name. */
    public List<Organization> organizations(String search, long offset, long limit) {
        var em = organizationRepository.getEntityManager();
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Organization.class);
        var root = cq.from(Organization.class);
        if (search != null && !search.isBlank()) {
            cq.where(cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
        }
        cq.orderBy(cb.asc(root.get("name")));
        var query = em.createQuery(cq);
        query.setFirstResult((int) offset);
        query.setMaxResults((int) limit);
        return query.getResultList();
    }

    public long organizationsCount(String search) {
        var em = organizationRepository.getEntityManager();
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Long.class);
        var root = cq.from(Organization.class);
        if (search != null && !search.isBlank()) {
            cq.where(cb.like(cb.lower(root.get("name")), "%" + search.toLowerCase() + "%"));
        }
        cq.select(cb.count(root));
        return em.createQuery(cq).getSingleResult();
    }

    /** Contacts of one organization, ordered like the demo. */
    public List<Contact> organizationContacts(Long organizationId, long offset, long limit) {
        var em = contactRepository.getEntityManager();
        var cb = em.getCriteriaBuilder();
        var cq = cb.createQuery(Contact.class);
        var root = cq.from(Contact.class);
        cq.where(cb.equal(root.get("organizationId"), organizationId));
        cq.orderBy(cb.asc(root.get("firstName")), cb.asc(root.get("lastName")), cb.asc(root.get("id")));
        var query = em.createQuery(cq);
        query.setFirstResult((int) offset);
        query.setMaxResults((int) limit);
        return query.getResultList();
    }

    public long organizationContactsCount(Long organizationId) {
        return contactRepository.count("organizationId", organizationId);
    }

    /** Organization name lookups for contact resources. */
    public Map<Long, String> organizationNames() {
        var orgs = organizationRepository.listAll();
        var names = new LinkedHashMap<Long, String>();
        for (var org : orgs) {
            names.put(org.id, org.name);
        }
        return names;
    }

    public Map<String, Object> organizationOptions() {
        var orgs = organizationRepository.find("order by name").list();
        var options = new ArrayList<Object>();
        for (var org : orgs) {
            var item = new LinkedHashMap<String, Object>();
            item.put("id", org.id);
            item.put("name", org.name);
            options.add(item);
        }
        return Map.of("organizations", options);
    }

    private void applyContactFilters(jakarta.persistence.criteria.CriteriaBuilder cb,
            jakarta.persistence.criteria.Root<?> root, List<Predicate> predicates,
            String search, boolean favorite) {
        if (search != null && !search.isBlank()) {
            var pattern = "%" + search.toLowerCase() + "%";
            var like = cb.or(
                cb.like(cb.lower(root.get("firstName")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern));
            var orgSub = cb.createQuery().subquery(Long.class);
            var orgRoot = orgSub.from(Organization.class);
            orgSub.select(orgRoot.get("id"));
            orgSub.where(cb.like(cb.lower(orgRoot.get("name")), pattern));
            predicates.add(cb.or(like, cb.exists(orgSub)));
        }
        if (favorite) {
            predicates.add(cb.isTrue(root.get("isFavorite")));
        }
    }
}