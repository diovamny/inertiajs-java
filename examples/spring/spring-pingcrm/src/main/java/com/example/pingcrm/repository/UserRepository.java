package com.example.pingcrm.repository;

import com.example.pingcrm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmail(String email);

    /** Whether any user (including soft-deleted) uses the email. */
    @Query("select count(u) > 0 from User u where u.email = :email and (:exclude is null or u.id <> :exclude)")
    boolean emailExistsForOtherUser(@Param("email") String email, @Param("exclude") Long excludeUserId);
}