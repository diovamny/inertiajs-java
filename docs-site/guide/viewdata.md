# View Data

`viewData` injects values directly into the root HTML template, **outside** the
Inertia page object. It is intended for server-rendered HTML that does not need
to reach Vue, React or Svelte: page titles, meta descriptions, Open Graph tags,
canonical URLs or a server-computed theme class.

::: info Not props
View data is not serialized into the page JSON and is not available in your
components. If the frontend needs a value, use a prop or shared data.
:::

## How it works

1. The controller calls `inertia.viewData(key, value)`.
2. The adapter stores the values in the request context.
3. When rendering the root template, the adapter substitutes
   `__VIEW_<KEY>__` placeholders or exposes the values as Qute variables.

## Spring Boot

```java
@GetMapping("/blog/{slug}")
public Object blogPost(@PathVariable String slug) {
    Post post = postService.findBySlug(slug);
    inertia.viewData("title", post.title());
    inertia.viewData("description", post.excerpt());
    return inertia.render("Blog/Post", Map.of("post", post));
}
```

### Thymeleaf

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:text="${viewData['title']} ?: 'My App'">My App</title>
    <meta name="description" th:content="${viewData['description']}" />
</head>
<body>
    <div id="app"></div>
    <script type="application/json" data-page="app" th:utext="${pageJson}"></script>
    <script type="module" src="/assets/app.js"></script>
</body>
</html>
```

### Static placeholders

```html
<!DOCTYPE html>
<html>
<head>
    <title>__VIEW_TITLE__</title>
    <meta name="description" content="__VIEW_DESCRIPTION__" />
</head>
<body>
    __INERTIA_ROOT__
</body>
</html>
```

The adapter replaces `__VIEW_TITLE__` and `__VIEW_DESCRIPTION__` with the
values from `viewData("title", ...)` and `viewData("description", ...)`.

## Quarkus

```java
@GET
@Path("/blog/{slug}")
public Response blogPost(@PathParam("slug") String slug) {
    Post post = postService.findBySlug(slug);
    inertia.viewData("title", post.title());
    inertia.viewData("description", post.excerpt());
    return inertia.render("Blog/Post", Map.of("post", post));
}
```

### Qute

```html
<!DOCTYPE html>
<html>
<head>
    <title>{title ?: 'My App'}</title>
    <meta name="description" content="{description}" />
</head>
<body>
    {#if ssrBody}{ssrBody.raw}{#else}<div id="app"></div>
    <script type="application/json" data-page="app">{dataPage}</script>{/if}
    <script type="module" src="/assets/app.js"></script>
</body>
</html>
```

Enable Qute with `inertia.use-qute=true` and add the `quarkus-qute` extension.
View-data keys become Qute variables (`title`, `description`, ...).

### Static placeholders in Quarkus

The same `__VIEW_<KEY>__` syntax works with a plain `.html` template:

```html
<title>__VIEW_TITLE__</title>
<meta name="description" content="__VIEW_DESCRIPTION__" />
```

## API

| Method | Description |
|---|---|
| `viewData(key, value)` | Stores one key/value for the root template |
| `viewData(Map<String, Object>)` | Stores several key/values at once |

Values are scoped to the current request. They are not shared across requests
and are never written to the session.

## Placeholder format

- Prefix and suffix: double underscore (`__`).
- Middle: `VIEW_` followed by the key in **UPPER_SNAKE_CASE**.
- Matching is case-insensitive for the placeholder key.

| Call | Placeholder |
|---|---|
| `viewData("title", ...)` | `__VIEW_TITLE__` |
| `viewData("og_image", ...)` | `__VIEW_OG_IMAGE__` |
| `viewData("pageDescription", ...)` | `__VIEW_PAGEDESCRIPTION__` |

## Full SEO example

```java
@GetMapping("/blog/{slug}")
public Object post(@PathVariable String slug) {
    var post = posts.findBySlug(slug);
    inertia.viewData(Map.of(
        "title", post.title() + " | My Blog",
        "og_title", post.title(),
        "og_description", post.excerpt(),
        "og_image", post.coverImageUrl(),
        "canonical", "https://blog.example/posts/" + post.slug()
    ));
    return inertia.render("Blog/Post", Map.of("post", post));
}
```

```html
<head>
    <meta charset="utf-8" />
    <title>__VIEW_TITLE__</title>
    <meta name="description" content="__VIEW_OG_DESCRIPTION__" />
    <meta property="og:title" content="__VIEW_OG_TITLE__" />
    <meta property="og:description" content="__VIEW_OG_DESCRIPTION__" />
    <meta property="og:image" content="__VIEW_OG_IMAGE__" />
    <link rel="canonical" href="__VIEW_CANONICAL__" />
    __INERTIA_SSR_HEAD__
</head>
```

## View data vs HeadBuilder vs props

| Mechanism | Available to components | Rendered by | Best for |
|---|---|---|---|
| `viewData` | No | Root template only | Static HTML metadata |
| `HeadBuilder` | Yes (`head` prop, optional) | Client `<Head>` / SSR | Dynamic SEO tags |
| Props | Yes | Component | Application data |

For titles and meta tags that change per page, `HeadBuilder` or the client
`<Head>` is usually a better fit because it can update without a full page
load. Use `viewData` when the value must be present in the initial HTML and
never changes during SPA navigation.

## Notes

- `viewData` is not escaped automatically for HTML attributes; treat values as
  untrusted and escape them if they come from user input.
- Do not use `viewData` for secrets, tokens or authorization decisions.
- If a placeholder has no matching key, it is left untouched; add a default in
  the template when needed.

Next: [Title & Meta](/guide/title-and-meta) ·
[Root Template](/guide/root-template) · [Pages](/guide/pages)
