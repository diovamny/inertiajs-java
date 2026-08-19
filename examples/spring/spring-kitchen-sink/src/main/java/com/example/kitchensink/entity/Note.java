package com.example.kitchensink.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Entity
@Table(name = "notes")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Note {

    @Id
    @SequenceGenerator(name = "noteSeq", sequenceName = "notes_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "noteSeq")
    public Long id;

    public String body;
    public Long contactId;
    public Long userId;
    public Instant createdAt;
    public Instant updatedAt;
}
