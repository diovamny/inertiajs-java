package com.example.kitchensink.repository;

import com.example.kitchensink.entity.Contact;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ContactRepository implements PanacheRepository<Contact> {

    public long countFavorites() {
        return count("isFavorite", true);
    }
}