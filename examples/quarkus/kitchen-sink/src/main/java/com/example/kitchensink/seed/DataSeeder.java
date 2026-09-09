package com.example.kitchensink.seed;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.UserTransaction;

import com.github.javafaker.Faker;
import com.example.kitchensink.entity.Contact;
import com.example.kitchensink.entity.Note;
import com.example.kitchensink.entity.Organization;
import com.example.kitchensink.entity.User;
import com.example.kitchensink.service.AuthService;
import io.quarkus.runtime.StartupEvent;

@ApplicationScoped
public class DataSeeder {

    @Inject
    EntityManager em;

    @Inject
    UserTransaction utx;

    @Inject
    org.flywaydb.core.Flyway flyway;

    void onStart(@Observes StartupEvent ev) {
        flyway.migrate();

        var userCount = em.createQuery("SELECT COUNT(u) FROM User u", Long.class).getSingleResult();
        if (userCount > 0) return;

        System.out.println("Seeding Kitchen Sink demo data...");
        var faker = new Faker(new Random(42));

        var userIds = seedUsers(faker);
        seedOrganizations(faker);
        var contacts = seedContacts(faker, userIds);
        seedNotes(faker, contacts, userIds);
        System.out.println("Kitchen Sink demo data seeded.");
    }

    private List<Long> seedUsers(Faker faker) {
        var userIds = new ArrayList<Long>();
        seedBatch("users", 4, (i) -> {
            var user = new User();
            user.name = i == 0 ? "Test User" : faker.name().fullName();
            user.email = i == 0 ? "test@example.com" : faker.internet().emailAddress();
            user.emailVerifiedAt = Instant.now();
            user.password = AuthService.hash("password");
            user.createdAt = randomPast(faker, 365);
            user.updatedAt = user.createdAt;
            user.persist();
            userIds.add(user.id);
        });
        return userIds;
    }

    private void seedOrganizations(Faker faker) {
        seedBatch("organizations", 15, (i) -> {
            var organization = new Organization();
            organization.name = faker.company().name();
            organization.createdAt = randomPast(faker, 365);
            organization.updatedAt = organization.createdAt;
            organization.persist();
        });
    }

    private List<Contact> seedContacts(Faker faker, List<Long> userIds) {
        var all = new ArrayList<Contact>();
        seedBatch("contacts", 75, (i) -> {
            var contact = new Contact();
            contact.firstName = faker.name().firstName();
            contact.lastName = faker.name().lastName();
            contact.email = faker.internet().emailAddress();
            contact.phone = faker.phoneNumber().phoneNumber();
            contact.organizationId = 1L + faker.random().nextInt(15);
            contact.isFavorite = faker.random().nextBoolean();
            contact.createdAt = randomPast(faker, 365);
            contact.updatedAt = contact.createdAt;
            contact.persist();
            all.add(contact);
        });
        seedBatch("contacts without organization", 25, (i) -> {
            var contact = new Contact();
            contact.firstName = faker.name().firstName();
            contact.lastName = faker.name().lastName();
            contact.email = faker.internet().emailAddress();
            contact.phone = faker.phoneNumber().phoneNumber();
            contact.organizationId = null;
            contact.isFavorite = faker.random().nextBoolean();
            contact.createdAt = randomPast(faker, 365);
            contact.updatedAt = contact.createdAt;
            contact.persist();
            all.add(contact);
        });
        return all;
    }

    private void seedNotes(Faker faker, List<Contact> contacts, List<Long> userIds) {
        Collections.shuffle(contacts, new Random(7));
        var chosen = contacts.subList(0, Math.min(40, contacts.size()));
        for (var contact : chosen) {
            int notesPerContact = ThreadLocalRandom.current().nextInt(1, 6);
            for (int n = 0; n < notesPerContact; n++) {
                final var contactRef = contact;
                seedBatch("notes", 1, (i) -> {
                    var note = new Note();
                    note.body = faker.lorem().sentence(ThreadLocalRandom.current().nextInt(5, 15));
                    note.contactId = contactRef.id;
                    note.userId = userIds.get(faker.random().nextInt(userIds.size()));
                    note.createdAt = randomPast(faker, 90);
                    note.updatedAt = note.createdAt;
                    note.persist();
                });
            }
        }
    }

    private Instant randomPast(Faker faker, int days) {
        return Instant.now().minus(faker.random().nextInt(days), ChronoUnit.DAYS);
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
