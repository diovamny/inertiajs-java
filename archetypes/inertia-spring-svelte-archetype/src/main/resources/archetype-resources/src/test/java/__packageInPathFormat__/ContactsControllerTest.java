package ${package};

import com.jayway.jsonpath.JsonPath;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end coverage of the starter CRUD through the real Inertia contract:
 * visits receive page objects, mutations answer 303 + Location, and
 * validation failures redirect back so the flashed errors and old input
 * appear on the next visit.
 *
 * <p>CSRF is disabled for these tests ({@code inertia.security.mode=disabled});
 * the token round-trip is covered by the adapter's own test suite.</p>
 */
@SpringBootTest(properties = "inertia.security.mode=disabled")
@AutoConfigureMockMvc
class ContactsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    void indexRendersTheHtmlShell() throws Exception {
        mockMvc.perform(get("/contacts"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("id=\"app\"")));
    }

    @Test
    void indexVisitReturnsTheContactsPageObject() throws Exception {
        mockMvc.perform(get("/contacts").header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Inertia", "true"))
            .andExpect(jsonPath("$.component").value("Contacts/Index"))
            .andExpect(jsonPath("$.props.contacts[0].name").value("Ada Lovelace"));
    }

    @Test
    void editVisitReturnsTheContactPageObject() throws Exception {
        mockMvc.perform(get("/contacts/1/edit").header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.component").value("Contacts/Edit"))
            .andExpect(jsonPath("$.props.contact.name").value("Ada Lovelace"));
    }

    @Test
    void storeCreatesContactAndFlashesSuccess() throws Exception {
        var session = new MockHttpSession();
        mockMvc.perform(post("/contacts")
                .session(session)
                .header("X-Inertia", "true")
                .param("name", "Linus Torvalds")
                .param("email", "linus@example.com")
                .param("phone", "+34 600 111 000"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/contacts"));

        mockMvc.perform(get("/contacts").session(session).header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("linus@example.com")))
            .andExpect(jsonPath("$.props.success").value("Contacto creado con éxito"));
    }

    @Test
    void storeWithInvalidDataRedirectsBackWithErrors() throws Exception {
        var session = new MockHttpSession();
        mockMvc.perform(post("/contacts")
                .session(session)
                .header("X-Inertia", "true")
                .header("Referer", "/contacts/create")
                .param("name", " ")
                .param("email", "not-an-email")
                .param("phone", "12345678901234567890123"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/contacts/create"));

        mockMvc.perform(get("/contacts/create").session(session).header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.props.errors.name").value("El nombre es obligatorio"))
            .andExpect(jsonPath("$.props.errors.email").value("El email no es válido"))
            .andExpect(jsonPath("$.props.errors.phone")
                .value("El teléfono no puede superar los 20 caracteres"))
            .andExpect(jsonPath("$.props.old.email").value("not-an-email"));
    }

    @Test
    void updateAndDeleteCompleteTheCrudCycle() throws Exception {
        var session = new MockHttpSession();
        mockMvc.perform(post("/contacts")
                .session(session)
                .header("X-Inertia", "true")
                .param("name", "Grace Field")
                .param("email", "grace.field@example.com")
                .param("phone", ""))
            .andExpect(status().isSeeOther());

        var id = idOf(session, "grace.field@example.com");

        mockMvc.perform(put("/contacts/" + id)
                .session(session)
                .header("X-Inertia", "true")
                .param("name", "Grace Fielding")
                .param("email", "grace.field@example.com")
                .param("phone", "+34 600 222 111"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/contacts"));

        mockMvc.perform(get("/contacts").session(session).header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("Grace Fielding")))
            .andExpect(jsonPath("$.props.success").value("Contacto actualizado con éxito"));

        mockMvc.perform(delete("/contacts/" + id)
                .session(session)
                .header("X-Inertia", "true"))
            .andExpect(status().isSeeOther())
            .andExpect(header().string("Location", "/contacts"));

        mockMvc.perform(get("/contacts").session(session).header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(content().string(not(containsString("Grace Fielding"))))
            .andExpect(jsonPath("$.props.success").value("Contacto eliminado con éxito"));
    }

    private long idOf(MockHttpSession session, String email) throws Exception {
        var body = mockMvc.perform(get("/contacts").session(session).header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();
        List<Integer> ids = JsonPath.read(
            body, "$.props.contacts[?(@.email == '" + email + "')].id");
        return ids.get(0).longValue();
    }
}
