package io.github.diovamny.spring.inertia.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * §3.17 adapter attack suite (Spring side): redirect targets fail closed
 * before any header is emitted, session-less visits never 500, and protocol
 * headers stay intact.
 */
@SpringBootTest(classes = TestApplication.class, properties = {
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class InertiaSecuritySuiteTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void javascriptRedirectIsRejectedWithoutLocation() throws Exception {
        mockMvc.perform(get("/redirect-to").param("url", "javascript:alert(1)"))
            .andExpect(status().isBadRequest())
            .andExpect(header().doesNotExist("Location"))
            .andExpect(header().doesNotExist("X-Inertia-Location"));
    }

    @Test
    void javascriptRedirectIsRejectedOnInertiaVisit() throws Exception {
        mockMvc.perform(get("/redirect-to")
                .param("url", "javascript:alert(1)")
                .header("X-Inertia", "true"))
            .andExpect(status().isBadRequest())
            .andExpect(header().doesNotExist("Location"))
            .andExpect(header().doesNotExist("X-Inertia-Location"));
    }

    @Test
    void crlfRedirectIsRejectedWithoutSplit() throws Exception {
        mockMvc.perform(get("/redirect-to").queryParam("url", "/ok\r\nX: 1"))
            .andExpect(status().isBadRequest())
            .andExpect(header().doesNotExist("Location"));
    }

    @Test
    void legitimateRedirectStillWorks() throws Exception {
        mockMvc.perform(get("/redirect-to").param("url", "/dashboard"))
            .andExpect(status().isFound())
            .andExpect(header().string("Location", "/dashboard"));
    }

    @Test
    void sessionlessInertiaVisitDoesNot500() throws Exception {
        mockMvc.perform(get("/dashboard").header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(header().string("Vary",
                org.hamcrest.Matchers.containsString("X-Inertia")));
    }
}
