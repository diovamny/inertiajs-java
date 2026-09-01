package com.example.pingcrm.seed;

import java.time.Instant;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import com.example.pingcrm.entity.Account;
import com.example.pingcrm.entity.Contact;
import com.example.pingcrm.entity.Organization;
import com.example.pingcrm.entity.User;
import com.example.pingcrm.service.AuthService;

import io.quarkus.runtime.StartupEvent;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import com.github.javafaker.Faker;

import java.util.Locale;

@ApplicationScoped
public class DataSeeder {

    @ConfigProperty(name = "pingcrm.seed.enabled", defaultValue = "false")
    boolean seedEnabled;

    @jakarta.inject.Inject
    EntityManager em;

    @Transactional
    void seed(@Observes StartupEvent ev) {
        if (!seedEnabled) return;

        var count = em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
        if (count > 0) return;

        var account = new Account();
        account.name = "Acme Corporation";
        account.createdAt = Instant.now();
        account.updatedAt = Instant.now();
        account.persist();

        var demoUser = new User();
        demoUser.accountId = account.id;
        demoUser.firstName = "John";
        demoUser.lastName = "Doe";
        demoUser.email = "johndoe@example.com";
        demoUser.password = AuthService.hash("secret");
        demoUser.owner = true;
        demoUser.createdAt = Instant.now();
        demoUser.updatedAt = Instant.now();
        demoUser.persist();

        var faker = new Faker(new Locale("en-US"));

        var orgs = new java.util.ArrayList<Organization>();
        for (int i = 0; i < 100; i++) {
            var org = new Organization();
            org.accountId = account.id;
            org.name = faker.company().name();
            org.email = faker.internet().emailAddress();
            org.phone = faker.phoneNumber().phoneNumber();
            org.address = faker.address().streetAddress();
            org.city = faker.address().city();
            org.region = faker.address().state();
            org.country = "US";
            org.postalCode = faker.address().zipCode();
            org.createdAt = Instant.now();
            org.updatedAt = Instant.now();
            org.persist();
            orgs.add(org);
        }

        for (int i = 0; i < 100; i++) {
            var contact = new Contact();
            contact.accountId = account.id;
            contact.firstName = faker.name().firstName();
            contact.lastName = faker.name().lastName();
            contact.email = faker.internet().emailAddress();
            contact.phone = faker.phoneNumber().phoneNumber();
            contact.address = faker.address().streetAddress();
            contact.city = faker.address().city();
            contact.region = faker.address().state();
            contact.country = "US";
            contact.postalCode = faker.address().zipCode();
            if (!orgs.isEmpty()) {
                contact.organizationId = orgs.get(i % orgs.size()).id;
            }
            contact.createdAt = Instant.now();
            contact.updatedAt = Instant.now();
            contact.persist();
        }
    }
}
