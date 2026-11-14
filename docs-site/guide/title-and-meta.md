# Title & Meta

Titles and meta tags can be managed on the client with `<Head>` or on the
server with the shared `HeadBuilder`. Both produce the same result; choose one
per application to avoid duplicate tags.

## Client-side `<Head>`

::: code-group

```vue [Vue 3]
<script setup lang="ts">
import { Head } from '@inertiajs/vue3'

defineProps<{ post: { title: string; excerpt: string } }>()
</script>

<template>
  <Head :title="post.title">
    <meta name="description" :content="post.excerpt" />
    <meta property="og:title" :content="post.title" />
    <link rel="canonical" :href="`https://app.example/blog/${post.slug}`" />
  </Head>
  <article>{{ post.excerpt }}</article>
</template>
```

```tsx [React 19]
import { Head } from '@inertiajs/react'

export default function Post({ post }: { post: Post }) {
  return (
    <>
      <Head title={post.title}>
        <meta name="description" content={post.excerpt} />
        <meta property="og:title" content={post.title} />
        <link
          rel="canonical"
          href={`https://app.example/blog/${post.slug}`}
        />
      </Head>
      <article>{post.excerpt}</article>
    </>
  )
}
```

```svelte [Svelte 5]
<script lang="ts">
  import { Head } from '@inertiajs/svelte'
  let { post } = $props()
</script>

<Head title={post.title}>
  <meta name="description" content={post.excerpt} />
  <meta property="og:title" content={post.title} />
  <link rel="canonical" href={`https://app.example/blog/${post.slug}`} />
</Head>

<article>{post.excerpt}</article>
```

:::

The client updates `document.title` and the matching tags after every
navigation. Tags are scoped to the component that declares them: when the
component unmounts, its tags are removed.

## Server-side `HeadBuilder`

The server builder is useful for SSR, for pages rendered without JavaScript and
for metadata that must be computed from backend data:

```java
@GetMapping("/dashboard")
public Object dashboard() {
    inertia.head()
        .title("Dashboard")
        .meta("description", "Your CRM dashboard")
        .property("og:title", "Dashboard")
        .property("og:type", "website")
        .link("canonical", "https://app.example/dashboard")
        .canonical("https://app.example/dashboard");

    return inertia.render("Dashboard", Map.of("stats", stats));
}
```

| Method | Produces | Key |
|---|---|---|
| `title(value)` | `<title>value</title>` | `title` |
| `meta(name, content)` | `<meta name="..." content="...">` | `meta-<name>` |
| `property(property, content)` | `<meta property="..." content="...">` | `og:<property>` |
| `link(rel, href)` | `<link rel="..." href="...">` | `link-<rel>` |
| `canonical(url)` | `<link rel="canonical" href="...">` | `link-canonical` |
| `remove(headKey)` | Removes one tag | — |
| `clear()` | Removes all tags | — |

Adding a tag with an existing key **replaces** the previous one, so a title,
description or canonical link never appears twice even if a layout and a page
both set it.

### Title templates

```properties
inertia.meta-title-template=%s | My App
```

`title("Dashboard")` then renders `Dashboard | My App`. The default is `%s`,
which keeps the title as-is.

### Publishing to the client

```properties
inertia.server-head=true
```

When enabled, the collected tags are published as the `head` page prop: a list
of `{key, tag, attributes}` maps that the client `<Head>` component can render.
An explicit `head` prop set by the application is never overwritten.

For SSR or app-owned layouts, use the builder directly:

```java
String html = inertia.head()
    .title("Dashboard")
    .meta("description", "CRM")
    .toHtml();

List<Map<String, Object>> props = inertia.head()
    .title("Dashboard")
    .toPropList();
```

`toHtml()` renders a fragment suitable for `__INERTIA_SSR_HEAD__`;
`toPropList()` returns the same data as the page prop.

## Choosing a strategy

| Strategy | Best for | Notes |
|---|---|---|
| Client `<Head>` | SPA navigation, dynamic UI titles | No server round trip; works with CSR |
| `HeadBuilder` + `server-head=true` | SEO, SSR, server-computed metadata | Tags travel as the `head` prop |
| `viewData` placeholders | Static root-template metadata | Not available to components |

Do not use `viewData` and `<Head>` for the same tag: the client will overwrite
the server value after hydration. Use one owner per tag.

## CSP nonces

The v3-pure bootstrap needs no nonce by default. If you add executable inline
scripts, register a `NonceProvider`:

```java
@Component
public class CspNonceProvider implements NonceProvider {
    @Override
    public String currentNonce() {
        return UUID.randomUUID().toString();
    }
}
```

Qute templates receive `{cspNonce}`; static templates can use
`__INERTIA_CSP_NONCE__`. The nonce is also available for SSR head tags.

## SEO checklist

- [ ] One `<title>` per page, with a stable suffix from `meta-title-template`.
- [ ] A unique `description` per page.
- [ ] Open Graph tags (`og:title`, `og:description`, `og:image`) on shareable pages.
- [ ] A canonical URL when the same content is reachable from several paths.
- [ ] SSR enabled if crawlers must see the content without executing JavaScript.
- [ ] `robots` and sitemap handled by the application, not by Inertia.

Next: [View Data](/guide/viewdata) · [SSR](/guide/ssr) ·
[Root Template](/guide/root-template)
