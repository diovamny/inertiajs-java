package io.github.diovamny.inertia.tck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class InertiaTckRunnerUnitTest {

    private static final ObjectMapper JSON = new ObjectMapper();

    @Test
    void specsLoadWithUniqueIds() {
        var cases = TckLoader.loadAll();
        assertThat(cases).isNotEmpty();
        assertThat(cases.stream().map(TckCase::id).distinct().toList())
            .hasSameSizeAs(cases);
    }

    @Test
    void everyCaseHasMethodAndPath() {
        for (var test : TckLoader.loadAll()) {
            assertThat(test.method()).isNotBlank();
            assertThat(test.path()).startsWith("/");
        }
    }

    @Test
    void navigateResolvesDotsAndIndexes() throws Exception {
        var root = JSON.readTree(
            "{\"props\":{\"users\":[{\"name\":\"Alice\"}],\"a\":\"A\"}}");
        assertThat(InertiaTckRunner.navigate(root, "props.a").asText()).isEqualTo("A");
        assertThat(InertiaTckRunner.navigate(root, "props.users[0].name").asText())
            .isEqualTo("Alice");
        assertThat(InertiaTckRunner.navigate(root, "props.missing")).isNull();
        assertThat(InertiaTckRunner.navigate(root, "props.users[9]")).isNull();
    }

    @Test
    void scalarEqualsHandlesTypes() throws Exception {
        var root = JSON.readTree("{\"s\":\"x\",\"n\":3,\"b\":true,\"nil\":null}");
        assertThat(InertiaTckRunner.scalarEquals("x", root.path("s"))).isTrue();
        assertThat(InertiaTckRunner.scalarEquals("y", root.path("s"))).isFalse();
        assertThat(InertiaTckRunner.scalarEquals(3, root.path("n"))).isTrue();
        assertThat(InertiaTckRunner.scalarEquals(true, root.path("b"))).isTrue();
        assertThat(InertiaTckRunner.scalarEquals(null, root.path("nil"))).isTrue();
        assertThat(InertiaTckRunner.scalarEquals(null, root.path("absent"))).isTrue();
        assertThat(InertiaTckRunner.scalarEquals("x", root.path("absent"))).isFalse();
    }

    @Test
    void stackOverrideDeepMerges() {
        var test = new TckCase(Map.of(
            "id", "x",
            "request", Map.of("method", "GET", "path", "/tck/page"),
            "expect", Map.of("status", 200, "headers", Map.of("A", "1")),
            "expect-spring", Map.of("status", 303, "headers", Map.of("B", "2"))));
        var merged = test.expectFor("spring");
        assertThat(merged.get("status")).isEqualTo(303);
        assertThat((Map<String, Object>) merged.get("headers"))
            .containsEntry("A", "1")
            .containsEntry("B", "2");
        assertThat(test.expectFor("quarkus").get("status")).isEqualTo(200);
    }

    @Test
    void unknownStackIsRejected() {
        assertThatThrownBy(() -> InertiaTckRunner.run("http://localhost:1", "rails"))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reportFormatsFailures() {
        var report = new TckReport("http://localhost:1", "spring");
        report.pass("a");
        report.fail("b", List.of("boom"));
        assertThat(report.green()).isFalse();
        assertThat(report.toString()).contains("1/2").contains("FAIL b").contains("boom");
    }
}
