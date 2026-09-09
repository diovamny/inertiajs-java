package com.example.pingcrm.repository;

import com.example.pingcrm.entity.Contact;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    
    @Query("SELECT c FROM Contact c WHERE c.accountId = ?1 AND (lower(c.firstName) LIKE %?2% OR lower(c.lastName) LIKE %?2% OR lower(c.email) LIKE %?2%) ORDER BY c.id DESC")
    Page<Contact> findByAccountIdAndSearch(Long accountId, String search, Pageable pageable);
    
    @Query("SELECT c FROM Contact c WHERE c.accountId = ?1 ORDER BY c.id DESC")
    Page<Contact> findByAccountId(Long accountId, Pageable pageable);
    
    List<Contact> findByAccountIdAndDeletedAtIsNull(Long accountId);
    
    Optional<Contact> findByAccountIdAndId(Long accountId, Long id);
    
    long countByAccountIdAndDeletedAtIsNull(Long accountId);
    
    long countByAccountId(Long accountId);
    
    boolean existsByAccountIdAndEmail(Long accountId, String email);
    
    List<Contact> findByAccountIdAndOrganizationIdAndDeletedAtIsNullOrderByLastNameAscFirstNameAsc(
            Long accountId, Long organizationId);
}
