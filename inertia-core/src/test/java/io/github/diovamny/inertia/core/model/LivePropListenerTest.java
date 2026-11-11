package io.github.diovamny.inertia.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class LivePropListenerTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializesChannelAndSingleEvent() throws Exception {
        var listener = new LivePropListener(LiveChannel.privateChannel("orders.1"), "OrderUpdated");
        var json = mapper.writeValueAsString(listener);
        assertThat(json).isEqualTo(
            "{\"channel\":{\"name\":\"orders.1\",\"type\":\"private\"},"
                + "\"events\":[\"OrderUpdated\"]}");
    }

    @Test
    void serializesMultipleEventsInOrder() throws Exception {
        var listener = new LivePropListener(
            LiveChannel.publicChannel("inventory"), "StockUpdated", "StockDepleted");
        var json = mapper.writeValueAsString(listener);
        assertThat(json).isEqualTo(
            "{\"channel\":{\"name\":\"inventory\",\"type\":\"public\"},"
                + "\"events\":[\"StockUpdated\",\"StockDepleted\"]}");
    }

    @Test
    void listConstructorKeepsEvents() {
        var listener = new LivePropListener(
            LiveChannel.presence("chat.room.1"), List.of("UserJoined", "UserLeft"));
        assertThat(listener.events()).containsExactly("UserJoined", "UserLeft");
    }
}
