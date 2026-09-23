package io.github.diovamny.inertia.core.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Immutable, order-preserving validation error bag.
 *
 * <p>Each field maps to one or more messages in insertion order. The wire
 * shape depends on {@code inertia.validation.all-errors}:</p>
 * <ul>
 *   <li>{@code false} (default, legacy): {@code Map<field, firstMessage>}.</li>
 *   <li>{@code true}: {@code Map<field, List<message>>} (arrays, even for a
 *   single message) — Inertia {@code withAllErrors} parity.</li>
 * </ul>
 *
 * <p>Framework-free: no Spring, Quarkus or Vert.x dependency.</p>
 */
public final class ValidationErrors {

    private final Map<String, List<String>> messages;

    private ValidationErrors(Map<String, List<String>> messages) {
        var copy = new LinkedHashMap<String, List<String>>();
        for (var entry : messages.entrySet()) {
            Objects.requireNonNull(entry.getKey(), "field");
            var values = entry.getValue() == null ? List.<String>of() : List.copyOf(entry.getValue());
            for (var message : values) {
                Objects.requireNonNull(message, "message for field " + entry.getKey());
            }
            copy.put(entry.getKey(), values);
        }
        this.messages = Collections.unmodifiableMap(copy);
    }

    /** Empty bag. */
    public static ValidationErrors empty() {
        return new ValidationErrors(Map.of());
    }

    /** Single message per field (legacy shape input). */
    public static ValidationErrors of(Map<String, String> singleMessages) {
        var acc = new LinkedHashMap<String, List<String>>();
        if (singleMessages != null) {
            for (var entry : singleMessages.entrySet()) {
                acc.put(entry.getKey(), entry.getValue() == null ? List.of() : List.of(entry.getValue()));
            }
        }
        return new ValidationErrors(acc);
    }

    /** One or more messages per field. */
    public static ValidationErrors ofLists(Map<String, ? extends Collection<String>> multiMessages) {
        var acc = new LinkedHashMap<String, List<String>>();
        if (multiMessages != null) {
            for (var entry : multiMessages.entrySet()) {
                acc.put(entry.getKey(),
                    entry.getValue() == null ? List.of() : List.copyOf(new ArrayList<>(entry.getValue())));
            }
        }
        return new ValidationErrors(acc);
    }

    /** Single field with one message. */
    public static ValidationErrors single(String field, String message) {
        return new ValidationErrors(Map.of(field, List.of(message)));
    }

    /** New builder for ordered accumulation. */
    public static Builder builder() {
        return new Builder();
    }

    /** Field names in insertion order. */
    public List<String> fields() {
        return List.copyOf(messages.keySet());
    }

    /** All messages for a field (empty list when absent). */
    public List<String> messagesFor(String field) {
        return messages.getOrDefault(field, List.of());
    }

    /** First message for a field, or {@code null}. */
    public String firstMessageFor(String field) {
        var list = messages.get(field);
        return list == null || list.isEmpty() ? null : list.get(0);
    }

    /** Whether the bag holds no fields. */
    public boolean isEmpty() {
        return messages.isEmpty();
    }

    /** Number of fields (not messages). */
    public int fieldCount() {
        return messages.size();
    }

    /**
     * Wire representation.
     *
     * @param allErrors {@code true} for arrays per field, {@code false} for first-message strings
     * @return ordered, unmodifiable map ready for JSON/flash
     */
    public Map<String, Object> toWireMap(boolean allErrors) {
        var wire = new LinkedHashMap<String, Object>();
        for (var entry : messages.entrySet()) {
            if (allErrors) {
                wire.put(entry.getKey(), List.copyOf(entry.getValue()));
            } else {
                var list = entry.getValue();
                wire.put(entry.getKey(), list.isEmpty() ? "" : list.get(0));
            }
        }
        return Collections.unmodifiableMap(wire);
    }

    /** Keep only the given fields (validate-only filtering). */
    public ValidationErrors filter(Collection<String> fields) {
        if (fields == null || fields.isEmpty()) {
            return this;
        }
        var acc = new LinkedHashMap<String, List<String>>();
        for (var field : fields) {
            var list = messages.get(field);
            if (list != null) {
                acc.put(field, list);
            }
        }
        return new ValidationErrors(acc);
    }

    /** Merge two bags, appending messages for duplicate fields in order. */
    public ValidationErrors merge(ValidationErrors other) {
        if (other == null || other.isEmpty()) {
            return this;
        }
        if (isEmpty()) {
            return other;
        }
        var acc = new LinkedHashMap<String, List<String>>(messages);
        for (var entry : other.messages.entrySet()) {
            acc.merge(entry.getKey(), entry.getValue(), (left, right) -> {
                var combined = new ArrayList<>(left);
                combined.addAll(right);
                return List.copyOf(combined);
            });
        }
        return new ValidationErrors(acc);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ValidationErrors that)) {
            return false;
        }
        return messages.equals(that.messages);
    }

    @Override
    public int hashCode() {
        return messages.hashCode();
    }

    @Override
    public String toString() {
        return "ValidationErrors" + messages;
    }

    /** Ordered accumulator. */
    public static final class Builder {
        private final Map<String, List<String>> acc = new LinkedHashMap<>();

        /** Append a message for a field, preserving order. */
        public Builder add(String field, String message) {
            Objects.requireNonNull(field, "field");
            Objects.requireNonNull(message, "message");
            acc.computeIfAbsent(field, key -> new ArrayList<>()).add(message);
            return this;
        }

        /** Append several messages for a field, preserving order. */
        public Builder addAll(String field, Collection<String> fieldMessages) {
            Objects.requireNonNull(field, "field");
            if (fieldMessages != null) {
                for (var message : fieldMessages) {
                    add(field, message);
                }
            }
            return this;
        }

        /** Build the immutable bag. */
        public ValidationErrors build() {
            var frozen = new LinkedHashMap<String, List<String>>();
            for (var entry : acc.entrySet()) {
                frozen.put(entry.getKey(), List.copyOf(entry.getValue()));
            }
            return new ValidationErrors(frozen);
        }
    }
}
