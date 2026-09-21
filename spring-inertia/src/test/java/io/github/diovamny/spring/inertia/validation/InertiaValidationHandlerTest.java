package io.github.diovamny.spring.inertia.validation;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import io.github.diovamny.spring.inertia.internal.ErrorResponseFactory;

class InertiaValidationHandlerTest {

    @Test
    void securityAccessDeniedPropagatesUntouched() {
        var handler = new InertiaValidationHandler(mock(ErrorResponseFactory.class));
        var denied = new org.springframework.security.access.AccessDeniedException("denied");
        var thrown = assertThrows(org.springframework.security.access.AccessDeniedException.class,
            () -> handler.handleOther(denied));
        assertSame(denied, thrown);
    }

    @Test
    void securitySubclassesPropagateByHierarchyWalk() {
        var handler = new InertiaValidationHandler(mock(ErrorResponseFactory.class));
        var denied = new AuthorizationDeniedStub("denied");
        assertTrue(InertiaValidationHandler.isSpringSecurityException(denied));
        var thrown = assertThrows(AuthorizationDeniedStub.class,
            () -> handler.handleOther(denied));
        assertSame(denied, thrown);
    }

    static class AuthorizationDeniedStub
            extends org.springframework.security.access.AccessDeniedException {
        AuthorizationDeniedStub(String message) {
            super(message);
        }
    }
}
