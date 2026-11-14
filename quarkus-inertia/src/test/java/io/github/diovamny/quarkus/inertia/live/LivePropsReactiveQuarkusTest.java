package io.github.diovamny.quarkus.inertia.live;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Certifies live props over the Quarkus Vert.x Reactive Routes transport.
 */
@QuarkusTest
class LivePropsReactiveQuarkusTest extends LivePropsContract {

    @Override
    String basePath() {
        return "/live-props-reactive";
    }

    @Override
    void resetCounters() {
        LivePropsReactiveRoutes.ORDER_RESOLVED.set(0);
        LivePropsReactiveRoutes.DEFERRED_RESOLVED.set(0);
    }

    @Override
    int orderResolved() {
        return LivePropsReactiveRoutes.ORDER_RESOLVED.get();
    }

    @Override
    int deferredResolved() {
        return LivePropsReactiveRoutes.DEFERRED_RESOLVED.get();
    }
}
