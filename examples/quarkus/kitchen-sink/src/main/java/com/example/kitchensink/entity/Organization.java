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
@Table(name = "organizations")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Organization extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "organizationSeq", sequenceName = "organizations_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organizationSeq")
    public Long id;

    public String name;
    public Instant createdAt;
    public Instant updatedAt;
}
