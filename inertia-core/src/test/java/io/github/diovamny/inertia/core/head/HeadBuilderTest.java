package io.github.diovamny.inertia.core.head;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class HeadBuilderTest {

    @Test
    void titleUsesKeyTitle() {
        var builder = new HeadBuilder().title("Home");
        assertThat(builder.tags()).hasSize(1);
        assertThat(builder.tags().get(0).headKey()).isEqualTo("title");
    }

    @Test
    void duplicateTitleReplaces() {
        var builder = new HeadBuilder().title("One").title("Two");
        assertThat(builder.tags()).hasSize(1);
        assertThat(builder.toHtml()).contains("<title>Two</title>");
    }

    @Test
    void titleTemplateApplies() {
        var builder = new HeadBuilder().titleTemplate("%s | App").title("Home");
        assertThat(builder.toHtml()).contains("<title>Home | App</title>");
    }

    @Test
    void metaUsesMetaNameKey() {
        var builder = new HeadBuilder()
            .meta("description", "a")
            .meta("description", "b");
        assertThat(builder.tags()).hasSize(1);
        assertThat(builder.toHtml()).contains("<meta name=\"description\" content=\"b\">");
    }

    @Test
    void propertyUsesPropertyValueAsKey() {
        var builder = new HeadBuilder().property("og:title", "Hi");
        assertThat(builder.tags().get(0).headKey()).isEqualTo("og:title");
        assertThat(builder.toHtml())
            .contains("<meta property=\"og:title\" content=\"Hi\">");
    }

    @Test
    void canonicalReplacesLinkCanonical() {
        var builder = new HeadBuilder()
            .link("canonical", "https://a.example/")
            .canonical("https://b.example/");
        assertThat(builder.tags()).hasSize(1);
        assertThat(builder.toHtml()).contains("href=\"https://b.example/\"");
    }

    @Test
    void removeAndClear() {
        var builder = new HeadBuilder().title("T").meta("description", "d");
        builder.remove("title");
        assertThat(builder.tags()).hasSize(1);
        builder.clear();
        assertThat(builder.isEmpty()).isTrue();
        assertThat(builder.toHtml()).isEmpty();
        assertThat(builder.toPropList()).isEmpty();
    }

    @Test
    void propListShape() {
        var builder = new HeadBuilder().title("T").link("canonical", "https://x.example/");
        var props = builder.toPropList();
        assertThat(props).hasSize(2);
        assertThat(props.get(0)).containsEntry("key", "title").containsEntry("tag", "title");
        assertThat(props.get(1)).containsEntry("key", "link-canonical");
    }

    @Test
    void htmlEscapesAttributes() {
        var builder = new HeadBuilder().meta("description", "a\"b<c&d");
        assertThat(builder.toHtml()).contains("content=\"a&quot;b&lt;c&amp;d\"");
    }

    @Test
    void blankKeyRejected() {
        assertThatThrownBy(() -> new MetaTag(" ", "meta", null))
            .isInstanceOf(IllegalArgumentException.class);
    }
}
