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
@Table(name = "organizations")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Organization {

    @Id
    @SequenceGenerator(name = "organizationSeq", sequenceName = "organizations_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organizationSeq")
    public Long id;

    public String name;
    public Instant createdAt;
    public Instant updatedAt;
}
