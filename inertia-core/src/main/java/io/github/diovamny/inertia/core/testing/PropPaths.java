package io.github.diovamny.inertia.core.testing;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Framework-agnostic navigation and diffing over parsed page-prop trees
 * (nested {@link Map}s and {@link List}s). Backs the {@code where},
 * {@code has}, {@code missing} and {@code dumpDiff} testing-DSL assertions
 * in both adapters.
 *
 * <p>Paths use dots with optional list indexes: {@code users.data[0].name}.
 * </p>
 */
public final class PropPaths {

    private PropPaths() {
    }

    /**
     * Resolve a path against a prop tree.
     *
     * @param root the root map (may be {@code null})
     * @param path dot path with optional {@code [n]} indexes
     * @return the value, or {@code null} when any segment is missing
     */
    public static Object navigate(Object root, String path) {
        if (root == null || path == null || path.isBlank()) {
            return null;
        }
        Object current = root;
        for (var segment : path.split("\\.")) {
            if (current == null) {
                return null;
            }
            var bracket = segment.indexOf('[');
            if (bracket < 0) {
                if (!(current instanceof Map<?, ?> map)) {
                    return null;
                }
                current = map.get(segment);
                continue;
            }
            if (!(current instanceof Map<?, ?> map)) {
                return null;
            }
            current = map.get(segment.substring(0, bracket));
            var rest = segment.substring(bracket);
            while (rest.startsWith("[")) {
                var end = rest.indexOf(']');
                if (end < 0 || !(current instanceof List<?> list)) {
                    return null;
                }
                int index;
                try {
                    index = Integer.parseInt(rest.substring(1, end));
                } catch (NumberFormatException e) {
                    return null;
                }
                current = index >= 0 && index < list.size() ? list.get(index) : null;
                rest = rest.substring(end + 1);
            }
            if (!rest.isEmpty()) {
                return null;
            }
        }
        return current;
    }

    /**
     * Whether the path resolves to a present (non-{@code null}) value.
     */
    public static boolean present(Object root, String path) {
        return navigate(root, path) != null;
    }

    /**
     * The size of the collection/map at the path, or {@code -1} when the
     * path does not resolve to a sized value.
     */
    public static int sizeOf(Object root, String path) {
        var value = navigate(root, path);
        if (value instanceof Collection<?> collection) {
            return collection.size();
        }
        if (value instanceof Map<?, ?> map) {
            return map.size();
        }
        if (value instanceof Object[] array) {
            return array.length;
        }
        return -1;
    }

    /**
     * Recursive human-readable diff between two prop trees, one line per
     * divergence ({@code path: expected <x> but was <y>}, plus missing/extra
     * markers). Used by {@code dumpDiff} failure output.
     *
     * @param expected the expected tree
     * @param actual   the actual tree
     * @return the diff lines, empty when equal
     */
    public static List<String> diff(Object expected, Object actual) {
        var lines = new ArrayList<String>();
        diffInto("", expected, actual, lines);
        return Collections.unmodifiableList(lines);
    }

    @SuppressWarnings("unchecked")
    private static void diffInto(String path, Object expected, Object actual, List<String> lines) {
        if (Objects.equals(expected, actual)) {
            return;
        }
        var label = path.isEmpty() ? "<root>" : path;
        if (expected instanceof Map<?, ?> expectedMap && actual instanceof Map<?, ?> actualMap) {
            for (var key : ((Map<Object, Object>) expectedMap).keySet()) {
                var child = path.isEmpty() ? String.valueOf(key) : path + "." + key;
                if (!((Map<?, ?>) actualMap).containsKey(key)) {
                    lines.add(child + ": missing in actual");
                } else {
                    diffInto(child, ((Map<?, ?>) expectedMap).get(key),
                        ((Map<?, ?>) actualMap).get(key), lines);
                }
            }
            for (var key : ((Map<?, ?>) actualMap).keySet()) {
                if (!((Map<?, ?>) expectedMap).containsKey(key)) {
                    var child = path.isEmpty() ? String.valueOf(key) : path + "." + key;
                    lines.add(child + ": unexpected in actual: <"
                        + preview(((Map<?, ?>) actualMap).get(key)) + ">");
                }
            }
            return;
        }
        if (expected instanceof List<?> expectedList && actual instanceof List<?> actualList) {
            if (expectedList.size() != actualList.size()) {
                lines.add(label + ": expected size " + expectedList.size()
                    + " but was " + actualList.size());
            }
            var common = Math.min(expectedList.size(), actualList.size());
            for (var i = 0; i < common; i++) {
                diffInto(label + "[" + i + "]", expectedList.get(i), actualList.get(i), lines);
            }
            return;
        }
        lines.add(label + ": expected <" + preview(expected) + "> but was <" + preview(actual) + ">");
    }

    private static String preview(Object value) {
        if (value == null) {
            return "null";
        }
        var text = String.valueOf(value);
        return text.length() <= 120 ? text : text.substring(0, 120) + "...";
    }
}
