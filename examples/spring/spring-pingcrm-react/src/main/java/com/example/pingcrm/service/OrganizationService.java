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

    public void update(Organization organization) {
        organizations.save(organization);
    }

    public void softDelete(Organization organization) {
        organization.deletedAt = java.time.LocalDateTime.now();
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