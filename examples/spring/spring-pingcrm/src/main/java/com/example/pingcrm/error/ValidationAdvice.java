package com.example.pingcrm.error;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.ConstraintViolationException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.dg.spring.inertia.api.Inertia;

/**
 * Maps form validation failures to a redirect back with the errors flashed,
 * matching the Laravel-style flow expected by the PingCRM frontend.
 */
@RestControllerAdvice
public class ValidationAdvice {

    private final Inertia inertia;

    public ValidationAdvice(Inertia inertia) {
        this.inertia = inertia;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public Object handle(ConstraintViolationException ex) {
        var errors = new LinkedHashMap<String, String>();
        for (var violation : ex.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return inertia.back().withErrors(errors);
    }
}