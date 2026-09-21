package io.github.diovamny.inertia.core.testing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class PropPathsTest {

    private static final Map<String, Object> TREE = Map.of(
        "users", Map.of("total", 50, "data", List.of(Map.of("name", "Alice"))),
        "title", "t");

    @Test
    void navigatesDotsAndIndexes() {
        assertThat(PropPaths.navigate(TREE, "users.total")).isEqualTo(50);
        assertThat(PropPaths.navigate(TREE, "users.data[0].name")).isEqualTo("Alice");
        assertThat(PropPaths.navigate(TREE, "title")).isEqualTo("t");
    }

    @Test
    void navigateReturnsNullOnMissing() {
        assertThat(PropPaths.navigate(TREE, "users.missing")).isNull();
        assertThat(PropPaths.navigate(TREE, "users.data[9]")).isNull();
        assertThat(PropPaths.navigate(TREE, "users.data[x]")).isNull();
        assertThat(PropPaths.navigate(null, "a")).isNull();
        assertThat(PropPaths.navigate(TREE, "title.deeper")).isNull();
    }

    @Test
    void presentAndSizeOf() {
        assertThat(PropPaths.present(TREE, "users.data[0].name")).isTrue();
        assertThat(PropPaths.present(TREE, "users.nope")).isFalse();
        assertThat(PropPaths.sizeOf(TREE, "users.data")).isEqualTo(1);
        assertThat(PropPaths.sizeOf(TREE, "users")).isEqualTo(2);
        assertThat(PropPaths.sizeOf(TREE, "title")).isEqualTo(-1);
        assertThat(PropPaths.sizeOf(TREE, "missing")).isEqualTo(-1);
    }

    @Test
    void diffFindsDivergences() {
        var lines = PropPaths.diff(
            Map.of("a", 1, "nested", Map.of("x", "x", "gone", true), "list", List.of(1, 2)),
            Map.of("a", 2, "nested", Map.of("x", "x"), "list", List.of(1), "extra", "e"));
        assertThat(lines).anySatisfy(line ->
            assertThat(line).contains("a").contains("1").contains("2"));
        assertThat(lines).anySatisfy(line ->
            assertThat(line).contains("gone").contains("missing"));
        assertThat(lines).anySatisfy(line ->
            assertThat(line).contains("extra").contains("unexpected"));
        assertThat(lines).anySatisfy(line ->
            assertThat(line).contains("list").contains("size"));
    }

    @Test
    void diffEmptyWhenEqual() {
        assertThat(PropPaths.diff(TREE, TREE)).isEmpty();
        assertThat(PropPaths.diff(Map.of("a", 1), Map.of("a", 1))).isEmpty();
    }
}
