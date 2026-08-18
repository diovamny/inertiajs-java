package com.example.demo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.criteria.Predicate;
import jakarta.transaction.Transactional;

import com.example.demo.entity.Employee;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class EmployeeService implements PanacheRepository<Employee> {

    private static final List<String> ALLOWED_SORT_FIELDS = List.of(
        "firstName", "lastName", "email", "salary", "active", "department", "hireDate"
    );

    public record LazyResult(List<Employee> data, long total, int page, int size) {}

    @SuppressWarnings("unchecked")
    public LazyResult findLazy(Map<String, Object> filters,
                                String sortField, int sortOrder,
                                int page, int size) {
        var cb = getEntityManager().getCriteriaBuilder();
        var cq = cb.createQuery(Employee.class);
        var root = cq.from(Employee.class);

        var predicates = new ArrayList<Predicate>();

        if (filters != null) {
            for (var entry : filters.entrySet()) {
                var field = entry.getKey();
                var filterVal = entry.getValue();
                if (filterVal instanceof Map) {
                    var constraint = (Map<String, Object>) filterVal;
                    addConstraint(predicates, cb, root, field, constraint);
                }
            }
        }

        cq.where(predicates.toArray(new Predicate[0]));

        var sortFieldResolved = sortField != null && ALLOWED_SORT_FIELDS.contains(sortField) ? sortField : "firstName";
        var order = sortOrder <= 0 ? cb.desc(root.get(sortFieldResolved)) : cb.asc(root.get(sortFieldResolved));
        cq.orderBy(order);

        var query = getEntityManager().createQuery(cq);
        query.setFirstResult(page * size);
        query.setMaxResults(size);

        // Count
        var countCb = getEntityManager().getCriteriaBuilder();
        var countCq = countCb.createQuery(Long.class);
        var countRoot = countCq.from(Employee.class);
        var countPredicates = new ArrayList<Predicate>();

        if (filters != null) {
            for (var entry : filters.entrySet()) {
                var field = entry.getKey();
                var filterVal = entry.getValue();
                if (filterVal instanceof Map) {
                    var constraint = (Map<String, Object>) filterVal;
                    addConstraint(countPredicates, countCb, countRoot, field, constraint);
                }
            }
        }

        countCq.where(countPredicates.toArray(new Predicate[0]));
        countCq.select(countCb.count(countRoot));

        var total = getEntityManager().createQuery(countCq).getSingleResult();
        var items = query.getResultList();

        return new LazyResult(items, total, page, size);
    }

    @SuppressWarnings("unchecked")
    private void addConstraint(List<Predicate> predicates, jakarta.persistence.criteria.CriteriaBuilder cb,
                                jakarta.persistence.criteria.Path<?> root, String field,
                                Map<String, Object> constraint) {
        var path = getPath(root, field);
        if (path == null) return;

        var matchMode = (String) constraint.get("matchMode");
        var value = constraint.get("value");
        var fieldType = path.getJavaType();

        if (value == null) return;

        if ("date".equals(matchMode) && value instanceof String) {
            value = java.time.LocalDate.parse((String) value);
        }

        if ("between".equals(matchMode) && value instanceof List) {
            var range = (List<Object>) value;
            if (range.size() == 2) {
                var lo = convertNumber(range.get(0), fieldType);
                var hi = convertNumber(range.get(1), fieldType);
                if (lo != null && hi != null) {
                    predicates.add(cb.between((jakarta.persistence.criteria.Path<Comparable>) path, (Comparable) lo, (Comparable) hi));
                }
            }
            return;
        }

        switch (matchMode) {
            case "contains" -> predicates.add(cb.like(cb.lower(path.as(String.class)),
                "%" + value.toString().toLowerCase() + "%"));
            case "equals" -> predicates.add(cb.equal(path, convertValue(value, fieldType)));
            case "startsWith" -> predicates.add(cb.like(cb.lower(path.as(String.class)),
                value.toString().toLowerCase() + "%"));
            case "endsWith" -> predicates.add(cb.like(cb.lower(path.as(String.class)),
                "%" + value.toString().toLowerCase()));
            case "gt" -> predicates.add(cb.greaterThan(path.as(Comparable.class), (Comparable) convertNumber(value, fieldType)));
            case "lt" -> predicates.add(cb.lessThan(path.as(Comparable.class), (Comparable) convertNumber(value, fieldType)));
            case "gte" -> predicates.add(cb.greaterThanOrEqualTo(path.as(Comparable.class), (Comparable) convertNumber(value, fieldType)));
            case "lte" -> predicates.add(cb.lessThanOrEqualTo(path.as(Comparable.class), (Comparable) convertNumber(value, fieldType)));
        }
    }

    private jakarta.persistence.criteria.Path<?> getPath(jakarta.persistence.criteria.Path<?> root, String field) {
        try {
            return root.get(field);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Object convertNumber(Object value, Class<?> targetType) {
        if (value instanceof Number n) {
            if (targetType == BigDecimal.class) return BigDecimal.valueOf(n.doubleValue());
            if (targetType == Double.class || targetType == double.class) return n.doubleValue();
            if (targetType == Integer.class || targetType == int.class) return n.intValue();
            if (targetType == Long.class || targetType == long.class) return n.longValue();
        }
        return value;
    }

    private Object convertValue(Object value, Class<?> targetType) {
        if (targetType == Boolean.class || targetType == boolean.class) {
            if (value instanceof Boolean) return value;
            return Boolean.valueOf(value.toString());
        }
        return value;
    }

    @Transactional
    public Employee create(Employee emp) {
        persist(emp);
        return emp;
    }

    @Transactional
    public Employee update(Long id, Employee updated) {
        var emp = findById(id);
        if (emp == null) throw new IllegalArgumentException("Employee not found: " + id);
        emp.firstName = updated.firstName;
        emp.lastName = updated.lastName;
        emp.email = updated.email;
        emp.salary = updated.salary;
        emp.active = updated.active;
        emp.department = updated.department;
        emp.hireDate = updated.hireDate;
        return emp;
    }

    @Transactional
    public void delete(Long id) {
        deleteById(id);
    }
}
