package com.example.pingcrm.repository;

import java.util.List;

import com.example.pingcrm.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findByAccountIdAndOrganizationIdAndDeletedAtIsNullOrderByLastNameAscFirstNameAsc(
            Long accountId, Long organizationId);
}