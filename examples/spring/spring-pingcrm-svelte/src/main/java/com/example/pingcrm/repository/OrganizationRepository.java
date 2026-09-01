package com.example.pingcrm.repository;

import com.example.pingcrm.entity.Organization;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    @Query("SELECT o FROM Organization o WHERE o.accountId = ?1 AND (lower(o.name) LIKE %?2% OR lower(o.email) LIKE %?2% OR lower(o.city) LIKE %?2%) ORDER BY o.id DESC")
    Page<Organization> findByAccountIdAndSearch(Long accountId, String search, Pageable pageable);

    @Query("SELECT o FROM Organization o WHERE o.accountId = ?1 ORDER BY o.id DESC")
    Page<Organization> findByAccountId(Long accountId, Pageable pageable);

    List<Organization> findByAccountIdAndDeletedAtIsNull(Long accountId);

    Optional<Organization> findByAccountIdAndId(Long accountId, Long id);

    long countByAccountIdAndDeletedAtIsNull(Long accountId);

    long countByAccountId(Long accountId);

    @Query("SELECT new Map(o.id as id, o.name as name) FROM Organization o WHERE o.accountId = ?1 AND o.deletedAt IS NULL ORDER BY o.name")
    List<Map<String, Object>> findOptionsByAccountId(Long accountId);

    List<Organization> findByAccountIdOrderByName(Long accountId);
}
