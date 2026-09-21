package io.github.diovamny.inertia.tck;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Outcome of a {@link InertiaTckRunner} execution: passing case ids plus,
 * per failing case, every expectation that did not hold.
 */
public final class TckReport {

    private final String baseUri;
    private final String stack;
    private final List<String> passed = new ArrayList<>();
    private final Map<String, List<String>> failures = new LinkedHashMap<>();

    TckReport(String baseUri, String stack) {
        this.baseUri = baseUri;
        this.stack = stack;
    }

    void pass(String id) {
        passed.add(id);
    }

    void fail(String id, List<String> errors) {
        failures.put(id, List.copyOf(errors));
    }

    public String baseUri() {
        return baseUri;
    }

    public String stack() {
        return stack;
    }

    public List<String> passed() {
        return Collections.unmodifiableList(passed);
    }

    public Map<String, List<String>> failures() {
        return Collections.unmodifiableMap(failures);
    }

    public int total() {
        return passed.size() + failures.size();
    }

    public boolean green() {
        return failures.isEmpty();
    }

    @Override
    public String toString() {
        var out = new StringBuilder("TCK ").append(stack).append(" @ ").append(baseUri)
            .append(": ").append(passed.size()).append("/").append(total()).append(" passed");
        failures.forEach((id, errors) -> {
            out.append("\n  FAIL ").append(id);
            errors.forEach(error -> out.append("\n    - ").append(error));
        });
        return out.toString();
    }
}
