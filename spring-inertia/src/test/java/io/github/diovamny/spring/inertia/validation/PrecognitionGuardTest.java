package io.github.diovamny.spring.inertia.validation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class PrecognitionGuardTest {

    @AfterEach
    void reset() {
        RequestContextHolder.resetRequestAttributes();
    }

    private void request(String precognition, String validateOnly, String validateOnlyAlias) {
        var servlet = new MockHttpServletRequest();
        if (precognition != null) {
            servlet.addHeader("Precognition", precognition);
        }
        if (validateOnly != null) {
            servlet.addHeader("Precognition-Validate-Only", validateOnly);
        }
        if (validateOnlyAlias != null) {
            servlet.addHeader("X-Inertia-Precognition-Validate-Fields", validateOnlyAlias);
        }
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(servlet));
    }

    @Test
    void outsideRequestAllowsMutations() {
        assertThat(PrecognitionGuard.isPrecognitionRequest()).isFalse();
        assertThat(PrecognitionGuard.isValidateOnlyRequest()).isFalse();
        assertThatNoException().isThrownBy(PrecognitionGuard::assertMutationsAllowed);
    }

    @Test
    void plainRequestAllowsMutations() {
        request(null, null, null);
        assertThat(PrecognitionGuard.isPrecognitionRequest()).isFalse();
        assertThatNoException().isThrownBy(PrecognitionGuard::assertMutationsAllowed);
    }

    @Test
    void precognitionWithoutValidateOnlyAllowsMutations() {
        request("true", null, null);
        assertThat(PrecognitionGuard.isPrecognitionRequest()).isTrue();
        assertThat(PrecognitionGuard.isValidateOnlyRequest()).isFalse();
        assertThatNoException().isThrownBy(PrecognitionGuard::assertMutationsAllowed);
    }

    @Test
    void validateOnlyBlocksMutations() {
        request("true", "name,email", null);
        assertThat(PrecognitionGuard.isValidateOnlyRequest()).isTrue();
        assertThatThrownBy(PrecognitionGuard::assertMutationsAllowed)
            .isInstanceOf(PrecognitionWriteBlockedException.class);
    }

    @Test
    void validateOnlyAliasBlocksMutations() {
        request(null, null, "name");
        assertThat(PrecognitionGuard.isValidateOnlyRequest()).isTrue();
        assertThatThrownBy(PrecognitionGuard::assertMutationsAllowed)
            .isInstanceOf(PrecognitionWriteBlockedException.class);
    }
}
