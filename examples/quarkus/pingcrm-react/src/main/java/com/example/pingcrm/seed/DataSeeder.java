package com.example.pingcrm.seed;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.UserTransaction;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.github.javafaker.Faker;

import com.example.pingcrm.entity.Account;
import com.example.pingcrm.entity.Contact;
import com.example.pingcrm.entity.Organization;
import com.example.pingcrm.entity.User;
import com.example.pingcrm.service.AuthService;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class DataSeeder {

    @Inject
    @ConfigProperty(name = "pingcrm.seed.enabled", defaultValue = "true")
    boolean enabled;

    @Inject
    @ConfigProperty(name = "pingcrm.seed.organizations", defaultValue = "100")
    int organizationCount;

    @Inject
    @ConfigProperty(name = "pingcrm.seed.contacts", defaultValue = "100")
    int contactCount;

    @Inject
    jakarta.persistence.EntityManager em;

    @Inject
    UserTransaction utx;

    @Inject
    org.flywaydb.core.Flyway flyway;

    void onStart(@Observes StartupEvent ev) {
        if (!enabled) return;

        // Flyway is also registered as a StartupEvent observer without an
        // ordering guarantee, so migrate explicitly (idempotent).
        flyway.migrate();

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
        try {
            utx.begin();
            var account = new Account();
            account.name = "Acme Corporation";
            account.createdAt = Instant.now();
            account.updatedAt = account.createdAt;
            account.persist();
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
            user.persist();
            em.flush();
            utx.commit();
            return account;
        } catch (Exception e) {
            try { utx.rollback(); } catch (Exception ex) {}
            throw new RuntimeException("Failed seeding account", e);
        }
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
            organization.persist();
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
            contact.persist();
        });
    }

    @FunctionalInterface
    interface BatchConsumer {
        void accept(int index);
    }

    private void seedBatch(String label, int total, BatchConsumer consumer) {
        final int batchSize = 500;
        for (int batch = 0; batch < total; batch += batchSize) {
            int end = Math.min(batch + batchSize, total);
            try {
                utx.begin();
                for (int i = batch; i < end; i++) {
                    consumer.accept(i);
                }
                em.flush();
                em.clear();
                utx.commit();
            } catch (Exception e) {
                try { utx.rollback(); } catch (Exception ex) {}
                throw new RuntimeException("Failed seeding " + label + " batch " + batch, e);
            }
        }
        System.out.println("Seeded " + total + " " + label);
    }
}
