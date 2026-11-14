# Introduction

Inertia.js Java brings the [Inertia.js](https://inertiajs.com) protocol to the
JVM: you keep Spring Boot or Quarkus as the single source of truth for routing,
controllers, validation and persistence, while Vue, React or Svelte renders the
interface as a modern single-page application.

There is no REST API to design, no duplicated validation, and no client-side
router to configure. Your controller returns a **page object** with a component
name and props; the official Inertia client swaps the component in place.

## What problem does it solve?

A classic server-rendered application is simple to build and secure, but every
interaction reloads the whole page. A SPA plus REST API feels great after the
first load, but forces you to maintain two applications: one for HTML, one for
JSON, plus authentication, validation and serialization in both.

Inertia removes the second application. The server keeps responding with HTML on
the first visit and with a compact JSON page object on every subsequent visit,
using the same controllers and the same validation rules.

| Dimension | Traditional MPA | SPA + REST API | Inertia.js |
|---|---|---|---|
| Routing | Server | Server + client router | Server only |
| First load | Full HTML | HTML shell + API calls | Full HTML with page object |
| Navigation | Full reload | XHR + client render | XHR + component swap |
| Validation | Server | Server + client (duplicated) | Server only |
| Data layer | Server-side | API + serializers + clients | Server-side props |
| Auth | Session | Tokens/CORS | Session + CSRF |
| Backend code | One app | Two APIs to maintain | One app |

## The modern monolith

The Inertia model is often called the **modern monolith**: one deployable
application, one session, one database, one security model — with the
interaction quality of a SPA.

<div class="pill-row">
  <span class="pill">Spring Boot 4.1.1</span>
  <span class="pill">Quarkus 3.40.1</span>
  <span class="pill">Vue 3 · React 19 · Svelte 5</span>
  <span class="pill">Protocol v3</span>
</div>

### Who owns what

| Concern | Server (Spring Boot / Quarkus) | Client (Vue / React / Svelte) |
|---|---|---|
| Routing | `@GetMapping`, `@Path`, `@RouteBase` | — |
| Controllers | `Inertia` facade, typed results | — |
| Validation | Bean Validation, error bags, Precognition | Renders `errors` props |
| Persistence | JPA, JDBC, reactive clients | — |
| Serialization | Page object (`component`, `props`, metadata) | — |
| Components | Component name, e.g. `Contacts/Index` | `src/pages/Contacts/Index.vue` |
| Reactivity | — | Framework state, `useForm`, `<Link>` |
| History & scroll | Flags on the page object | Enforced by the official client |

The server never returns raw JSON APIs as a public contract; the page object is
an implementation detail of the Inertia protocol.

## A taste of it

::: code-group

```java [Spring Boot]
@RestController
public class ContactsController {

    private final Inertia inertia;
    private final ContactRepository contacts;

    public ContactsController(Inertia inertia, ContactRepository contacts) {
        this.inertia = inertia;
        this.contacts = contacts;
    }

    @GetMapping("/contacts")
    public Object index() {
        return inertia.render("Contacts/Index",
            Map.of("contacts", contacts.findAll()));
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
}
```

```vue [Vue 3]
<script setup lang="ts">
import { Link } from '@inertiajs/vue3'

defineProps<{ contacts: Array<{ id: number; name: string }> }>()
</script>

<template>
  <h1>Contacts</h1>
  <Link href="/contacts/create">Create contact</Link>
  <ul>
    <li v-for="contact in contacts" :key="contact.id">
      {{ contact.name }}
    </li>
  </ul>
</template>
```

```tsx [React 19]
import { Link } from '@inertiajs/react'

export default function Index({ contacts }: { contacts: Contact[] }) {
  return (
    <main>
      <h1>Contacts</h1>
      <Link href="/contacts/create">Create contact</Link>
      <ul>
        {contacts.map((contact) => (
          <li key={contact.id}>{contact.name}</li>
        ))}
      </ul>
    </main>
  )
}
```

```svelte [Svelte 5]
<script lang="ts">
  import { Link } from '@inertiajs/svelte'
  let { contacts } = $props()
</script>

<h1>Contacts</h1>
<Link href="/contacts/create">Create contact</Link>
<ul>
  {#each contacts as contact (contact.id)}
    <li>{contact.name}</li>
  {/each}
</ul>
```

:::

The same controller serves the initial HTML and every subsequent navigation.
The component only changes when the controller renders a different page.

## Adapter modules

| Module | Purpose |
|---|---|
| `inertia-core` | Protocol v3 model, page object, props engines, BigInt, SSR policy |
| `spring-inertia` | Spring Boot 4.1 / Spring MVC adapter |
| `spring-inertia-security` | Spring Security integration (CSRF, redirects) |
| `quarkus-inertia` | Quarkus 3.40 JAX-RS, Reactive Routes and session integration |
| `quarkus-inertia-security` | Quarkus Security integration |
| `inertia-tck` | Executable protocol suites 01–11 shared by all transports |

## Next steps

- [How It Works](/guide/how-it-works) — the full request lifecycle and protocol headers.
- [Starter Kits](/guide/starter-kits) — generate a certified app with Contact CRUD.
- [Props](/guide/props) — the complete typed prop hierarchy.
- [Live Props](/guide/live-props) — real-time updates on top of the same model.
