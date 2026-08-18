package com.example.kitchensink.entity;

import java.time.Instant;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

@Entity
@Table(name = "notes")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Note extends PanacheEntityBase {

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
