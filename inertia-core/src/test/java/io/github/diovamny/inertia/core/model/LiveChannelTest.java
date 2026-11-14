package io.github.diovamny.inertia.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

class LiveChannelTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializesPublicChannel() throws Exception {
        var json = mapper.writeValueAsString(LiveChannel.publicChannel("orders.1"));
        assertThat(json).isEqualTo("{\"name\":\"orders.1\",\"type\":\"public\"}");
    }

    @Test
    void serializesPrivateChannel() throws Exception {
        var json = mapper.writeValueAsString(LiveChannel.privateChannel("orders.1"));
        assertThat(json).isEqualTo("{\"name\":\"orders.1\",\"type\":\"private\"}");
    }

    @Test
    void serializesPresenceChannel() throws Exception {
        var json = mapper.writeValueAsString(LiveChannel.presence("chat.room.1"));
        assertThat(json).isEqualTo("{\"name\":\"chat.room.1\",\"type\":\"presence\"}");
    }

    @Test
    void omitsNullFields() throws Exception {
        var json = mapper.writeValueAsString(new LiveChannel("orders.1", null));
        assertThat(json).isEqualTo("{\"name\":\"orders.1\"}");
    }
}
