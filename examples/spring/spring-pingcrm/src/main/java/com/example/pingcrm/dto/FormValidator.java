package com.example.pingcrm.dto;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;

public final class FormValidator {

    private FormValidator() {
    }

    public static void validate(Validator validator, Object form) {
        var violations = validator.validate(form);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    public static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
