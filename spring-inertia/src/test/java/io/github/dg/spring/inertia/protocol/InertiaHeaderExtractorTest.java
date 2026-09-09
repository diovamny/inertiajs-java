package io.github.dg.spring.inertia.protocol;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InertiaHeaderExtractorTest {

    private MockHttpServletRequest request;
    private final InertiaHeaderExtractor extractor = new InertiaHeaderExtractor();

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, new MockHttpServletResponse()));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void extractsInertiaVisit() {
        request.setMethod("GET");
        request.setRequestURI("/users");
        request.setQueryString("page=2");
        request.addHeader("X-Inertia", "true");
        request.addHeader("X-Inertia-Version", "abc");
        request.addHeader("X-Inertia-Partial-Component", "Users");
        request.addHeader("X-Inertia-Partial-Data", "list,count");
        request.addHeader("X-Inertia-Partial-Except", "sidebar");
        request.addHeader("X-Inertia-Partial-Reset", "true");
        request.addHeader("X-Inertia-Precognition", "true");
        request.addHeader("X-Inertia-Precognition-Validate-Fields", "email");
        request.addHeader("X-Inertia-Error-Bag", "login");

        extractor.extract(request);

        assertEquals("GET", request.getAttribute(InertiaHeaderExtractor.CONTEXT_METHOD));
        assertEquals("/users?page=2", request.getAttribute(InertiaHeaderExtractor.CONTEXT_URI));
        assertEquals("true", request.getAttribute(InertiaHeaderExtractor.CONTEXT_INERTIA));
        assertEquals("abc", request.getAttribute(InertiaHeaderExtractor.CONTEXT_VERSION));
        assertEquals("Users", request.getAttribute(InertiaHeaderExtractor.CONTEXT_PARTIAL_COMPONENT));
        assertEquals("list,count", request.getAttribute(InertiaHeaderExtractor.CONTEXT_PARTIAL_DATA));
        assertEquals("sidebar", request.getAttribute(InertiaHeaderExtractor.CONTEXT_PARTIAL_EXCEPT));
        assertEquals("true", request.getAttribute(InertiaHeaderExtractor.CONTEXT_PARTIAL_RESET));
        assertEquals("true", request.getAttribute(InertiaHeaderExtractor.CONTEXT_PRECOGNITION));
        assertEquals("email", request.getAttribute(InertiaHeaderExtractor.CONTEXT_PRECOGNITION_VALIDATE_FIELDS));
        assertEquals("login", request.getAttribute(InertiaHeaderExtractor.CONTEXT_ERROR_BAG));
    }

    @Test
    void extractsOncePerRequest() {
        request.addHeader("X-Inertia", "true");
        extractor.extract(request);
        extractor.extract(request);
        assertTrue(Boolean.TRUE.equals(request.getAttribute(InertiaHeaderExtractor.CONTEXT_HEADERS_EXTRACTED)));
    }

    @Test
    void extractsLaravelPrecognitionHeaderAliases() {
        request.setMethod("POST");
        request.addHeader("Precognition", "true");
        request.addHeader("Precognition-Validate-Only", "email,name");

        extractor.extract(request);

        assertEquals("true", request.getAttribute(InertiaHeaderExtractor.CONTEXT_PRECOGNITION));
        assertEquals("email,name", request.getAttribute(InertiaHeaderExtractor.CONTEXT_PRECOGNITION_VALIDATE_FIELDS));
    }

    @Test
    void plainVisitHasNoInertiaAttribute() {
        extractor.extract(request);
        assertFalse(request.getAttribute(InertiaHeaderExtractor.CONTEXT_INERTIA) != null);
    }

    @Test
    void uriWithoutQuery() {
        request.setRequestURI("/home");
        extractor.extract(request);
        assertEquals("/home", request.getAttribute(InertiaHeaderExtractor.CONTEXT_URI));
    }
}
