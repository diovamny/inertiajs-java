package io.github.diovamny.quarkus.inertia.live;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Certifies live props over the Quarkus JAX-RS (RESTEasy Reactive) transport.
 */
@QuarkusTest
class LivePropsQuarkusTest extends LivePropsContract {

    @Override
    String basePath() {
        return "/live-props";
    }

    @Override
    void resetCounters() {
        LivePropsTestResource.ORDER_RESOLVED.set(0);
        LivePropsTestResource.DEFERRED_RESOLVED.set(0);
    }

    @Override
    int orderResolved() {
        return LivePropsTestResource.ORDER_RESOLVED.get();
    }

    @Override
    int deferredResolved() {
        return LivePropsTestResource.DEFERRED_RESOLVED.get();
    }
}
