package io.github.diovamny.inertia.core.protocol;

import java.util.List;
import java.util.Set;

/**
 * Merge/reset label decisions (protocol: merging props, resetting props).
 *
 * <p>A reset prop is re-resolved and returned unlabeled so the client
 * replaces instead of merging: resetting a parent ({@code contacts}) also
 * prunes its dotted descendants ({@code contacts.data}). Scroll/once
 * resolution stays in the adapters (intent handling, TTL clocks and drain
 * semantics diverge); only the shared set operations live here.</p>
 */
public final class MergeLabels {

    private MergeLabels() {
    }

    /**
     * Parse the {@code X-Inertia-Reset} header into an ordered set.
     *
     * @param raw the raw header value, if any
     * @return the reset paths, empty when the header is absent
     */
    public static Set<String> resetSet(String raw) {
        return PartialFilter.parseCsv(raw);
    }

    /**
     * Drop reset keys from a merge metadata list. Never mutates the input:
     * callers pass a copy when they need an updated list.
     *
     * @param keys the labeled prop paths
     * @param reset the reset paths
     * @return the surviving labels, in order
     */
    public static List<String> pruneReset(List<String> keys, Set<String> reset) {
        if (reset == null || reset.isEmpty()) {
            return keys;
        }
        return keys.stream()
            .filter(key -> reset.stream()
                .noneMatch(item -> key.equals(item) || key.startsWith(item + ".")))
            .toList();
    }
}
