package io.github.diovamny.inertia.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class PageObjectLivePropsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    private static Map<String, LivePropMetadata> orderLiveProps() {
        var liveProps = new LinkedHashMap<String, LivePropMetadata>();
        liveProps.put("order", new LivePropMetadata(
            List.of(new LivePropListener(
                LiveChannel.privateChannel("orders.1"), "OrderUpdated")),
            1000));
        return liveProps;
    }

    private static PageObject pageWithLiveProps(Map<String, LivePropMetadata> liveProps) {
        var order = new LinkedHashMap<String, Object>();
        order.put("id", 1);
        order.put("status", "shipped");
        var props = new LinkedHashMap<String, Object>();
        props.put("order", order);
        return new PageObject("Orders/Show", props, "/orders/1", "c3ab87",
            null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, liveProps);
    }

    @Test
    void serializesLivePropsWhenPresent() throws Exception {
        var json = mapper.writeValueAsString(pageWithLiveProps(orderLiveProps()));
        var tree = mapper.readTree(json);

        assertThat(tree.has("liveProps")).isTrue();
        assertThat(tree.path("liveProps").size()).isEqualTo(1);
        var order = tree.path("liveProps").path("order");
        assertThat(order.path("throttle").asInt()).isEqualTo(1000);
        assertThat(order.path("listeners").size()).isEqualTo(1);
        assertThat(order.path("listeners").get(0).path("channel").path("name").asText())
            .isEqualTo("orders.1");
        assertThat(order.path("listeners").get(0).path("channel").path("type").asText())
            .isEqualTo("private");
        assertThat(order.path("listeners").get(0).path("events").get(0).asText())
            .isEqualTo("OrderUpdated");
    }

    @Test
    void omitsLivePropsWhenNull() throws Exception {
        var json = mapper.writeValueAsString(pageWithLiveProps(null));
        assertThat(json).doesNotContain("\"liveProps\"");
    }

    @Test
    void omitsLivePropsWhenEmpty() throws Exception {
        var page = pageWithLiveProps(Map.of());
        assertThat(page.liveProps()).isNull();
        assertThat(mapper.writeValueAsString(page)).doesNotContain("\"liveProps\"");
    }

    @Test
    void matchesOfficialWireStructure() throws Exception {
        var json = mapper.writeValueAsString(pageWithLiveProps(orderLiveProps()));
        assertThat(json).isEqualTo(
            "{\"component\":\"Orders/Show\","
                + "\"props\":{\"order\":{\"id\":1,\"status\":\"shipped\"}},"
                + "\"url\":\"/orders/1\","
                + "\"version\":\"c3ab87\","
                + "\"liveProps\":{\"order\":{"
                + "\"listeners\":[{\"channel\":{\"name\":\"orders.1\",\"type\":\"private\"},"
                + "\"events\":[\"OrderUpdated\"]}],"
                + "\"throttle\":1000}}}");
    }

    @Test
    void livePropsCountAsMetadataAndSurviveCopies() {
        var page = pageWithLiveProps(orderLiveProps());
        assertThat(page.hasMetadata()).isTrue();
        assertThat(page.withProps(Map.of("other", 1)).liveProps()).containsKey("order");
        assertThat(page.withVersion("v2").liveProps()).containsKey("order");
        assertThat(page.withUrl("/orders/2").liveProps()).containsKey("order");
        assertThat(page.withPreserveBigIntegers(Boolean.TRUE).liveProps()).containsKey("order");
        assertThat(page.withMergeMetadata(List.of("order"), List.of(), List.of(), List.of())
            .liveProps()).containsKey("order");
    }
}
