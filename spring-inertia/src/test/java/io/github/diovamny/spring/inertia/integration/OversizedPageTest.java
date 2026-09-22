package io.github.diovamny.spring.inertia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * §3.17 scenario 11 (Spring side): pages beyond
 * {@code inertia.max-page-bytes} fail closed with {@code 413}.
 */
@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.max-page-bytes=1024",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class OversizedPageTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void oversizedJsonVisitIs413() throws Exception {
        mockMvc.perform(get("/big-page").header("X-Inertia", "true"))
            .andExpect(status().isPayloadTooLarge())
            .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
    }

    @Test
    void oversizedHtmlVisitIs413() throws Exception {
        mockMvc.perform(get("/big-page"))
            .andExpect(status().isPayloadTooLarge());
    }

    @Test
    void smallPageStillPassesUnderCap() throws Exception {
        mockMvc.perform(get("/dashboard").header("X-Inertia", "true"))
            .andExpect(status().isOk());
    }
}
