package com.example.kitchensink.seed;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import com.github.javafaker.Faker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.kitchensink.entity.Contact;
import com.example.kitchensink.entity.Note;
import com.example.kitchensink.entity.Organization;
import com.example.kitchensink.entity.User;
import com.example.kitchensink.repository.ContactRepository;
import com.example.kitchensink.repository.NoteRepository;
import com.example.kitchensink.repository.OrganizationRepository;
import com.example.kitchensink.repository.UserRepository;
import com.example.kitchensink.service.AuthService;

@Component
public class DataSeeder implements ApplicationRunner {

    private final UserRepository users;
    private final OrganizationRepository organizations;
    private final ContactRepository contacts;
    private final NoteRepository notes;
    private final boolean enabled;

    public DataSeeder(UserRepository users, OrganizationRepository organizations,
            ContactRepository contacts, NoteRepository notes,
            @Value("${kitchensink.seed.enabled:true}") boolean enabled) {
        this.users = users;
        this.organizations = organizations;
        this.contacts = contacts;
        this.notes = notes;
        this.enabled = enabled;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!enabled) {
            return;
        }
        if (users.count() > 0) {
            return;
        }

        System.out.println("Seeding Kitchen Sink demo data...");
        var faker = new Faker(new Random(42));
        var userIds = seedUsers(faker);
        seedOrganizations(faker);
        var contactList = seedContacts(faker, userIds);
        seedNotes(faker, contactList, userIds);
        System.out.println("Kitchen Sink demo data seeded.");
    }

    private List<Long> seedUsers(Faker faker) {
        var userIds = new ArrayList<Long>();
        for (int i = 0; i < 4; i++) {
            var user = new User();
            user.name = i == 0 ? "Test User" : faker.name().fullName();
            user.email = i == 0 ? "test@example.com" : faker.internet().emailAddress();
            user.emailVerifiedAt = Instant.now();
            user.password = AuthService.hash("password");
            user.createdAt = randomPast(faker, 365);
            user.updatedAt = user.createdAt;
            users.save(user);
            userIds.add(user.id);
        }
        return userIds;
    }

    private void seedOrganizations(Faker faker) {
        for (int i = 0; i < 15; i++) {
            var organization = new Organization();
            organization.name = faker.company().name();
            organization.createdAt = randomPast(faker, 365);
            organization.updatedAt = organization.createdAt;
            organizations.save(organization);
        }
    }

    private List<Contact> seedContacts(Faker faker, List<Long> userIds) {
        var all = new ArrayList<Contact>();
        for (int i = 0; i < 75; i++) {
            var contact = new Contact();
            contact.firstName = faker.name().firstName();
            contact.lastName = faker.name().lastName();
            contact.email = faker.internet().emailAddress();
            contact.phone = faker.phoneNumber().phoneNumber();
            contact.organizationId = 1L + faker.random().nextInt(15);
            contact.isFavorite = faker.random().nextBoolean();
            contact.createdAt = randomPast(faker, 365);
            contact.updatedAt = contact.createdAt;
            contacts.save(contact);
            all.add(contact);
        }
        for (int i = 0; i < 25; i++) {
            var contact = new Contact();
            contact.firstName = faker.name().firstName();
            contact.lastName = faker.name().lastName();
            contact.email = faker.internet().emailAddress();
            contact.phone = faker.phoneNumber().phoneNumber();
            contact.organizationId = null;
            contact.isFavorite = faker.random().nextBoolean();
            contact.createdAt = randomPast(faker, 365);
            contact.updatedAt = contact.createdAt;
            contacts.save(contact);
            all.add(contact);
        }
        return all;
    }

    private void seedNotes(Faker faker, List<Contact> contactList, List<Long> userIds) {
        Collections.shuffle(contactList, new Random(7));
        var chosen = contactList.subList(0, Math.min(40, contactList.size()));
        for (var contact : chosen) {
            int notesPerContact = ThreadLocalRandom.current().nextInt(1, 6);
            for (int n = 0; n < notesPerContact; n++) {
                var note = new Note();
                note.body = faker.lorem().sentence(ThreadLocalRandom.current().nextInt(5, 15));
                note.contactId = contact.id;
                note.userId = userIds.get(faker.random().nextInt(userIds.size()));
                note.createdAt = randomPast(faker, 90);
                note.updatedAt = note.createdAt;
                notes.save(note);
            }
        }
    }

    private Instant randomPast(Faker faker, int days) {
        return Instant.now().minus(faker.random().nextInt(days), ChronoUnit.DAYS);
    }
}