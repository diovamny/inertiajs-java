---
layout: home
hero:
  name: Inertia.js Java
  text: Build single-page apps with Vue, React or Svelte — without writing a REST API.
  tagline: v0.0.6 · JDK 21+ · Spring Boot 4.1 & Quarkus 3.40 · C100 69/69 · I100 92/92
  actions:
    - theme: brand
      text: Get Started
      link: /guide/introduction
    - theme: alt
      text: View on GitHub
      link: https://github.com/diovamny/inertiajs-java
features:
  - icon: 🧪
    title: Protocol-First TCK
    details: 11 normative suites, 69/69 contract checks and 92/92 official-client interop cells — including Live Props and BigInt.
  - icon: ⚡
    title: v3-Pure Bootstrap
    details: The page object travels once, in a script tag. 59% smaller initial HTML than the legacy double payload.
  - icon: 🧱
    title: GraalVM Native Ready
    details: AOT compilation and native smoke tests for Spring Boot and Quarkus starters on every release.
  - icon: 🔴
    title: Live Props in Real Time
    details: Public, private and presence channels, event listeners and throttling — composed with deferred props.
  - icon: 🧩
    title: Zero-API Monolith
    details: Controllers, validation and persistence stay on the server. Vue, React and Svelte stay fully reactive.
  - icon: ✅
    title: Bean Validation Native
    details: Automatic error mapping, named error bags, multi-message arrays and Precognition without extra glue.
---

<div class="home-section">

## The modern monolith, now for Java

Inertia.js lets you build a classic server-driven application with the feel of a
single-page app. Your Spring Boot or Quarkus controllers return **page objects**
instead of JSON APIs, and the official Inertia client swaps components in place.

```text
GET /contacts            →  Contacts/Index  + props  →  HTML (first visit)
GET /contacts (XHR)      →  Contacts/Index  + props  →  JSON (Inertia visit)
```

No REST endpoints, no duplicated validation, no client-side router to maintain.
Just controllers, templates and the frontend framework you already know.

</div>

<div class="home-section">

## One controller. Three frontends.

### Server

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

    @PostMapping("/contacts")
    public Object store(@Valid @RequestBody ContactForm form) {
        var contact = contacts.create(form);
        return inertia.redirect("/contacts")
            .with("success", "Contact created.");
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
        var contact = contacts.create(form);
        return inertia.redirect("/contacts")
            .with("success", "Contact created.");
    }
}
```

:::

### Client

::: code-group

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
      <Link :href="`/contacts/${contact.id}/edit`">Edit</Link>
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
          <li key={contact.id}>
            {contact.name}{' '}
            <Link href={`/contacts/${contact.id}/edit`}>Edit</Link>
          </li>
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
    <li>
      {contact.name}
      <Link href={`/contacts/${contact.id}/edit`}>Edit</Link>
    </li>
  {/each}
</ul>
```

:::

</div>

<div class="home-section">

## Quickstart in three steps

<div class="step-grid">
  <div class="step-card">
    <span class="step-number">01</span>
    <h3>Generate a certified starter</h3>
    <p>Pick Spring Boot or Quarkus, then Vue, React or Svelte. Every starter ships a working Contact CRUD.</p>
  </div>
  <div class="step-card">
    <span class="step-number">02</span>
    <h3>Run backend and Vite</h3>
    <p>Start <code>mvn spring-boot:run</code> (or <code>mvn quarkus:dev</code>) next to <code>npm run dev</code>.</p>
  </div>
  <div class="step-card">
    <span class="step-number">03</span>
    <h3>Open the app</h3>
    <p>Visit <code>/contacts</code>, create a record and inspect the <code>X-Inertia</code> response in DevTools.</p>
  </div>
</div>

```bash
mvn -B archetype:generate \
  -DarchetypeGroupId=io.github.diovamny \
  -DarchetypeArtifactId=inertia-spring-vue-archetype \
  -DarchetypeVersion=0.0.6 \
  -DgroupId=com.example \
  -DartifactId=hello-inertia \
  -Dpackage=com.example.hello

cd hello-inertia
npm --prefix src/main/webui install
npm --prefix src/main/webui run dev   # terminal 1
mvn spring-boot:run                   # terminal 2
```

Replace `inertia-spring-vue-archetype` with `inertia-quarkus-vue-archetype`
(and `spring-boot:run` with `quarkus:dev`) for Quarkus. React and Svelte
variants use `-react-archetype` and `-svelte-archetype`.

[Starter kits](/guide/starter-kits) · [Server setup](/guide/server-setup) · [Client setup](/guide/client-setup)

</div>

<div class="home-section">

## What's new in 0.0.6

- **Live Props** — resolve props on demand and let the official client refresh
  them over public, private or presence channels. See [Live Props](/guide/live-props).
- **BigInt precision** — opt in globally or per response and keep 64-bit IDs
  intact. See [Responses & BigInt](/guide/responses#bigint-and-64-bit-ids).
- **Official SSR runtime** — `createServer` from `@inertiajs/*/server`, with
  `npm run build:ssr` and the sidecar on `:13714`. See [SSR](/guide/ssr).
- **Quarkus Vert.x session** — flash messages and CSRF work out of the box via
  `inertia.session.enabled=true`. See [Quarkus](/guide/quarkus#auto-provisioned-vertx-session).
- **Contact CRUD starters** — six certified archetypes with `ErrorPage`,
  `useForm` and integration tests. See [Starter Kits](/guide/starter-kits).
- **ADR-007** — the DevTools server protocol is intentionally out of scope. See
  [Not Supported](/reference/not-supported).

<div class="pill-row">
  <span class="pill">Java 21+</span>
  <span class="pill">Spring Boot 4.1.1</span>
  <span class="pill">Quarkus 3.40.1</span>
  <span class="pill">@inertiajs/* 3.11.0</span>
  <span class="pill">Protocol v3</span>
  <span class="pill">11 TCK suites</span>
  <span class="pill">GraalVM</span>
</div>

</div>
