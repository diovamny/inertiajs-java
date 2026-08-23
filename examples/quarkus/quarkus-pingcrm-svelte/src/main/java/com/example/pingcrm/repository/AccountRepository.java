package com.example.pingcrm.repository;

import com.example.pingcrm.entity.Account;
import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.smallrye.mutiny.Uni;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public interface AccountRepository extends PanacheRepository<Account> {
    
    Uni<Account> findByName(String name);
}