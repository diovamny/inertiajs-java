package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import com.example.demo.entity.Person;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class PersonService implements PanacheRepository<Person> {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of("name", "lastName", "email", "phone", "active", "createdAt");

    public record PageResult(List<Person> items, long total, int page, int size) {}

    public long countAll() {
        return count();
    }

    public PageResult findPaginated(String search, Boolean active,
                                     String sortField, String sortOrder,
                                     int page, int size) {
        var cb = getEntityManager().getCriteriaBuilder();
        var cq = cb.createQuery(Person.class);
        var root = cq.from(Person.class);

        var predicates = new ArrayList<Predicate>();

        if (search != null && !search.isBlank()) {
            var pattern = "%" + search.toLowerCase() + "%";
            predicates.add(cb.or(
                cb.like(cb.lower(root.get("name")), pattern),
                cb.like(cb.lower(root.get("lastName")), pattern),
                cb.like(cb.lower(root.get("email")), pattern),
                cb.like(cb.lower(root.get("phone")), pattern)
            ));
        }

        if (active != null) {
            predicates.add(cb.equal(root.get("active"), active));
        }

        cq.where(predicates.toArray(new Predicate[0]));

        var sortFieldResolved = sortField != null && ALLOWED_SORT_FIELDS.contains(sortField) ? sortField : "createdAt";
        var order = "desc".equalsIgnoreCase(sortOrder) ? cb.desc(root.get(sortFieldResolved)) : cb.asc(root.get(sortFieldResolved));
        cq.orderBy(order);

        var query = getEntityManager().createQuery(cq);
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        // Count
        var countCb = getEntityManager().getCriteriaBuilder();
        var countCq = countCb.createQuery(Long.class);
        var countRoot = countCq.from(Person.class);
        var countPredicates = new ArrayList<Predicate>();

        if (search != null && !search.isBlank()) {
            var pattern = "%" + search.toLowerCase() + "%";
            countPredicates.add(countCb.or(
                countCb.like(countCb.lower(countRoot.get("name")), pattern),
                countCb.like(countCb.lower(countRoot.get("lastName")), pattern),
                countCb.like(countCb.lower(countRoot.get("email")), pattern),
                countCb.like(countCb.lower(countRoot.get("phone")), pattern)
            ));
        }

        if (active != null) {
            countPredicates.add(countCb.equal(countRoot.get("active"), active));
        }

        countCq.where(countPredicates.toArray(new Predicate[0]));
        countCq.select(countCb.count(countRoot));

        var total = getEntityManager().createQuery(countCq).getSingleResult();
        var items = query.getResultList();

        return new PageResult(items, total, page, size);
    }

    @Transactional
    public Person create(Person person) {
        persist(person);
        return person;
    }

    @Transactional
    public Person update(Long id, Person updated) {
        var person = findById(id);
        if (person == null) throw new IllegalArgumentException("Person not found: " + id);
        person.name = updated.name;
        person.lastName = updated.lastName;
        person.email = updated.email;
        person.phone = updated.phone;
        person.active = updated.active;
        return person;
    }

    @Transactional
    public void delete(Long id) {
        deleteById(id);
    }
}
