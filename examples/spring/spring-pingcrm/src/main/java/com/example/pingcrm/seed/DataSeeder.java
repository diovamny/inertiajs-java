package com.example.pingcrm.seed;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.pingcrm.entity.Account;
import com.example.pingcrm.entity.Contact;
import com.example.pingcrm.entity.Organization;
import com.example.pingcrm.entity.User;
import com.example.pingcrm.service.AuthService;

@Component
public class DataSeeder implements ApplicationRunner {

    private final TransactionTemplate tx;

    @PersistenceContext
    private EntityManager em;

    @Value("${pingcrm.seed.enabled:true}")
    private boolean enabled;

    @Value("${pingcrm.seed.organizations:100}")
    private int organizationCount;

    @Value("${pingcrm.seed.contacts:100}")
    private int contactCount;

    public DataSeeder(PlatformTransactionManager transactionManager) {
        this.tx = new TransactionTemplate(transactionManager);
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) return;

        var accountCount = em.createQuery("SELECT COUNT(a) FROM Account a", Long.class).getSingleResult();
        if (accountCount > 0) return;

        System.out.println("Seeding PingCRM demo data...");
        var faker = new Faker(new Random(42));

        var account = createAccount(faker);
        var organizationIds = seedOrganizations(faker, account);
        seedContacts(faker, account, organizationIds);
        System.out.println("PingCRM demo data seeded.");
    }

    private Account createAccount(Faker faker) {
        return tx.execute(status -> {
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
            user.password = AuthService.hash("secret");
            user.owner = true;
            user.createdAt = Instant.now();
            user.updatedAt = user.createdAt;
            em.persist(user);
            em.flush();
            return account;
        });
    }

    private List<Long> seedOrganizations(Faker faker, Account account) {
        var ids = new ArrayList<Long>();
        seedBatch("organizations", organizationCount, (i) -> {
            var organization = new Organization();
            organization.accountId = account.id;
            organization.name = faker.company().name();
            organization.email = faker.internet().emailAddress();
            organization.phone = faker.phoneNumber().phoneNumber();
            organization.address = faker.address().streetAddress();
            organization.city = faker.address().city();
            organization.region = faker.address().state();
            organization.country = faker.address().countryCode();
            organization.postalCode = faker.address().zipCode();
            organization.createdAt = faker.date().past(365, java.util.concurrent.TimeUnit.DAYS).toInstant();
            organization.updatedAt = organization.createdAt;
            em.persist(organization);
            ids.add(organization.id);
        });
        return ids;
    }

    private void seedContacts(Faker faker, Account account, List<Long> organizationIds) {
        seedBatch("contacts", contactCount, (i) -> {
            var contact = new Contact();
            contact.accountId = account.id;
            contact.firstName = faker.name().firstName();
            contact.lastName = faker.name().lastName();
            contact.organizationId = organizationIds.get(faker.random().nextInt(organizationIds.size()));
            contact.email = faker.internet().emailAddress();
            contact.phone = faker.phoneNumber().phoneNumber();
            contact.address = faker.address().streetAddress();
            contact.city = faker.address().city();
            contact.region = faker.address().state();
            contact.country = faker.address().countryCode();
            contact.postalCode = faker.address().zipCode();
            contact.createdAt = faker.date().past(365, java.util.concurrent.TimeUnit.DAYS).toInstant();
            contact.updatedAt = contact.createdAt;
            em.persist(contact);
        });
    }

    @FunctionalInterface
    interface BatchConsumer {
        void accept(int index);
    }

    private void seedBatch(String label, int total, BatchConsumer consumer) {
        final int batchSize = 500;
        for (int batch = 0; batch < total; batch += batchSize) {
            final int start = batch;
            int end = Math.min(batch + batchSize, total);
            tx.executeWithoutResult(status -> {
                IntStream.range(start, end).forEach(consumer::accept);
                em.flush();
                em.clear();
            });
        }
        System.out.println("Seeded " + total + " " + label);
    }
}