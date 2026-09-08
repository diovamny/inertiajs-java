package com.example.pingcrm.entity;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "users")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class User {

    @Id
    @SequenceGenerator(name = "userSeq", sequenceName = "users_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userSeq")
    public Long id;

    public Long accountId;
    public String firstName;
    public String lastName;
    public String email;
    public Instant emailVerifiedAt;
    @JsonIgnore
    public String password;
    public boolean owner;
    public String photoPath;
    @JsonIgnore
    public String rememberToken;
    public Instant createdAt;
    public Instant updatedAt;
    public Instant deletedAt;

    public boolean isDemoUser() {
        return "johndoe@example.com".equals(email);
    }

    public String getName() {
        return firstName + " " + lastName;
    }

    /** Avatar URL used by the profile edit page (60px crop). */
    @JsonIgnore
    public String getPhoto() {
        return photoPath == null || photoPath.isBlank() ? null : "/img/" + photoPath + "?w=60&h=60&fit=crop";
    }
}
