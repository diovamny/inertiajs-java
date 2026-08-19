package com.example.kitchensink.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kitchensink.entity.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    List<Organization> findAllByOrderByNameAsc();
}