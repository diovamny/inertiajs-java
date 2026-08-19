package com.example.kitchensink.dto;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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

    /**
     * Validate only the given fields of the form and return their errors as
     * a {@code field -> message} map. Unrelated fields are never validated,
     * so empty siblings do not produce errors during precognition requests.
     *
     * @param validator the bean validation instance
     * @param form      the form instance
     * @param fields    the fields to validate
     * @return a map of field errors, empty when all fields are valid
     */
    public static Map<String, String> fieldErrors(Validator validator, Object form, Collection<String> fields) {
        var errors = new LinkedHashMap<String, String>();
        for (var field : fields) {
            for (var violation : validator.validateProperty(form, field)) {
                errors.putIfAbsent(field, violation.getMessage());
            }
        }
        return errors;
    }

    /**
     * Resolve the effective fields to validate for a precognition request:
     * the comma-separated {@code Precognition-Validate-Only} value, with the
     * dependent {@code password}/{@code password_confirmation} pair always
     * expanded together. A blank value (or no value) means every field.
     *
     * @param validateOnly the raw {@code Precognition-Validate-Only} header
     * @param allFields    every validatable field of the form
     * @return the fields to validate
     */
    public static Set<String> precognitionFields(String validateOnly, Collection<String> allFields) {
        var fields = new LinkedHashSet<String>();
        if (validateOnly != null && !validateOnly.isBlank()) {
            for (var part : validateOnly.split(",")) {
                var field = part.trim();
                if (!field.isEmpty()) {
                    fields.add(field);
                }
            }
        }
        if (fields.isEmpty()) {
            return new LinkedHashSet<>(allFields);
        }
        if (fields.contains("password") || fields.contains("password_confirmation")) {
            fields.add("password");
            fields.add("password_confirmation");
        }
        return fields;
    }

    public static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}