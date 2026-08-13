package com.example.kitchensink.repository;

import com.example.kitchensink.entity.Organization;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrganizationRepository implements PanacheRepository<Organization> {

    public long countContacts(Long organizationId) {
        return count("organizationId", organizationId);
    }
}