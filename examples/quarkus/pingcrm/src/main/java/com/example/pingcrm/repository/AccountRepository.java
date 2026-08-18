package com.example.pingcrm.repository;

import com.example.pingcrm.entity.Account;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AccountRepository implements PanacheRepository<Account> {

    public Account findByName(String name) {
        return find("name", name).firstResult();
    }
}
