package com.example.kitchensink.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kitchensink.entity.Contact;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    long countByIsFavorite(boolean isFavorite);

    long countByOrganizationId(Long organizationId);

    List<Contact> findAllByOrderByIdDesc();
}
