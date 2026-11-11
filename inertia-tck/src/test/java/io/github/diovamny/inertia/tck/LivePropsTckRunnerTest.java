package io.github.diovamny.inertia.tck;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Contract check for the normative live-props cases loaded from
 * {@code protocol-v3/11-live-props.yaml}. The HTTP execution is certified by
 * the adapter suites ({@code TckSpringTest}, {@code TckQuarkusTest},
 * {@code TckReactiveTest}); this test keeps the spec well-formed and indexed
 * where adapter runs cannot reach (core/TCK module build).
 */
class LivePropsTckRunnerTest {

    private static final List<String> LIVE_PROP_CASES = List.of(
        "live-props-page-json",
        "live-props-partial-reload-resolves",
        "live-props-partial-reload-foreign-key-skips-resolver",
        "live-props-throttle",
        "live-props-empty-omitted",
        "live-props-defer-composition");

    @Test
    void livePropsSpecsAreIndexed() {
        var cases = TckLoader.loadAll();
        var ids = cases.stream().map(TckCase::id).filter(LIVE_PROP_CASES::contains).toList();
        assertThat(ids).containsExactlyInAnyOrderElementsOf(LIVE_PROP_CASES);
    }

    @Test
    void everyLivePropsCaseTargetsTheFixedEndpoints() {
        var cases = TckLoader.loadAll().stream()
            .filter(test -> LIVE_PROP_CASES.contains(test.id()))
            .toList();
        assertThat(cases).hasSize(LIVE_PROP_CASES.size());
        for (var test : cases) {
            assertThat(test.method()).isEqualTo("GET");
            assertThat(test.path()).startsWith("/tck/live-props");
            assertThat(test.headers()).containsEntry("X-Inertia", "true");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void livePropsCasesDeclareTheirWireExpectations() {
        var byId = TckLoader.loadAll().stream()
            .filter(test -> LIVE_PROP_CASES.contains(test.id()))
            .collect(java.util.stream.Collectors.toMap(TckCase::id, test -> test));

        assertThat(byId.get("live-props-page-json").expectFor("spring"))
            .containsEntry("component", "Tck/LiveProps")
            .containsKey("jsonKeys");
        assertThat(byId.get("live-props-partial-reload-resolves").headers())
            .containsEntry("X-Inertia-Partial-Data", "order, mirror");
        assertThat(byId.get("live-props-partial-reload-foreign-key-skips-resolver").headers())
            .containsEntry("X-Inertia-Partial-Data", "mirror");
        assertThat(byId.get("live-props-throttle").expectFor("quarkus"))
            .containsKey("json");
        assertThat(byId.get("live-props-empty-omitted").expectFor("spring"))
            .containsEntry("component", "Tck/LivePropsEmpty")
            .containsEntry("jsonAbsent", List.of("liveProps"));
        assertThat((Map<String, Object>) byId.get("live-props-defer-composition").expectFor("spring")
            .get("jsonContains"))
            .containsEntry("deferredProps.default", "stats");
    }
}
