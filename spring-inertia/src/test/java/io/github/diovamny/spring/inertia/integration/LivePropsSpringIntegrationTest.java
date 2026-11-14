package io.github.diovamny.spring.inertia.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.version-custom=test-version",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class LivePropsSpringIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @BeforeEach
    void resetCounters() {
        TestController.liveOrderResolved = 0;
        TestController.liveDeferredResolved = 0;
    }

    @Test
    void fullVisitResolvesLivePropAndEmitsMetadata() throws Exception {
        mockMvc.perform(get("/live")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.component").value("Orders/Show"))
            .andExpect(jsonPath("$.props.order.id").value(1))
            .andExpect(jsonPath("$.props.order.status").value("shipped"))
            .andExpect(jsonPath("$.props.order.revision").value(1))
            .andExpect(jsonPath("$.liveProps.order.listeners[0].channel.name").value("orders.1"))
            .andExpect(jsonPath("$.liveProps.order.listeners[0].channel.type").value("private"))
            .andExpect(jsonPath("$.liveProps.order.listeners[0].events[0]").value("OrderUpdated"));

        assertThat(TestController.liveOrderResolved).isEqualTo(1);
    }

    @Test
    void htmlVisitEmbedsResolvedLivePropAndMetadata() throws Exception {
        mockMvc.perform(get("/live"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("id=\"app\"")))
            .andExpect(content().string(containsString("\"liveProps\"")))
            .andExpect(content().string(containsString("\"orders.1\"")))
            .andExpect(content().string(containsString("\"revision\":1")));

        assertThat(TestController.liveOrderResolved).isEqualTo(1);
    }

    @Test
    void partialReloadOfLivePropResolvesItAgain() throws Exception {
        mockMvc.perform(get("/live")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "Orders/Show")
                .header("X-Inertia-Partial-Data", "order"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.props.order.revision").value(1))
            .andExpect(jsonPath("$.props.other").doesNotExist())
            .andExpect(jsonPath("$.liveProps.order.listeners[0].channel.name").value("orders.1"));

        assertThat(TestController.liveOrderResolved).isEqualTo(1);
    }

    @Test
    void foreignPartialReloadNeverRunsTheLiveResolver() throws Exception {
        mockMvc.perform(get("/live")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "Orders/Show")
                .header("X-Inertia-Partial-Data", "other"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.props.other").value("o"))
            .andExpect(jsonPath("$.props.order").doesNotExist())
            .andExpect(jsonPath("$.liveProps.order.listeners[0].channel.name").value("orders.1"));

        assertThat(TestController.liveOrderResolved).isZero();
    }

    @Test
    void deferCompositionEmitsBothMetadataSectionsWithoutResolving() throws Exception {
        mockMvc.perform(get("/live-defer")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.deferredProps.default[0]").value("stats"))
            .andExpect(jsonPath("$.liveProps.stats.listeners[0].channel.name").value("stats"))
            .andExpect(jsonPath("$.liveProps.stats.listeners[0].channel.type").value("public"))
            .andExpect(jsonPath("$.liveProps.stats.listeners[0].events[0]").value("StatsUpdated"))
            .andExpect(jsonPath("$.props.stats").doesNotExist());

        assertThat(TestController.liveDeferredResolved).isZero();
    }

    @Test
    void deferCompositionResolvesOnSelectedPartialReload() throws Exception {
        mockMvc.perform(get("/live-defer")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version")
                .header("X-Inertia-Partial-Component", "Orders/Show")
                .header("X-Inertia-Partial-Data", "stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.props.stats.total").value(42))
            .andExpect(jsonPath("$.liveProps.stats.listeners[0].channel.name").value("stats"));

        assertThat(TestController.liveDeferredResolved).isEqualTo(1);
    }

    @Test
    void throttleIsEmittedOnlyWhenConfigured() throws Exception {
        mockMvc.perform(get("/live-throttle")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.liveProps.poll.throttle").value(2000))
            .andExpect(jsonPath("$.liveProps.poll.listeners[0].channel.name").value("poll"))
            .andExpect(jsonPath("$.liveProps.poll.listeners[0].channel.type").value("public"))
            .andExpect(jsonPath("$.props.poll").value("p"));

        mockMvc.perform(get("/live")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "test-version"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.liveProps.order.throttle").doesNotExist());
    }
}
