package io.github.dg.quarkus.inertia.testing;

import java.util.List;

/**
 * Functional executor for performing Inertia reload requests in test suites.
 */
@FunctionalInterface
public interface InertiaReloadExecutor {

    /**
     * Execute an Inertia partial or full reload request.
     *
     * @param url       the target page URL
     * @param component the component name
     * @param version   the asset version
     * @param only      props to include (null or empty for all)
     * @param except    props to exclude (null or empty for none)
     * @return the resulting parsed InertiaPage
     */
    InertiaPage execute(String url, String component, String version, List<String> only, List<String> except);
}
