package io.github.diovamny.inertia.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class LivePropMetadataTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void omitsThrottleWhenNotConfigured() throws Exception {
        var metadata = new LivePropMetadata(List.of(
            new LivePropListener(LiveChannel.privateChannel("orders.1"), "OrderUpdated")));
        var json = mapper.writeValueAsString(metadata);
        assertThat(json).isEqualTo(
            "{\"listeners\":[{\"channel\":{\"name\":\"orders.1\",\"type\":\"private\"},"
                + "\"events\":[\"OrderUpdated\"]}]}");
    }

    @Test
    void includesThrottleWhenConfigured() throws Exception {
        var metadata = new LivePropMetadata(
            List.of(new LivePropListener(LiveChannel.publicChannel("inventory"), "StockUpdated")),
            1000);
        var json = mapper.writeValueAsString(metadata);
        assertThat(json).contains("\"throttle\":1000");
    }

    @Test
    void explicitNullThrottleIsOmitted() throws Exception {
        var metadata = new LivePropMetadata(
            List.of(new LivePropListener(LiveChannel.presence("chat.room.1"), "UserJoined")),
            null);
        var json = mapper.writeValueAsString(metadata);
        assertThat(json).doesNotContain("throttle");
    }
}
