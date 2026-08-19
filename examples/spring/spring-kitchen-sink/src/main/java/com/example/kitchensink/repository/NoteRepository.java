package com.example.kitchensink.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kitchensink.entity.Note;

public interface NoteRepository extends JpaRepository<Note, Long> {

    long countByCreatedAtGreaterThanEqual(Instant since);

    List<Note> findTop10ByOrderByCreatedAtDesc();

    List<Note> findByContactIdOrderByCreatedAtDesc(Long contactId);
}