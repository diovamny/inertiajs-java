package com.example.pingcrm.entity;

import java.time.Instant;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@Entity
@Table(name = "accounts")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Account extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "accountSeq", sequenceName = "accounts_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "accountSeq")
    public Long id;

    public String name;
    public Instant createdAt;
    public Instant updatedAt;
}