package com.example.kitchensink.repository;

import com.example.kitchensink.entity.Note;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NoteRepository implements PanacheRepository<Note> {

    public long countSince(java.time.Instant since) {
        return count("createdAt >= ?1", since);
    }
}
