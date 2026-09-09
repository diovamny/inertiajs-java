package io.github.diovamny.spring.inertia.protocol;

import java.util.List;
import java.util.Map;

/**
 * Merge metadata processor. In Inertia v3, the server only emits merge
 * metadata ({@code merge}, {@code prepend}, {@code deepMerge}, {@code matchOn})
 * and the client performs the actual merging. This class is a no-op that
 * exists for API compatibility.
 */
public class MergePropProcessor {

    /**
     * No-op: the server does not merge props; the client handles merging
     * based on the emitted metadata.
     *
     * @param props the current prop set
     * @return the unmodified prop set
     */
    public Map<String, Object> mergeProps(Map<String, Object> props, List<String> mergeProps,
            List<String> prependProps, List<String> deepMergeProps, List<String> matchPropsOn) {
        return props;
    }

    /**
     * No-op: the server does not store props for merging.
     *
     * @param props the props to persist (ignored)
     */
    public void propagateProps(Map<String, Object> props) {
        // No-op: client-side merging
    }

    /**
     * No-op: reset is handled by clearing metadata via {@code X-Inertia-Reset}.
     */
    public void reset() {
        // No-op: metadata reset handled client-side
    }
}
