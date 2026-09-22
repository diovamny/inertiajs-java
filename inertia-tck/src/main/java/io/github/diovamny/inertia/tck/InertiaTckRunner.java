package io.github.diovamny.inertia.tck;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Stack-agnostic HTTP executor for the normative {@code protocol-v3} YAML
 * suite. Point it at any running adapter (Spring MVC, Quarkus JAX-RS,
 * Quarkus reactive routes) and it certifies the wire contract: handshake,
 * partial reloads, deferred/once props, redirects, versioning, adapter-mode
 * CSRF and validation errors.
 *
 * <p>Sessions persist across cases within one run (cookie jar), so
 * multi-step flows (flash chains, login-gated pages) work. Redirects are
 * never followed: the status and {@code Location} are asserted as-is.
 */
public final class InertiaTckRunner {

    private static final ObjectMapper JSON = new ObjectMapper();

    private InertiaTckRunner() {
    }

    /**
     * Run the whole suite; throws {@link AssertionError} with the full
     * report unless every case passes.
     */
    public static void assertGreen(String baseUri, String stack) {
        var report = run(baseUri, stack);
        if (!report.green()) {
            throw new AssertionError("Inertia TCK failures:\n" + report);
        }
    }

    /**
     * Run the whole suite and return the report.
     */
    public static TckReport run(String baseUri, String stack) {
        return runSubset(baseUri, stack, null, java.util.function.UnaryOperator.identity());
    }

