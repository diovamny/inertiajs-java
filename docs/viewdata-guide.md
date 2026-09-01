# viewData Guide — Root Template Data Injection

The `viewData` API allows you to pass arbitrary data directly into the root HTML template, **outside** the Inertia page JSON object. This is useful for injecting page titles, meta tags, Open Graph data, or any other server-side HTML values that don't need to be accessible to your frontend JavaScript.

---

## How It Works

When you call `inertia.viewData(key, value)`, the adapter stores that data alongside the request context. When rendering the HTML root template:

1. **Qute templates (Quarkus)**: data is passed as template data variables accessible directly in the template.
2. **HTML templates with placeholders (both adapters)**: the adapter performs token substitution for `__VIEW_<KEY>__` placeholders.

---

## Spring Boot

### Controller usage

```java
@GetMapping("/blog/{slug}")
public ResponseEntity<?> blogPost(@PathVariable String slug, Inertia inertia) {
    Post post = postService.findBySlug(slug);
    inertia.viewData("title", post.getTitle());
    inertia.viewData("description", post.getExcerpt());
    return inertia.render("Blog/Post", Map.of("post", post));
}
```

### Template — Thymeleaf

Use the standard Thymeleaf variable syntax:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title th:text="${viewData['title']} ?: 'My App'">My App</title>
    <meta name="description" th:content="${viewData['description']}">
</head>
<body>
    <div id="app" th:attr="data-page=${page}"></div>
    <script type="module" src="/resources/js/app.js"></script>
</body>
</html>
```

### Template — Placeholder substitution

If you use a static HTML file (e.g. for Vite-generated templates), use `__VIEW_<KEY>__` placeholders:

```html
<!DOCTYPE html>
<html>
<head>
    <title>__VIEW_TITLE__</title>
    <meta name="description" content="__VIEW_DESCRIPTION__">
</head>
<body>
    <div id="app" data-page="{{ page }}"></div>
</body>
</html>
```

The adapter replaces `__VIEW_TITLE__` and `__VIEW_DESCRIPTION__` with the values from `viewData("title", ...)` and `viewData("description", ...)`.

> **Note**: Placeholder keys are **case-insensitive** — `__VIEW_TITLE__` matches `viewData("title", ...)` and `viewData("TITLE", ...)`.

---

## Quarkus

### Controller usage

```java
@Inject Inertia inertia;

@GET @Path("/blog/{slug}")
public Response blogPost(@PathParam("slug") String slug) {
    Post post = postService.findBySlug(slug);
    inertia.viewData("title", post.getTitle());
    inertia.viewData("description", post.getExcerpt());
    return inertia.render("Blog/Post", Map.of("post", post));
}
```

### Template — Qute

Quarkus uses Qute templates. View data is passed as template variables:

```html
<!DOCTYPE html>
<html>
<head>
    <title>{title ?: 'My App'}</title>
    <meta name="description" content="{description}">
</head>
<body>
    <div id="app" data-page="{page}"></div>
    <script type="module" src="/resources/js/app.js"></script>
</body>
</html>
```

### Template — Placeholder substitution (static HTML)

You may also use the `__VIEW_<KEY>__` placeholder syntax with a static `.html` file:

```html
<title>__VIEW_TITLE__</title>
<meta name="description" content="__VIEW_DESCRIPTION__">
```

---

## viewData API Reference

| Method | Description |
|--------|-------------|
| `viewData(key, value)` | Stores a single key/value for root template injection |
| `viewData(Map<String, Object>)` | Stores multiple key/values at once |

> `viewData` keys are **not** included in the Inertia JSON page object and are **not** accessible in your frontend JavaScript components.

---

## Placeholder Format

The `__VIEW_<KEY>__` placeholder format follows these rules:

- Prefix and suffix: double underscore `__`
- Middle: `VIEW_` followed by the key in **UPPER_SNAKE_CASE**
- Examples:
  - `viewData("title", ...)` → `__VIEW_TITLE__`
  - `viewData("og_image", ...)` → `__VIEW_OG_IMAGE__`
  - `viewData("pageDescription", ...)` → `__VIEW_PAGEDESCRIPTION__` (camelCase key becomes uppercase)

---

## Example: Full SEO Setup

```java
// Controller
inertia.viewData("title", post.getTitle() + " | My Blog");
inertia.viewData("og_title", post.getTitle());
inertia.viewData("og_description", post.getExcerpt());
inertia.viewData("og_image", post.getCoverImageUrl());
return inertia.render("Blog/Post", Map.of("post", post));
```

```html
<!-- root template -->
<title>__VIEW_TITLE__</title>
<meta property="og:title" content="__VIEW_OG_TITLE__">
<meta property="og:description" content="__VIEW_OG_DESCRIPTION__">
<meta property="og:image" content="__VIEW_OG_IMAGE__">
```
