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
@Table(name = "contacts")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Contact extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "contactSeq", sequenceName = "contacts_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contactSeq")
    public Long id;

    public Long accountId;
    public Long organizationId;
    public String firstName;
    public String lastName;
    public String email;
    public String phone;
    public String address;
    public String city;
    public String region;
    public String country;
    public String postalCode;
    public Instant createdAt;
    public Instant updatedAt;
    public Instant deletedAt;

    public String getName() {
        return firstName + " " + lastName;
    }
}
