package io.github.diovamny.quarkus.inertia.api;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;

import io.github.diovamny.inertia.core.model.ValidationErrors;
import io.github.diovamny.inertia.core.spi.FlashStore;

class ValidationErrorsApiUnitTest {

    @Test
    void redirectEmitsOrderedArraysViaNewApis() {
        var flashStore = mock(FlashStore.class);
        var redirect = new InertiaRedirect(
            io.smallrye.mutiny.Uni.createFrom().item((Object) Response.ok().build()), flashStore);
        redirect.withValidationErrors(
            ValidationErrors.builder().add("email", "required").add("email", "must be valid").build());
        verify(flashStore).put("errors", Map.of("email", List.of("required", "must be valid")));
    }

    @Test
    void redirectEmitsArraysFromMultimap() {
        var flashStore = mock(FlashStore.class);
        var redirect = new InertiaRedirect(
            io.smallrye.mutiny.Uni.createFrom().item((Object) Response.ok().build()), flashStore);
        redirect.withErrorMessages(Map.of("name", List.of("a", "b")));
        verify(flashStore).put("errors", Map.of("name", List.of("a", "b")));
    }

    @Test
    void renderAndResponseEmitOrderedArraysViaNewApis() {
        var flashStore = mock(FlashStore.class);
        var render = new InertiaRender(
            io.smallrye.mutiny.Uni.createFrom().item((Object) Response.ok().build()),
            flashStore, null);
        render.withValidationErrors(ValidationErrors.single("email", "required"));
        verify(flashStore).put("errors", Map.of("email", List.of("required")));

        var flashStore2 = mock(FlashStore.class);
        var response = new InertiaResponse(Response.ok().build(), null, flashStore2);
        response.withErrorMessages(Map.of("email", List.of("required", "must be valid")));
        verify(flashStore2).put("errors", Map.of("email", List.of("required", "must be valid")));
    }
}
