package io.github.diovamny.inertia.core.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Immutable merge plan for one prop: value plus per-path append / prepend /
 * deep-merge operations and list-matching fields.
 *
 * <p>Framework-free. Adapters translate the qualified paths into the
 * existing {@code mergeProps} / {@code prependProps} /
 * {@code deepMergeProps} / {@code matchPropsOn} page metadata:</p>
 * <ul>
 *   <li>{@code append(path)} → {@code mergeProps} entry {@code key.path}
 *   (client appends).</li>
 *   <li>{@code prepend(path)} → {@code prependProps} entry.</li>
 *   <li>{@code deep(path)} → {@code deepMergeProps} entry.</li>
 *   <li>{@code matchOn(fields...)} → {@code matchPropsOn} entries, resolved
 *   against every append/prepend path (or the prop root when no path was
 *   declared).</li>
 * </ul>
 *
 * <p>Rules: paths are dot-notation relative to the prop; blank paths are
 * rejected; duplicates collapse (last operation wins, order of first
 * declaration kept); {@code matchOn} fields may be multiple.</p>
 */
public final class MergePlan {

    /** Append operation (client merge). */
    public enum Operation {
        APPEND, PREPEND, DEEP
    }

    private final String propKey;
    private final Object value;
    private final List<String> appendPaths;
    private final List<String> prependPaths;
    private final List<String> deepPaths;
    private final List<String> matchOnFields;

    private MergePlan(String propKey, Object value, List<String> appendPaths,
            List<String> prependPaths, List<String> deepPaths, List<String> matchOnFields) {
        this.propKey = propKey;
        this.value = value;
        this.appendPaths = List.copyOf(appendPaths);
        this.prependPaths = List.copyOf(prependPaths);
        this.deepPaths = List.copyOf(deepPaths);
        this.matchOnFields = List.copyOf(matchOnFields);
    }

    /** New builder for a prop. */
    public static Builder builder(String propKey, Object value) {
        return new Builder(propKey, value);
    }

    /** The prop key. */
    public String propKey() {
        return propKey;
    }

    /** The prop value (returned into the props map). */
    public Object value() {
        return value;
    }

    /** Relative append paths. */
    public List<String> appendPaths() {
        return appendPaths;
    }

    /** Relative prepend paths. */
    public List<String> prependPaths() {
        return prependPaths;
    }

    /** Relative deep-merge paths. */
    public List<String> deepPaths() {
        return deepPaths;
    }

    /** Match-on fields (relative to each operation path). */
    public List<String> matchOnFields() {
        return matchOnFields;
    }

    /** Qualified {@code key.path} merge entries. */
    public List<String> qualifiedMergePaths() {
        return qualify(appendPaths);
    }

    /** Qualified prepend entries. */
    public List<String> qualifiedPrependPaths() {
        return qualify(prependPaths);
    }

    /** Qualified deep-merge entries. */
    public List<String> qualifiedDeepMergePaths() {
        return qualify(deepPaths);
    }

    /**
     * Qualified match entries: every match field resolved against the prop
     * root (same rule as {@code merge(key, value, rule, matchOn...)} which
     * emits {@code key.field}). Example: {@code posts} + {@code data.id} →
     * {@code posts.data.id}.
     */
    public List<String> qualifiedMatchPaths() {
        var out = new LinkedHashSet<String>();
        for (var field : matchOnFields) {
            out.add(propKey + "." + field);
        }
        return List.copyOf(out);
    }

    private List<String> qualify(List<String> paths) {
        var out = new ArrayList<String>(paths.size());
        for (var path : paths) {
            out.add(propKey + "." + path);
        }
        return Collections.unmodifiableList(out);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof MergePlan that)) {
            return false;
        }
        return propKey.equals(that.propKey)
            && Objects.equals(value, that.value)
            && appendPaths.equals(that.appendPaths)
            && prependPaths.equals(that.prependPaths)
            && deepPaths.equals(that.deepPaths)
            && matchOnFields.equals(that.matchOnFields);
    }

    @Override
    public int hashCode() {
        return Objects.hash(propKey, value, appendPaths, prependPaths, deepPaths, matchOnFields);
    }

    @Override
    public String toString() {
        return "MergePlan{key=" + propKey + ", append=" + appendPaths
            + ", prepend=" + prependPaths + ", deep=" + deepPaths
            + ", matchOn=" + matchOnFields + "}";
    }

    /** Fluent builder with duplicate/invalid-path rules. */
    public static final class Builder {
        private final String propKey;
        private final Object value;
        private final Set<String> appendPaths = new LinkedHashSet<>();
        private final Set<String> prependPaths = new LinkedHashSet<>();
        private final Set<String> deepPaths = new LinkedHashSet<>();
        private final Set<String> matchOnFields = new LinkedHashSet<>();

        private Builder(String propKey, Object value) {
            if (propKey == null || propKey.isBlank()) {
                throw new IllegalArgumentException("propKey must not be blank");
            }
            this.propKey = propKey;
            this.value = value;
        }

        /** Declare an append (merge) of a nested path. */
        public Builder append(String path) {
            return operate(path, Operation.APPEND);
        }

        /** Declare a prepend of a nested path. */
        public Builder prepend(String path) {
            return operate(path, Operation.PREPEND);
        }

        /** Declare a deep-merge of a nested path. */
        public Builder deep(String path) {
            return operate(path, Operation.DEEP);
        }

        private Builder operate(String path, Operation operation) {
            var clean = cleanPath(path);
            // Last operation wins across kinds; keep first-declaration order.
            appendPaths.remove(clean);
            prependPaths.remove(clean);
            deepPaths.remove(clean);
            switch (operation) {
                case APPEND -> appendPaths.add(clean);
                case PREPEND -> prependPaths.add(clean);
                case DEEP -> deepPaths.add(clean);
            }
            return this;
        }

        /** List-matching fields (dot-notation, may be several). */
        public Builder matchOn(String... fields) {
            if (fields == null) {
                return this;
            }
            for (var field : fields) {
                matchOnFields.add(cleanPath(field));
            }
            return this;
        }

        /** Build the immutable plan. */
        public MergePlan build() {
            return new MergePlan(propKey, value, List.copyOf(appendPaths),
                List.copyOf(prependPaths), List.copyOf(deepPaths), List.copyOf(matchOnFields));
        }

        private static String cleanPath(String path) {
            if (path == null || path.isBlank()) {
                throw new IllegalArgumentException("merge path must not be blank");
            }
            var clean = path.trim();
            if (clean.startsWith(".") || clean.endsWith(".") || clean.contains("..")) {
                throw new IllegalArgumentException("invalid merge path: " + path);
            }
            return clean;
        }
    }
}
