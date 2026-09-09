package com.example.pingcrm.repository;

import java.util.List;

import com.example.pingcrm.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findByAccountIdOrderByName(Long accountId);
}
