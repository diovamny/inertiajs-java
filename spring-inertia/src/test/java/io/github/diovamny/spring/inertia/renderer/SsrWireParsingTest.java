package io.github.diovamny.spring.inertia.renderer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

/**
 * The Node sidecar answers {@code {"head":[...],"body":"..."}}; the adapter
 * must surface both parts (a silent {@code null} body renders an empty shell
 * with no error, which is exactly what this guards against).
 */
class SsrWireParsingTest {

    private final JsonMapper mapper = JsonMapper.builder().build();

    @Test
    void parsesSidecarHeadAndBody() throws Exception {
        var json = """
            {"head":["<title>Hello</title>"],
             "body":"<div data-server-rendered=\\"true\\">Hi</div>"}""";
        var result = mapper.readValue(json, SsrClient.SsrResult.class);
        assertThat(result.head()).containsExactly("<title>Hello</title>");
        assertThat(result.body()).isEqualTo("<div data-server-rendered=\"true\">Hi</div>");
    }

    @Test
    void emptyHeadArrayParsesToEmptyList() throws Exception {
        var json = """
            {"head":[],"body":"<div>x</div>"}""";
        var result = mapper.readValue(json, SsrClient.SsrResult.class);
        assertThat(result.head()).isEmpty();
        assertThat(result.body()).isEqualTo("<div>x</div>");
        assertThat(result.headHtml()).isEmpty();
    }
}
