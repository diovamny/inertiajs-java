package io.github.diovamny.inertia.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MergeableBuilderUnitTest {

    @Test
    void valueRegistersPlanAndReturnsValue() {
        var posts = Map.of("data", List.of(Map.of("id", 1)));
        var registered = new ArrayList<MergePlan>();
        var builder = MergeableBuilder.of("posts", posts, registered::add);
        var value = builder.append("data").prepend("pinned").matchOn("data.id").value();
        assertThat(value).isSameAs(posts);
        assertThat(registered).hasSize(1);
        assertThat(registered.get(0).qualifiedMergePaths()).containsExactly("posts.data");
        assertThat(registered.get(0).qualifiedPrependPaths()).containsExactly("posts.pinned");
        assertThat(registered.get(0).qualifiedMatchPaths()).containsExactly("posts.data.id");
        assertThat(builder.isRegistered()).isTrue();
    }

    @Test
    void planDoesNotRegister() {
        var registered = new ArrayList<MergePlan>();
        var builder = MergeableBuilder.of("posts", Map.of(), registered::add);
        builder.append("data").plan();
        assertThat(registered).isEmpty();
        assertThat(builder.isRegistered()).isFalse();
    }
}
