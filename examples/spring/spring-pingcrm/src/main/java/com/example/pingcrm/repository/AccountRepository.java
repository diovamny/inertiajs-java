package com.example.pingcrm.repository;

import com.example.pingcrm.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Account findByName(String name);
}