    /**
     * Run a subset of cases, optionally rewriting request paths (used to
     * certify alternate transports such as reactive routes against the same
     * normative cases).
     *
     * @param ids        case ids to run, or {@code null} for all
     * @param pathMapper maps each case path to the transport-specific path
     */
    public static TckReport runSubset(String baseUri, String stack,
            java.util.Collection<String> ids,
            java.util.function.UnaryOperator<String> pathMapper) {
        if (!"spring".equals(stack) && !"quarkus".equals(stack)) {
            throw new IllegalArgumentException("TCK stack must be 'spring' or 'quarkus', got: " + stack);
        }
        var client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NEVER)
            .connectTimeout(Duration.ofSeconds(10))
            .cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL))
            .build();
        var report = new TckReport(baseUri, stack);
        for (var test : TckLoader.loadAll()) {
            if (ids != null && !ids.contains(test.id())) {
                continue;
            }
            var errors = execute(client, baseUri, pathMapper.apply(test.path()), test, stack,
                pathMapper);
            if (errors.isEmpty()) {
                report.pass(test.id());
            } else {
                report.fail(test.id(), errors);
            }
        }
        return report;
    }

    /**
     * Run a subset and throw unless every selected case passes.
     */
    public static void assertSubsetGreen(String baseUri, String stack,
            java.util.Collection<String> ids,
            java.util.function.UnaryOperator<String> pathMapper) {
        var report = runSubset(baseUri, stack, ids, pathMapper);
        if (!report.green()) {
            throw new AssertionError("Inertia TCK failures:\n" + report);
        }
    }

    private static List<String> execute(HttpClient client, String baseUri, String path,
            TckCase test, String stack, java.util.function.UnaryOperator<String> pathMapper) {
        var errors = new ArrayList<String>();
        var headers = new java.util.LinkedHashMap<String, String>();
        // Real browsers always send the serving origin (including a
        // non-default port) in Referer; ${baseUri} keeps cases realistic on
        // random test ports.
        test.headers().forEach((name, value) ->
            headers.put(name, value.replace("${baseUri}", baseUri)));
        if (Boolean.TRUE.equals(test.expectFor(stack).get("withCsrfToken"))) {
            var token = fetchCsrfToken(client, baseUri, test, stack, pathMapper, errors);
            if (token == null) {
                return errors;
            }
            headers.put("X-XSRF-TOKEN", token);
        }
        HttpResponse<String> response;
        try {
            var builder = HttpRequest.newBuilder(URI.create(baseUri + path))
                .timeout(Duration.ofSeconds(10));
            headers.forEach(builder::header);
            if (test.multipart() != null) {
                var boundary = String.valueOf(test.multipart().getOrDefault("boundary", "tckboundary"));
                builder.header("Content-Type", "multipart/form-data; boundary=" + boundary);
                builder.method(test.method(),
                    HttpRequest.BodyPublishers.ofString(buildMultipart(test.multipart(), boundary)));
            } else if (test.body() != null) {
                if (test.contentType() != null) {
                    builder.header("Content-Type", test.contentType());
                }
                builder.method(test.method(), HttpRequest.BodyPublishers.ofString(test.body()));
            } else {
                builder.method(test.method(), HttpRequest.BodyPublishers.noBody());
            }
            response = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        } catch (Exception e) {
            return List.of("request failed: " + e);
        }
        var expect = test.expectFor(stack);
        checkStatus(expect, response, errors);
        checkContentType(expect, response, errors);
        checkHeaders(expect, response, errors);
        checkLocation(expect, response, errors);
        checkBody(expect, response, errors);
        checkJson(expect, response, errors, test.id());
        return errors;
    }

    private static String fetchCsrfToken(HttpClient client, String baseUri, TckCase test,
            String stack, java.util.function.UnaryOperator<String> pathMapper, List<String> errors) {
        try {
            var tokenPath = pathMapper.apply(String.valueOf(
                test.expectFor(stack).getOrDefault("csrfTokenPath", "/tck/page")));
            var tokenResponse = client.send(
                HttpRequest.newBuilder(URI.create(baseUri + tokenPath))
                    .timeout(Duration.ofSeconds(10)).GET().build(),
                HttpResponse.BodyHandlers.ofString());
            for (var setCookie : tokenResponse.headers().allValues("Set-Cookie")) {
                if (setCookie.startsWith("XSRF-TOKEN=")) {
                    var end = setCookie.indexOf(';');
                    return setCookie.substring("XSRF-TOKEN=".length(), end < 0 ? setCookie.length() : end);
                }
            }
            errors.add("withCsrfToken: no XSRF-TOKEN cookie issued by " + tokenPath);
            return null;
        } catch (Exception e) {
            errors.add("withCsrfToken: token fetch failed: " + e);
            return null;
        }
    }

    private static void checkStatus(Map<String, Object> expect, HttpResponse<String> response,
            List<String> errors) {
        var expected = expect.get("status");
        if (expected != null && ((Number) expected).intValue() != response.statusCode()) {
            errors.add("status: expected " + expected + " but was " + response.statusCode()
                + " body=" + preview(response.body()));
        }
    }

    private static void checkContentType(Map<String, Object> expect, HttpResponse<String> response,
            List<String> errors) {
        var expected = expect.get("contentTypeContains");
        if (expected != null) {
            var actual = response.headers().firstValue("Content-Type").orElse("");
            if (!actual.contains(expected.toString())) {
                errors.add("content-type: expected to contain '" + expected + "' but was '" + actual + "'");
            }
        }
    }

    private static void checkHeaders(Map<String, Object> expect, HttpResponse<String> response,
            List<String> errors) {
        var headers = asMap(expect.get("headers"));
        headers.forEach((name, expected) -> {
            var actual = response.headers().firstValue(name).orElse(null);
            if (!String.valueOf(expected).equals(actual)) {
                errors.add("header '" + name + "': expected '" + expected + "' but was '" + actual + "'");
            }
        });
        var existing = asList(expect.get("headersExist"));
        existing.forEach(name -> {
            if (response.headers().firstValue(String.valueOf(name)).isEmpty()) {
                errors.add("header '" + name + "' expected to exist but is missing");
            }
        });
    }

    private static void checkBody(Map<String, Object> expect, HttpResponse<String> response,
            List<String> errors) {
        var body = response.body() != null ? response.body() : "";
        for (var snippet : asList(expect.get("bodyContains"))) {
            if (!body.contains(String.valueOf(snippet))) {
                errors.add("body: expected to contain '" + snippet + "' got " + preview(body));
            }
        }
        for (var snippet : asList(expect.get("bodyAbsent"))) {
            if (body.contains(String.valueOf(snippet))) {
                errors.add("body: expected NOT to contain '" + snippet + "'");
            }
        }
        for (var name : asList(expect.get("setsCookie"))) {
            var prefix = name + "=";
            var found = response.headers().allValues("Set-Cookie").stream()
                .anyMatch(h -> h.startsWith(prefix));
            if (!found) {
                errors.add("expected Set-Cookie '" + prefix + "...' but got "
                    + response.headers().allValues("Set-Cookie"));
            }
        }
    }

    private static void checkLocation(Map<String, Object> expect, HttpResponse<String> response,
            List<String> errors) {
        var location = response.headers().firstValue("Location").orElse(null);
        var exact = expect.get("location");
        if (exact != null && !String.valueOf(exact).equals(location)) {
            errors.add("location: expected '" + exact + "' but was '" + location + "'");
        }
        var suffix = expect.get("locationEndsWith");
        if (suffix != null && (location == null || !location.endsWith(String.valueOf(suffix)))) {
            errors.add("location: expected to end with '" + suffix + "' but was '" + location + "'");
        }
        var inertiaLocation = response.headers().firstValue("X-Inertia-Location").orElse(null);
        var expectedInertia = expect.get("xInertiaLocation");
        if (expectedInertia != null && !String.valueOf(expectedInertia).equals(inertiaLocation)) {
            errors.add("X-Inertia-Location: expected '" + expectedInertia + "' but was '"
                + inertiaLocation + "'");
        }
    }

    @SuppressWarnings("unchecked")
    private static void checkJson(Map<String, Object> expect, HttpResponse<String> response,
            List<String> errors, String caseId) {
        var component = expect.get("component");
        var jsonPaths = asMap(expect.get("json"));
        var jsonAbsent = asList(expect.get("jsonAbsent"));
        var jsonContains = asMap(expect.get("jsonContains"));
        var jsonMissing = asMap(expect.get("jsonMissing"));
        var jsonKeys = asList(expect.get("jsonKeys"));
        var propsKeys = asList(expect.get("propsKeys"));
        var propsAbsent = asList(expect.get("propsAbsent"));
        if (component == null && jsonPaths.isEmpty() && jsonAbsent.isEmpty()
                && jsonContains.isEmpty() && jsonMissing.isEmpty() && jsonKeys.isEmpty()
                && propsKeys.isEmpty() && propsAbsent.isEmpty()) {
            return;
        }
        JsonNode root;
        try {
            root = JSON.readTree(response.body());
        } catch (Exception e) {
            errors.add("body is not JSON (" + caseId + "): " + preview(response.body()));
            return;
        }
        if (component != null) {
            var actual = root.path("component").asText(null);
            if (!String.valueOf(component).equals(actual)) {
                errors.add("component: expected '" + component + "' but was '" + actual + "'");
            }
        }
        jsonPaths.forEach((path, expected) -> {
            var node = navigate(root, path);
            if (!scalarEquals(expected, node)) {
                errors.add("json '" + path + "': expected <" + expected + "> but was <" + node + ">");
            }
        });
        jsonAbsent.forEach(path -> {
            var node = navigate(root, String.valueOf(path));
            if (node != null && !node.isNull() && !node.isMissingNode()) {
                errors.add("json '" + path + "' expected absent but was <" + node + ">");
            }
        });
        jsonContains.forEach((path, expected) -> {
            var node = navigate(root, String.valueOf(path));
            if (node != null && node.isArray()) {
                var found = false;
                for (var item : node) {
                    if (scalarEquals(expected, item)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    errors.add("json '" + path + "' expected to contain <" + expected + "> but was <"
                        + node + ">");
                }
            } else if (node != null && node.isObject()) {
                if (!node.has(String.valueOf(expected))) {
                    errors.add("json '" + path + "' expected to have field <" + expected + "> but was <"
                        + node + ">");
                }
            } else if (!scalarEquals(expected, node)) {
                errors.add("json '" + path + "': expected <" + expected + "> but was <" + node + ">");
            }
        });
        jsonMissing.forEach((path, unexpected) -> {
            var node = navigate(root, String.valueOf(path));
            if (node != null && node.isArray()) {
                for (var item : node) {
                    if (scalarEquals(unexpected, item)) {
                        errors.add("json '" + path + "' must not contain <" + unexpected + "> but was <"
                            + node + ">");
                        break;
                    }
                }
            } else if (node != null && node.isObject()) {
                if (node.has(String.valueOf(unexpected))) {
                    errors.add("json '" + path + "' must not have field <" + unexpected + "> but was <"
                        + node + ">");
                }
            } else if (scalarEquals(unexpected, node)) {
                errors.add("json '" + path + "' must not equal <" + unexpected + "> but was <" + node
                    + ">");
            }
        });
        for (var key : jsonKeys) {
            if (!root.has(String.valueOf(key))) {
                errors.add("json: expected top-level key '" + key + "' missing");
            }
        }
        var props = root.path("props");
        for (var key : propsKeys) {
            if (!props.has(String.valueOf(key))) {
                errors.add("props: expected key '" + key + "' missing in " + props);
            }
        }
        for (var key : propsAbsent) {
            if (props.has(String.valueOf(key))) {
                errors.add("props: key '" + key + "' should be absent but was <"
                    + props.get(String.valueOf(key)) + ">");
            }
        }
    }

    static JsonNode navigate(JsonNode root, String path) {
        JsonNode current = root;
        for (var segment : path.split("\\.")) {
            if (current == null || current.isMissingNode()) {
                return null;
            }
            var bracket = segment.indexOf('[');
            if (bracket < 0) {
                current = current.path(segment);
                continue;
            }
            current = current.path(segment.substring(0, bracket));
            var rest = segment.substring(bracket);
            while (rest.startsWith("[")) {
                var end = rest.indexOf(']');
                if (end < 0) {
                    return null;
                }
                int index;
                try {
                    index = Integer.parseInt(rest.substring(1, end));
                } catch (NumberFormatException e) {
                    return null;
                }
                current = current.path(index);
                rest = rest.substring(end + 1);
            }
        }
        return current == null || current.isMissingNode() ? null : current;
    }

    static boolean scalarEquals(Object expected, JsonNode node) {
        if (expected == null) {
            return node == null || node.isNull() || node.isMissingNode();
        }
        if (node == null || node.isNull() || node.isMissingNode()) {
            return false;
        }
        if (expected instanceof Boolean) {
            return node.isBoolean() && node.booleanValue() == (Boolean) expected;
        }
        if (expected instanceof Number) {
            return node.isNumber()
                && Double.compare(node.doubleValue(), ((Number) expected).doubleValue()) == 0;
        }
        return node.isTextual() && node.textValue().equals(String.valueOf(expected));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : Map.of();
    }

    @SuppressWarnings("unchecked")
    private static List<Object> asList(Object value) {
        return value instanceof List ? (List<Object>) value : List.of();
    }

    @SuppressWarnings("unchecked")
    static String buildMultipart(Map<String, Object> spec, String boundary) {
        var out = new StringBuilder();
        var parts = spec.get("parts");
        var list = parts instanceof List ? (List<Object>) parts : List.of();
        for (var raw : list) {
            if (!(raw instanceof Map)) {
                continue;
            }
            var part = (Map<String, Object>) raw;
            out.append("--").append(boundary).append("\r\n");
            out.append("Content-Disposition: form-data; name=\"").append(part.get("name")).append("\"");
            if (part.get("filename") != null) {
                out.append("; filename=\"").append(part.get("filename")).append("\"");
            }
            out.append("\r\n");
            if (part.get("contentType") != null) {
                out.append("Content-Type: ").append(part.get("contentType")).append("\r\n");
            }
            out.append("\r\n");
            out.append(part.getOrDefault("content", ""));
            out.append("\r\n");
        }
        out.append("--").append(boundary).append("--\r\n");
        return out.toString();
    }

    private static String preview(String body) {
        if (body == null) {
            return "null";
        }
        return body.length() <= 200 ? body : body.substring(0, 200) + "...";
    }
}
