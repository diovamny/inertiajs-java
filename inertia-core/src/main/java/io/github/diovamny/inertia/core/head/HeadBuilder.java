package io.github.diovamny.inertia.core.head;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-request builder for server-rendered {@code <head>} tags (Rails
 * {@code MetaTagBuilder} parity).
 *
 * <p>Every tag carries a {@code headKey}; adding a tag with an existing key
 * replaces the previous one (smart deduplication):</p>
 * <ul>
 *   <li>{@code title("Home")} → key {@code title};</li>
 *   <li>{@code meta("description", ...)} → key {@code meta-description};</li>
 *   <li>{@code property("og:title", ...)} → key {@code og:title};</li>
 *   <li>{@code link("canonical", ...)} → key {@code link-canonical} (so
 *   {@code canonical(href)} replaces a previous canonical link).</li>
 * </ul>
 *
 * <p>Titles honor an optional {@code String.format}-style template
 * ({@code "%s | My App"}). Adapters expose one builder per request and, when
 * server head is enabled, publish {@link #toPropList()} as the
 * {@code head} page prop for the frontend {@code Head} component.</p>
 */
public class HeadBuilder {

    private final Map<String, MetaTag> tags = new LinkedHashMap<>();
    private String titleTemplate = "%s";

    /**
     * Set the title template applied by {@link #title(String)}.
     *
     * @param titleTemplate a {@code String.format} template containing
     *                      {@code %s}, e.g. {@code "%s | My App"}
     * @return this builder
     */
    public HeadBuilder titleTemplate(String titleTemplate) {
        if (titleTemplate != null && !titleTemplate.isBlank()) {
            this.titleTemplate = titleTemplate;
        }
        return this;
    }

    /**
     * Set the page title (template applied).
     */
    public HeadBuilder title(String title) {
        var text = title != null ? title : "";
        if (!"%s".equals(titleTemplate)) {
            try {
                text = String.format(titleTemplate, text);
            } catch (Exception e) {
                text = title != null ? title : "";
            }
        }
        tags.put("title", new MetaTag("title", "title", Map.of("text", text)));
        return this;
    }

    /**
     * Add {@code <meta name="..." content="...">}.
     */
    public HeadBuilder meta(String name, String content) {
        tags.put("meta-" + name,
            new MetaTag("meta-" + name, "meta",
                Map.of("name", name, "content", content != null ? content : "")));
        return this;
    }

    /**
     * Add {@code <meta property="..." content="...">} (OpenGraph & friends).
     */
    public HeadBuilder property(String property, String content) {
        tags.put(property,
            new MetaTag(property, "meta",
                Map.of("property", property, "content", content != null ? content : "")));
        return this;
    }

    /**
     * Add {@code <link rel="..." href="...">}.
     */
    public HeadBuilder link(String rel, String href) {
        tags.put("link-" + rel,
            new MetaTag("link-" + rel, "link",
                Map.of("rel", rel, "href", href != null ? href : "")));
        return this;
    }

    /**
     * Set the canonical URL (replaces any previous canonical link).
     */
    public HeadBuilder canonical(String href) {
        return link("canonical", href);
    }

    /**
     * Remove the tag with the given head key, if present.
     */
    public HeadBuilder remove(String headKey) {
        tags.remove(headKey);
        return this;
    }

    /**
     * Drop every tag.
     */
    public HeadBuilder clear() {
        tags.clear();
        return this;
    }

    /**
     * Whether any tag was collected.
     */
    public boolean isEmpty() {
        return tags.isEmpty();
    }

    /**
     * Snapshot of the collected tags in insertion order.
     */
    public List<MetaTag> tags() {
        return Collections.unmodifiableList(new ArrayList<>(tags.values()));
    }

    /**
     * The {@code head} page-prop payload: a list of
     * {@code {key, tag, attributes}} maps.
     */
    public List<Map<String, Object>> toPropList() {
        var result = new ArrayList<Map<String, Object>>();
        for (var tag : tags.values()) {
            var entry = new LinkedHashMap<String, Object>();
            entry.put("key", tag.headKey());
            entry.put("tag", tag.tag());
            entry.put("attributes", new LinkedHashMap<>(tag.attributes()));
            result.add(Collections.unmodifiableMap(entry));
        }
        return Collections.unmodifiableList(result);
    }

    /**
     * Render the collected tags as an HTML fragment (for SSR root templates
     * and app-owned layouts).
     */
    public String toHtml() {
        var out = new StringBuilder();
        for (var tag : tags.values()) {
            switch (tag.tag()) {
                case "title" -> out.append("<title>")
                    .append(escape(tag.attributes().getOrDefault("text", "")))
                    .append("</title>");
                case "meta" -> {
                    out.append("<meta");
                    appendAttr(out, tag, "name");
                    appendAttr(out, tag, "property");
                    appendAttr(out, tag, "content");
                    out.append(">");
                }
                case "link" -> {
                    out.append("<link");
                    appendAttr(out, tag, "rel");
                    appendAttr(out, tag, "href");
                    out.append(">");
                }
                default -> {
                    out.append("<").append(tag.tag());
                    tag.attributes().forEach((name, value) -> {
                        out.append(' ').append(name).append("=\"").append(escape(value)).append('"');
                    });
                    out.append(">");
                }
            }
        }
        return out.toString();
    }

    private static void appendAttr(StringBuilder out, MetaTag tag, String name) {
        var value = tag.attributes().get(name);
        if (value != null) {
            out.append(' ').append(name).append("=\"").append(escape(value)).append('"');
        }
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace("\"", "&quot;");
    }
}
