package io.github.diovamny.spring.inertia.integration;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import io.github.diovamny.inertia.core.result.InertiaLocationResult;
import io.github.diovamny.inertia.core.result.InertiaPageResult;
import io.github.diovamny.inertia.core.result.InertiaRedirectResult;

/**
 * Plain {@code @Controller} (no {@code @ResponseBody} semantics) so typed
 * results reach {@code InertiaReturnValueHandler} instead of being
 * serialized as JSON by Spring's default body processor.
 */
@Controller
public class TypedResultController {

    @GetMapping("/typed-page")
    public InertiaPageResult typedPage() {
        return InertiaPageResult.of("TypedPage", Map.of("a", 1));
    }

    @GetMapping("/typed-redirect")
    public InertiaRedirectResult typedRedirect() {
        return InertiaRedirectResult.to("/flash");
    }

    @GetMapping("/typed-location")
    public InertiaLocationResult typedLocation() {
        return InertiaLocationResult.to("https://example.com");
    }
}
