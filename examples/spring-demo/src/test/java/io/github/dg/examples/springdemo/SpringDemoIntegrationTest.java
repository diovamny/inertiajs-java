package io.github.dg.examples.springdemo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static io.github.dg.spring.inertia.testing.InertiaResultMatchers.inertia;

@SpringBootTest(classes = SpringDemoApplication.class, properties = {
    "inertia.version-custom=1.0.0",
    "inertia.csrf-enabled=false",
    "inertia.ssr-enabled=false"
})
@AutoConfigureMockMvc
class SpringDemoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rendersDashboardWithSharedDeferredAndOnceProps() throws Exception {
        mockMvc.perform(get("/")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "1.0.0"))
            .andExpect(status().isOk())
            .andExpect(inertia().component("Dashboard"))
            .andExpect(jsonPath("$.props.app.name").value("spring-demo"))
            .andExpect(jsonPath("$.props.stats.contacts").value(org.hamcrest.Matchers.greaterThanOrEqualTo(24)))
            .andExpect(jsonPath("$.props.welcome.title").exists())
            .andExpect(jsonPath("$.props.monthlyStats").doesNotExist());
    }

    @Test
    void loadsDeferredGroupOnPartialReload() throws Exception {
        mockMvc.perform(get("/")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "1.0.0")
                .header("X-Inertia-Partial-Component", "Dashboard")
                .header("X-Inertia-Partial-Data", "monthlyStats"))
            .andExpect(status().isOk())
            .andExpect(inertia().component("Dashboard"))
            .andExpect(jsonPath("$.props.monthlyStats.visits").value(1234))
            .andExpect(jsonPath("$.props.monthlyStats.growth").value(12.5));
    }

    @Test
    void listsContactsWithPaginationAndLiveSearch() throws Exception {
        mockMvc.perform(get("/contacts?search=ada")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "1.0.0"))
            .andExpect(status().isOk())
            .andExpect(inertia().component("Contacts/Index"))
            .andExpect(jsonPath("$.props.contacts", hasSize(1)))
            .andExpect(jsonPath("$.props.contacts[0].name").value("Ada Lovelace"))
            .andExpect(jsonPath("$.props.filters.search").value("ada"))
            .andExpect(jsonPath("$.props.pagination.total").value(1));

        mockMvc.perform(get("/contacts")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "1.0.0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.props.contacts", hasSize(10)))
            .andExpect(jsonPath("$.props.pagination.page").value(1))
            .andExpect(jsonPath("$.props.pagination.lastPage").value(3));
    }

    @Test
    void mergesPaginatedListsOnPartialReload() throws Exception {
        var session = new MockHttpSession();
        mockMvc.perform(get("/contacts?page=1")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "1.0.0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.props.contacts", hasSize(10)));

        mockMvc.perform(get("/contacts?page=2")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "1.0.0")
                .header("X-Inertia-Partial-Component", "Contacts/Index")
                .header("X-Inertia-Partial-Data", "contacts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.props.contacts", hasSize(20)));
    }

    @Test
    void createsContactAndFlashesSuccess() throws Exception {
        var session = new MockHttpSession();
        mockMvc.perform(post("/contacts")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "1.0.0")
                .param("name", "Linus Pauling")
                .param("email", "linus.pauling@example.com")
                .param("phone", "555-0200"))
            .andExpect(status().isSeeOther())
            .andExpect(jsonPath("$").doesNotExist())
            .andExpect(result -> {
                var location = result.getResponse().getHeader("Location");
                if (!"/contacts".equals(location)) {
                    throw new AssertionError("Expected Location /contacts but was " + location);
                }
            });

        mockMvc.perform(get("/contacts")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "1.0.0"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.props.success").value("Contacto creado correctamente."))
            .andExpect(jsonPath("$.props.pagination.total").value(25));
    }

    @Test
    void flashesValidationErrorsAndRedirectsBack() throws Exception {
        var session = new MockHttpSession();
        mockMvc.perform(post("/contacts")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "1.0.0")
                .header("Referer", "/contacts/create")
                .param("name", "")
                .param("email", "not-an-email")
                .param("phone", ""))
            .andExpect(status().isSeeOther())
            .andExpect(result -> {
                var location = result.getResponse().getHeader("Location");
                if (!"/contacts/create".equals(location)) {
                    throw new AssertionError("Expected Location /contacts/create but was " + location);
                }
            });

        mockMvc.perform(get("/contacts/create")
                .session(session)
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "1.0.0"))
            .andExpect(status().isOk())
            .andExpect(inertia().component("Contacts/Create"))
            .andExpect(jsonPath("$.props.errors.name").exists())
            .andExpect(jsonPath("$.props.errors.email").exists())
            .andExpect(jsonPath("$.props.errors.phone").exists());
    }

    @Test
    void precognitionReturns422WithFieldErrors() throws Exception {
        mockMvc.perform(post("/contacts")
                .header("X-Inertia", "true")
                .header("X-Inertia-Version", "1.0.0")
                .header("X-Inertia-Precognition", "true")
                .param("name", "")
                .param("email", "bad")
                .param("phone", ""))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.email").exists());
    }
}