package io.github.dg.spring.inertia.mvc;

import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.jspecify.annotations.NonNull;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.dg.spring.inertia.protocol.InertiaHeaderExtractor;
import io.github.dg.spring.inertia.protocol.SharedDataRegistry;
import io.github.dg.spring.inertia.spi.InertiaSharedDataContributor;

/**
 * Runs before every controller: extracts the Inertia protocol headers into
 * the request attributes and runs any registered {@link InertiaSharedDataContributor}
 * beans to populate global shared props.
 */
public class InertiaInterceptor implements HandlerInterceptor {

    private final InertiaHeaderExtractor headerExtractor;
    private final ObjectProvider<List<InertiaSharedDataContributor>> contributorsProvider;
    private final ObjectProvider<SharedDataRegistry> sharedDataRegistryProvider;

    public InertiaInterceptor(InertiaHeaderExtractor headerExtractor) {
        this(headerExtractor, null, null);
    }

    public InertiaInterceptor(
            InertiaHeaderExtractor headerExtractor,
            ObjectProvider<List<InertiaSharedDataContributor>> contributorsProvider,
            ObjectProvider<SharedDataRegistry> sharedDataRegistryProvider) {
        this.headerExtractor = headerExtractor;
        this.contributorsProvider = contributorsProvider;
        this.sharedDataRegistryProvider = sharedDataRegistryProvider;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        headerExtractor.extract(request);

        if (contributorsProvider != null && sharedDataRegistryProvider != null) {
            var contributors = contributorsProvider.getIfAvailable();
            if (contributors != null && !contributors.isEmpty()) {
                var registry = sharedDataRegistryProvider.getIfAvailable();
                if (registry != null) {
                    for (var contributor : contributors) {
                        var shared = contributor.contribute(request);
                        if (shared != null && !shared.isEmpty()) {
                            registry.setSharedProps(shared);
                        }
                    }
                }
            }
        }

        return true;
    }
}
