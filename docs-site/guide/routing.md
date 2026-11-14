# Routing

Routing stays on the server: Spring MVC or JAX-RS decides which controller runs
and which page component is rendered. The client only knows how to navigate to a
URL and how to render the component it receives.

## Server routes

::: code-group

```java [Spring Boot]
@Controller
public class ContactsController {

    @GetMapping("/contacts")
    public Object index() {
        return inertia.render("Contacts/Index", Map.of("contacts", contacts.findAll()));
    }

    @PostMapping("/contacts")
    public Object store(@Valid ContactForm form) {
        contacts.create(form);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact created.");
    }

    @PutMapping("/contacts/{id}")
    public Object update(@PathVariable long id, @Valid ContactForm form) {
        contacts.update(id, form);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact updated.");
    }

    @DeleteMapping("/contacts/{id}")
    public Object destroy(@PathVariable long id) {
        contacts.delete(id);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact deleted.");
    }
}
```

```java [Quarkus]
@Path("/contacts")
public class ContactsResource {

    @Inject Inertia inertia;
    @Inject ContactRepository contacts;

    @GET
    public Uni<Object> index() {
        return inertia.render("Contacts/Index",
            Map.of("contacts", contacts.listAll()));
    }

    @POST
    public Uni<Object> store(@Valid ContactForm form) {
        contacts.create(form);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact created.");
    }

    @PUT
    @Path("{id}")
    public Uni<Object> update(@PathParam("id") long id,
            @Valid ContactForm form) {
        contacts.update(id, form);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact updated.");
    }

    @DELETE
    @Path("{id}")
    public Uni<Object> destroy(@PathParam("id") long id) {
        contacts.delete(id);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact deleted.");
    }
}
```

:::

There is no client-side route table, no route names and no duplicated path
definitions. Vue Router, React Router and SvelteKit routing are not used.

## Client navigation with `<Link>`

::: code-group

```vue [Vue 3]
<script setup lang="ts">
import { Link } from '@inertiajs/vue3'
</script>

<template>
  <nav>
    <Link href="/contacts">Contacts</Link>
    <Link href="/contacts/create" preserve-scroll>New contact</Link>
    <Link href="/contacts" replace>Reload list</Link>
  </nav>
</template>
```

```tsx [React 19]
import { Link } from '@inertiajs/react'

export default function Navigation() {
  return (
    <nav>
      <Link href="/contacts">Contacts</Link>
      <Link href="/contacts/create" preserveScroll>
        New contact
      </Link>
      <Link href="/contacts" replace>
        Reload list
      </Link>
    </nav>
  )
}
```

```svelte [Svelte 5]
<script lang="ts">
  import { Link } from '@inertiajs/svelte'
</script>

<nav>
  <Link href="/contacts">Contacts</Link>
  <Link href="/contacts/create" preserveScroll>New contact</Link>
  <Link href="/contacts" replace>Reload list</Link>
</nav>
```

:::

`<Link>` renders a real `<a>` element. Middle-click, `Ctrl+click` and "open in a
new tab" perform a normal browser navigation. External links should use a plain
`<a href="https://...">` and are never intercepted.

### Link options

| Prop | Effect |
|---|---|
| `method` | HTTP method (default `get`) |
| `data` | Request body/query data |
| `as` | Render as another element (e.g. `button`) |
| `preserveScroll` | Keep the current scroll position |
| `preserveState` | Keep local component state |
| `replace` | Replace the history entry instead of pushing |
| `only` / `except` | Partial reload keys |
| `headers` | Extra request headers |
| `onSuccess` / `onError` / `onFinish` | Callbacks |

A link can also submit data:

```vue
<Link href="/contacts" method="post" :data="{ name: 'Ada' }" as="button">
  Create
</Link>
```

## Programmatic visits

::: code-group

```js [Vue 3]
import { router } from '@inertiajs/vue3'

router.visit('/contacts/create')
router.get('/contacts', { search: 'ada' }, { preserveState: true })
router.post('/contacts', { name: 'Ada' })
router.reload({ only: ['contacts'] })
```

```js [React 19]
import { router } from '@inertiajs/react'

router.visit('/contacts/create')
router.get('/contacts', { search: 'ada' }, { preserveState: true })
router.post('/contacts', { name: 'Ada' })
router.reload({ only: ['contacts'] })
```

```js [Svelte 5]
import { router } from '@inertiajs/svelte'

router.visit('/contacts/create')
router.get('/contacts', { search: 'ada' }, { preserveState: true })
router.post('/contacts', { name: 'Ada' })
router.reload({ only: ['contacts'] })
```

:::

`router.visit(url, options)` is the general form; `router.get`, `post`, `put`,
`patch` and `delete` are shorthands. `router.reload()` performs a partial reload
of the current page.

| Option | Default | Meaning |
|---|---|---|
| `method` | `get` | HTTP method |
| `data` | `{}` | Request data |
| `replace` | `false` | Replace history entry |
| `preserveState` | `false` | Keep component state |
| `preserveScroll` | `false` | Keep scroll position |
| `only` / `except` | — | Partial reload keys |
| `headers` | `{}` | Extra headers |

## Server-side detection

The adapter decides the response format from the request:

```java
boolean isInertia = request.getHeader("X-Inertia") != null;
```

- Initial visit: render the root template with the page object.
- Inertia visit: serialize the page object as JSON with `X-Inertia: true`.
- Version mismatch: `409` + `X-Inertia-Location`.
- External redirect: `409` + `X-Inertia-Location`.

You do not need to branch on headers in controllers; `inertia.render(...)` and
`inertia.redirect(...)` handle both cases.

## Route organization

A typical application groups controllers by resource:

```text
src/main/java/com/example/
├── contacts/
│   ├── ContactsController.java   # /contacts
│   ├── ContactForm.java
│   └── ContactRepository.java
├── dashboard/
│   └── DashboardController.java  # /dashboard
└── auth/
    └── LoginController.java      # /login
```

Component names should mirror the resource: `Contacts/Index`, `Contacts/Create`,
`Contacts/Edit`. This keeps server and client paths easy to find.

Next: [Redirects](/guide/redirects) · [Pages](/guide/pages) ·
[Partial Reloads](/guide/partial-reloads)
