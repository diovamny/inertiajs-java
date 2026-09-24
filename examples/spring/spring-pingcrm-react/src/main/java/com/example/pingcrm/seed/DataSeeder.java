package com.example.pingcrm.seed;

import java.time.Instant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.pingcrm.entity.Account;
import com.example.pingcrm.entity.Contact;
import com.example.pingcrm.entity.Organization;
import com.example.pingcrm.entity.User;

/**
 * Deterministic demo seed (no faker): one account, the johndoe demo user
 * (password {@code secret}), two organizations and three contacts. Runs once
 * when the account table is empty; powers the E2E login contracts.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private final TransactionTemplate tx;
    private final PasswordEncoder passwordEncoder;

    @PersistenceContext
    private EntityManager em;

    @Value("${pingcrm.seed.enabled:true}")
    private boolean enabled;

    public DataSeeder(PlatformTransactionManager transactionManager, PasswordEncoder passwordEncoder) {
        this.tx = new TransactionTemplate(transactionManager);
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) return;

        var accountCount = em.createQuery("SELECT COUNT(a) FROM Account a", Long.class).getSingleResult();
        if (accountCount > 0) return;

        System.out.println("Seeding PingCRM React demo data...");
        tx.executeWithoutResult(status -> {
            var account = new Account();
            account.name = "Acme Corporation";
            account.createdAt = Instant.now();
            account.updatedAt = account.createdAt;
            em.persist(account);
            em.flush();

            var user = new User();
            user.accountId = account.id;
            user.firstName = "John";
            user.lastName = "Doe";
            user.email = "johndoe@example.com";
            user.password = passwordEncoder.encode("secret");
            user.owner = true;
            user.createdAt = Instant.now();
            user.updatedAt = user.createdAt;
            em.persist(user);

            var orgNames = new String[] {"Acme Corporation", "Globex"};
            for (var orgName : orgNames) {
                var organization = new Organization();
                organization.accountId = account.id;
                organization.name = orgName;
                organization.email = "info@example.com";
                organization.createdAt = Instant.now();
                organization.updatedAt = organization.createdAt;
                em.persist(organization);
                em.flush();

                var contact = new Contact();
                contact.accountId = account.id;
                contact.organizationId = organization.id;
                contact.firstName = "Jane";
                contact.lastName = "Doe";
                contact.email = "jane@example.com";
                contact.createdAt = Instant.now();
                contact.updatedAt = contact.createdAt;
                em.persist(contact);
            }
            em.flush();
        });
        System.out.println("PingCRM React demo data seeded.");
    }
}
