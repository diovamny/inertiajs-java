package com.example.pingcrm.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FormValidator {

    public static void validate(Validator validator, Object form) {
        Set<ConstraintViolation<Object>> violations = validator.validate(form);
        if (!violations.isEmpty()) {
            var errors = new LinkedHashMap<String, String>();
            for (var violation : violations) {
                var path = violation.getPropertyPath().toString();
                errors.put(path, violation.getMessage());
            }
            throw new ValidationException(errors);
        }
    }

    public static String blankToNull(String value) {
        return value != null && !value.isBlank() ? value : null;
    }

    public static Long parseId(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static void normalize(ContactForm form) {
        form.first_name = blankToNull(form.first_name);
        form.last_name = blankToNull(form.last_name);
        form.email = blankToNull(form.email);
        form.phone = blankToNull(form.phone);
        form.address = blankToNull(form.address);
        form.city = blankToNull(form.city);
        form.region = blankToNull(form.region);
        form.country = blankToNull(form.country);
        form.postal_code = blankToNull(form.postal_code);
        form.organization_id = blankToNull(form.organization_id);
    }

    public static void normalize(OrganizationForm form) {
        form.name = blankToNull(form.name);
        form.email = blankToNull(form.email);
        form.phone = blankToNull(form.phone);
        form.address = blankToNull(form.address);
        form.city = blankToNull(form.city);
        form.region = blankToNull(form.region);
        form.country = blankToNull(form.country);
        form.postal_code = blankToNull(form.postal_code);
    }

    public static void normalize(UserForm form) {
        form.first_name = blankToNull(form.first_name);
        form.last_name = blankToNull(form.last_name);
        form.email = blankToNull(form.email);
        form.password = blankToNull(form.password);
    }

    public static void normalize(LoginForm form) {
        form.email = blankToNull(form.email);
        form.password = blankToNull(form.password);
    }

    public static class ValidationException extends RuntimeException {
        private final Map<String, String> errors;

        public ValidationException(Map<String, String> errors) {
            super("Validation failed");
            this.errors = errors;
        }

        public Map<String, String> getErrors() {
            return errors;
        }
    }
}
