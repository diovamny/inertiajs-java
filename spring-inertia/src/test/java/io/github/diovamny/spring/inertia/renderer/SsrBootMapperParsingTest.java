package io.github.diovamny.spring.inertia.renderer;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.diovamny.spring.inertia.integration.TestApplication;

/**
 * The Boot-configured {@code tools.jackson} mapper (not a vanilla one) must
 * also surface the sidecar {@code body}, otherwise SSR renders an empty shell
 * with no error.
 */
@SpringBootTest(classes = TestApplication.class, properties = {"inertia.ssr-enabled=false"})
class SsrBootMapperParsingTest {

    @Autowired
    private tools.jackson.databind.ObjectMapper mapper;

    @Test
    void bootMapperParsesSidecarBody() throws Exception {
        var json = """
            {"head":[],"body":"<div>x</div>"}""";
        var result = mapper.readValue(json, SsrClient.SsrResult.class);
        assertThat(result.head()).isEmpty();
        assertThat(result.body()).isEqualTo("<div>x</div>");
    }
}
