package io.github.diovamny.spring.inertia.api;

import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import io.github.diovamny.inertia.core.spi.FlashStore;

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
     * @param errors field-to-message map (one message per field, legacy)
     * @return this redirect for chaining
     */
    public InertiaRedirect withErrors(Map<String, String> errors) {
        flashStore.put("errors", errors);
        return this;
    }

    /**
     * Flash multiple messages per field (Inertia {@code withAllErrors} parity).
     * The wire shape is {@code Map<field, List<message>>} with order preserved.
     *
     * @param errors immutable multi-message bag
     * @return this redirect for chaining
     */
    public InertiaRedirect withValidationErrors(
            io.github.diovamny.inertia.core.model.ValidationErrors errors) {
        flashStore.put("errors", errors != null ? errors.toWireMap(true) : Map.of());
        return this;
    }

    /**
     * Flash multiple messages per field from a plain multimap.
     *
     * @param errors field-to-messages multimap
     * @return this redirect for chaining
     */
    public InertiaRedirect withErrorMessages(Map<String, ? extends java.util.Collection<String>> errors) {
        flashStore.put("errors",
            io.github.diovamny.inertia.core.model.ValidationErrors.ofLists(errors).toWireMap(true));
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

    /**
     * Alias of {@link #with(String, Object)}.
     */
    public InertiaRedirect flash(String key, Object value) {
        return with(key, value);
    }

    /**
     * Alias of {@link #with(Map)}.
     */
    public InertiaRedirect flash(Map<String, Object> values) {
        return with(values);
    }

    /**
     * Alias of {@link #with(String, Object)}.
     */
    public InertiaRedirect withFlash(String key, Object value) {
        return with(key, value);
    }

    /**
     * Alias of {@link #with(Map)}.
     */
    public InertiaRedirect withFlash(Map<String, Object> values) {
        return with(values);
    }
}
