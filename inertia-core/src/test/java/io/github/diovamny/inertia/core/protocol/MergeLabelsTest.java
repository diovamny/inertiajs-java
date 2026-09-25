package io.github.diovamny.inertia.core.protocol;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

class MergeLabelsTest {

    @Test
    void resetSetParsesHeader() {
        assertThat(MergeLabels.resetSet("entries, users.data")).containsExactly("entries", "users.data");
        assertThat(MergeLabels.resetSet(null)).isEmpty();
        assertThat(MergeLabels.resetSet("  ")).isEmpty();
    }

    @Test
    void pruneResetDropsKeysAndDottedDescendants() {
        var pruned = MergeLabels.pruneReset(
            new ArrayList<>(Arrays.asList("entries", "contacts", "contacts.data", "users")),
            Set.of("contacts"));
        assertThat(pruned).containsExactly("entries", "users");
    }

    @Test
    void pruneResetKeepsInputUntouched() {
        var keys = new ArrayList<>(List.of("a", "b"));
        var pruned = MergeLabels.pruneReset(keys, Set.of());
        assertThat(pruned).containsExactly("a", "b");
        assertThat(keys).containsExactly("a", "b");
    }

    @Test
    void pruneResetExactKeyOnly() {
        var pruned = MergeLabels.pruneReset(
            new ArrayList<>(Arrays.asList("entries", "entriesArchive")),
            Set.of("entries"));
        assertThat(pruned).containsExactly("entriesArchive");
    }
}
