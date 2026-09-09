package com.example.pingcrm.repository;

import com.example.pingcrm.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.accountId = ?1 AND (lower(u.firstName) LIKE %?2% OR lower(u.lastName) LIKE %?2% OR lower(u.email) LIKE %?2%) ORDER BY u.id DESC")
    Page<User> findByAccountIdAndSearch(Long accountId, String search, Pageable pageable);
    
    @Query("SELECT u FROM User u WHERE u.accountId = ?1 ORDER BY u.id DESC")
    Page<User> findByAccountId(Long accountId, Pageable pageable);
    
    Optional<User> findByAccountIdAndEmail(Long accountId, String email);
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByAccountIdAndId(Long accountId, Long id);
    
    long countByAccountId(Long accountId);
    
    boolean existsByAccountIdAndEmail(Long accountId, String email);
}
