package io.github.dg.spring.inertia.api;

import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import io.github.dg.spring.inertia.spi.FlashStore;

/**
 * An Inertia redirect response. Extends {@link ResponseEntity} so Spring MVC
 * serves it natively; the fluent {@code with(...)} methods write flash data
 * that is injected into the page rendered by the redirect target.
 */
public class InertiaRedirect extends ResponseEntity<String> {

    private final FlashStore flashStore;

    /**
     * @param status     the HTTP status (302, 303, 409, ...)
     * @param headers    the response headers
     * @param body       the body; usually {@code null}
     * @param flashStore the flash storage written by {@code with(...)}
     */
    public InertiaRedirect(HttpStatusCode status, HttpHeaders headers, String body, FlashStore flashStore) {
        super(body, headers, status);
        this.flashStore = flashStore;
    }

    /**
     * Flash a value into the redirect target page.
     *
     * @param key   the flash key
     * @param value the value
     * @return this redirect for chaining
     */
    public InertiaRedirect with(String key, Object value) {
        flashStore.put(key, value);
        return this;
    }

    /**
     * Flash several values into the redirect target page.
     *
     * @param values the entries
     * @return this redirect for chaining
     */
    public InertiaRedirect with(Map<String, Object> values) {
        flashStore.putAll(values);
        return this;
    }

    /**
     * Flash validation errors under the {@code errors} key.
     *
     * @param errors field-to-message map
     * @return this redirect for chaining
     */
    public InertiaRedirect withErrors(Map<String, String> errors) {
        flashStore.put("errors", errors);
        return this;
    }

    /**
     * Flash the submitted input under the {@code old} key.
     *
     * @param input the submitted values
     * @return this redirect for chaining
     */
    public InertiaRedirect withInput(Map<String, Object> input) {
        flashStore.put("old", input);
        return this;
    }
}
