package com.example.pingcrm.service;

import com.example.pingcrm.entity.Organization;
import com.example.pingcrm.repository.OrganizationRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OrganizationService {

    private final OrganizationRepository organizations;

    public OrganizationService(OrganizationRepository organizations) {
        this.organizations = organizations;
    }

    public Page<Organization> page(Long accountId, String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return organizations.findByAccountIdAndSearch(accountId, search, pageable);
        }
        return organizations.findByAccountId(accountId, pageable);
    }

    public Organization findById(Long id) {
        return organizations.findById(id).orElse(null);
    }

    public void create(Organization organization) {
        organizations.save(organization);
    }

    public Organization create(Long accountId, Map<String, Object> values) {
        var organization = new Organization();
        organization.accountId = accountId;
        apply(organization, values);
        organization.createdAt = java.time.Instant.now();
        organization.updatedAt = organization.createdAt;
        organizations.save(organization);
        return organization;
    }

    public void update(Organization organization) {
        organizations.save(organization);
    }

    public void update(Organization organization, Map<String, Object> values) {
        apply(organization, values);
        organization.updatedAt = java.time.Instant.now();
        organizations.save(organization);
    }

    private void apply(Organization organization, Map<String, Object> values) {
        organization.name = (String) values.get("name");
        organization.email = (String) values.get("email");
        organization.phone = (String) values.get("phone");
        organization.address = (String) values.get("address");
        organization.city = (String) values.get("city");
        organization.region = (String) values.get("region");
        organization.country = (String) values.get("country");
        organization.postalCode = (String) values.get("postal_code");
    }

    public void softDelete(Organization organization) {
        organization.deletedAt = java.time.Instant.now();
        organizations.save(organization);
    }

    public void restore(Organization organization) {
        organization.deletedAt = null;
        organizations.save(organization);
    }

    public List<Map<String, Object>> options(Long accountId) {
        return organizations.findOptionsByAccountId(accountId);
    }
}
