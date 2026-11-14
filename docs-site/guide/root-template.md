# Root Template

The root template is the HTML document served on the initial visit. It must
contain the mount point and the page object; on SSR visits it also receives the
rendered body and head.

The default is `index.html` in `src/main/resources/templates/`. Change it with
`inertia.root-template`.

## Placeholders

| Placeholder | Replaced with |
|---|---|
| `__INERTIA_ROOT__` | Complete root: mount point, page JSON script and SSR body |
| `__INERTIA_SSR_BODY__` | Server-rendered component HTML (empty on CSR) |
| `__INERTIA_SSR_HEAD__` | Server-rendered `<head>` tags (empty on CSR) |
| `__INERTIA_PAGE_JSON__` | Safe-encoded page object JSON |
| `__INERTIA_PAGE_TITLE__` | Page title (when provided by `viewData`/head) |
| `__INERTIA_CSP_NONCE__` | `nonce="..."` attribute when a `NonceProvider` is registered |
| `__INERTIA_PAGE__` | Legacy v1/v2 placeholder; always blanked in v3 |

`__INERTIA_ROOT__` is the recommended option: the adapter assembles the
v3-pure markup and handles the SSR/CSR branch for you. Use the explicit
placeholders when you need full control over the mount element.

::: warning v3-pure contract
`<div id="app">` must **not** carry a `data-page` attribute. The page object
travels only inside `<script type="application/json" data-page="app">`. The
legacy `__INERTIA_PAGE__` placeholder is blanked and there is no opt-in.
:::

## Spring Boot (Thymeleaf or static HTML)

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>__INERTIA_PAGE_TITLE__</title>
    __INERTIA_SSR_HEAD__
    <link rel="stylesheet" href="/assets/app.css" />
</head>
<body>
    <noscript>This application requires JavaScript.</noscript>
    __INERTIA_ROOT__
    <script type="module" src="/assets/app.js"></script>
</body>
</html>
```

If you prefer to spell out the mount point:

```html
<body>
    <div id="app">__INERTIA_SSR_BODY__</div>
    <script type="application/json" data-page="app" __INERTIA_CSP_NONCE__>__INERTIA_PAGE_JSON__</script>
    <script type="module" src="/assets/app.js"></script>
</body>
```

The same file works with Thymeleaf, but do not let Thymeleaf escape the JSON
placeholder. Either keep the file static (the adapter substitutes placeholders
after reading it) or use `th:utext` carefully. The starters keep it static.

## Quarkus (Qute)

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>{pageTitle ?: 'Hello Inertia'}</title>
    {#if ssrHead}{ssrHead.raw}{/if}
    <link rel="stylesheet" href="/assets/app.css" />
</head>
<body>
    <noscript>This application requires JavaScript.</noscript>
    {#if ssrBody}{ssrBody.raw}{#else}<div id="app"></div>
    <script type="application/json" data-page="app">{dataPage}</script>{/if}
    <script type="module" src="/assets/app.js"></script>
</body>
</html>
```

Enable Qute with `inertia.use-qute=true` and add the `quarkus-qute` extension.
Qute receives `pageTitle`, `ssrHead`, `ssrBody`, `dataPage`, `dataPageAttr`
and `cspNonce` as template data. The adapter wraps the JSON in a `RawString`,
so no escaping is applied.

## Static HTML variant

If you do not use a template engine at all:

```html
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>__INERTIA_PAGE_TITLE__</title>
    __INERTIA_SSR_HEAD__
</head>
<body>
    __INERTIA_ROOT__
    <script type="module" src="/assets/app.js"></script>
</body>
</html>
```

The adapter performs plain string substitution for every placeholder. Keys are
matched exactly and are case-sensitive (`__INERTIA_PAGE_JSON__`, not
`__inertia_page_json__`).

## CSR vs SSR

| Mode | `__INERTIA_SSR_BODY__` | `__INERTIA_SSR_HEAD__` | Client behavior |
|---|---|---|---|
| CSR | Empty | Empty | Mounts a new app and reads the page JSON |
| SSR success | Rendered HTML | Rendered head tags | Hydrates the existing markup |
| SSR failure | Empty | Empty | Automatically falls back to CSR |

SSR failures are never fatal: an unreachable, slow or failing sidecar degrades
to client rendering for that request. See [SSR](/guide/ssr).

## View data and titles

Root-template data that is not part of the page object uses
`inertia.viewData(key, value)` and `__VIEW_<KEY>__` placeholders. This is
different from `props`: view data is only available to the HTML template, not
to Vue, React or Svelte. See [View Data](/guide/viewdata).

```java
inertia.viewData("title", post.title() + " | My Blog");
inertia.viewData("description", post.excerpt());
return inertia.render("Blog/Post", Map.of("post", post));
```

```html
<title>__VIEW_TITLE__</title>
<meta name="description" content="__VIEW_DESCRIPTION__" />
```

For `<Head>` and the server-side `HeadBuilder`, see
[Title & Meta](/guide/title-and-meta).

## CSP nonces

The v3-pure bootstrap is data-only and needs no nonce by default. If you add
executable inline scripts, register a `NonceProvider`; Qute templates receive
`{cspNonce}` and static templates can use `__INERTIA_CSP_NONCE__`:

```html
<script type="module" __INERTIA_CSP_NONCE__ src="/assets/app.js"></script>
```

## Checklist

- [ ] `<div id="app">` has no `data-page` attribute.
- [ ] The page JSON is inside `<script type="application/json" data-page="app">`.
- [ ] `__INERTIA_PAGE__` is not used.
- [ ] The Vite entry points to the built asset.
- [ ] The template is on the classpath at `templates/<inertia.root-template>`.
- [ ] SSR placeholders exist only if SSR is enabled (the adapter tolerates them otherwise).
