package com.example.kitchensink.entity;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Table(name = "users")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class User extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "userSeq", sequenceName = "users_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "userSeq")
    public Long id;

    public String name;
    public String email;
    public Instant emailVerifiedAt;
    @JsonIgnore
    public String password;
    @JsonIgnore
    public String rememberToken;
    public Instant createdAt;
    public Instant updatedAt;

    public String getName() {
        return name;
    }
}
