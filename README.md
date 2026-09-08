# Inertia.js Java Adapters

Single-page Vue and React apps powered by Spring Boot and Quarkus controllers. No API required.

[![CI](https://github.com/OWNER/inertiajs-java/actions/workflows/ci.yml/badge.svg)](https://github.com/OWNER/inertiajs-java/actions/workflows/ci.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue)](LICENSE)
[![Java 21](https://img.shields.io/badge/java-21-blue)](https://adoptium.net/)
<!-- TODO(publication): add Maven Central version badge after the first release. -->

Visit [inertiajs.com](https://inertiajs.com/) to learn the protocol. This is a community
project and is not officially maintained by the Inertia.js team.

## Your controllers. Your routes. Modern components.

One router on the server, props to the page — like `inertia-rails`, but with
Spring MVC annotations or Quarkus reactive resources instead of `routes.rb`:

| URL | Spring Boot | Quarkus | Page |
|---|---|---|---|
| `GET /` | `WelcomeController#welcome` | `WelcomeController#welcome` | `Welcome` |
| `GET /dashboard` | `DashboardController#index` | `DashboardController#index` | `Dashboard` |
| `GET /contacts` | `ContactsController#index` | `ContactsController#index` | `Contacts/Index` |
| `GET /contacts/create` | `ContactsController#create` | `ContactsController#create` | `Contacts/Create` |
| `POST /contacts` | `ContactsController#store` → `303` | `ContactsController#store` → `303` | (redirect) |
| `GET /contacts/{id}/edit` | `ContactsController#edit` | `ContactsController#edit` | `Contacts/Edit` |

```java
// Spring Boot — same four methods (the server is client-agnostic: this one
// controller serves the Vue and the React pages below)
package com.example.crm;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class ContactsController {

    private final Inertia inertia;
    private final ContactRepository contacts;

    public ContactsController(Inertia inertia, ContactRepository contacts) {
        this.inertia = inertia;
        this.contacts = contacts;
    }

    @GetMapping("/contacts")           // GET /contacts  →  "Contacts/Index"
    public Object index() {
        return inertia.render("Contacts/Index",
            Map.of("contacts", contacts.findAll()));
    }

    @GetMapping("/contacts/create")    // GET /contacts/create  →  "Contacts/Create"
    public Object create() {
        return inertia.render("Contacts/Create",
            Map.of("organizations", organizations.findAll()));
    }

    @PostMapping(value = "/contacts", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object store(@RequestBody ContactForm form) {   // POST → 303 redirect
        var contact = contacts.create(form);
        return inertia.redirect("/contacts/" + contact.id)
            .with("message", "Contact created.");
    }

    @GetMapping("/contacts/{id}/edit") // GET /contacts/{id}/edit  →  "Contacts/Edit"
    public Object edit(@PathVariable long id) {
        return inertia.render("Contacts/Edit",
            Map.of("contact", contacts.findById(id)));
    }
}
```

```java
// Quarkus — @Router style with Reactive Routes. @RouteBase + @Route ARE the
// router (the equivalent of Rails' config/routes.rb): each route renders a
// page, redirects, or answers an Inertia visit — no separate API layer.
// Requires the quarkus-reactive-routes extension on the classpath.
package com.example.crm;

import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import io.github.dg.quarkus.inertia.api.Inertia;
import io.quarkus.vertx.web.Route;
import io.quarkus.vertx.web.RouteBase;
import io.quarkus.vertx.web.Route.HttpMethod;
import io.quarkus.vertx.web.Body;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

@RouteBase(path = "/contacts")   // <-- base route, like `resources :contacts`
public class ContactsRouter {

    @Inject Inertia inertia;
    @Inject ContactRepository contacts;

    @Route(path = "", methods = HttpMethod.GET)   // GET /contacts → "Contacts/Index"
    public Uni<Object> index() {
        return inertia.render("Contacts/Index",
            Map.of("contacts", contacts.listAll()));
    }

    @Route(path = "/create", methods = HttpMethod.GET) // GET /contacts/create
    public Uni<Object> create() {
        return inertia.render("Contacts/Create",
            Map.of("organizations", organizations.listAll()));
    }

    @Route(path = "", methods = HttpMethod.POST,   // POST /contacts → 303
            consumes = MediaType.APPLICATION_JSON)
    public Uni<Object> store(@Body ContactForm form) {
        var contact = contacts.create(form);
        return inertia.redirect("/contacts/" + contact.id)
            .with("message", "Contact created.");
    }

    @Route(path = "/:id/edit", methods = HttpMethod.GET) // GET /contacts/:id/edit
    public Uni<Object> edit(RoutingContext rc) {
        long id = Long.parseLong(rc.pathParam("id"));
        return inertia.render("Contacts/Edit",
            Map.of("contact", contacts.findById(id)));
    }
}
```

> **Note:** these routes run on the event loop without `@Blocking`. Only add
> `@Blocking` to a method if your database access is not reactive (classic JPA,
> JDBC, Panache blocking); with a reactive client (Hibernate Reactive,
> MongoDB reactive, REST calls) leave it off.

Every route returns `Uni<Object>` (Quarkus) or `Object` (Spring): an HTML shell
with the page object on the first visit, the JSON page object on Inertia
visits, a `303` + flash message on `redirect()`, and a `409` re-visit when the
asset version mismatches — all handled by the adapter from these same methods.

```java
// Quarkus — same four methods WITHOUT @Router: plain JAX-RS style with
// @Path. Pick whichever style fits your app; the adapter supports both.
// Requires the quarkus-rest extension on the classpath.
package com.example.crm;

import java.util.Map;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import io.github.dg.quarkus.inertia.api.Inertia;
import io.smallrye.mutiny.Uni;

@Path("/contacts")          // <-- base route, like `resources :contacts`
public class ContactsController {

    @Inject Inertia inertia;
    @Inject ContactRepository contacts;

    @GET                     // GET /contacts  →  page "Contacts/Index"
    public Uni<Object> index() {
        return inertia.render("Contacts/Index",
            Map.of("contacts", contacts.listAll()));
    }

    @GET                     // GET /contacts/create  →  page "Contacts/Create"
    @Path("create")
    public Uni<Object> create() {
        return inertia.render("Contacts/Create",
            Map.of("organizations", organizations.listAll()));
    }

    @POST                    // POST /contacts  →  validate, then 303 redirect
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Object> store(ContactForm form) {
        var contact = contacts.create(form);
        return inertia.redirect("/contacts/" + contact.id)
            .with("message", "Contact created.");
    }

    @GET                     // GET /contacts/{id}/edit  →  page "Contacts/Edit"
    @Path("{id}/edit")
    public Uni<Object> edit(@PathParam("id") long id) {
        return inertia.render("Contacts/Edit",
            Map.of("contact", contacts.findById(id)));
    }
}
```

```vue
<!-- Vue 3 using the routes: webui/src/pages/Contacts/Index.vue -->
<script setup lang="ts">
import { Link } from '@inertiajs/vue3'

interface Contact {
  id: number
  name: string
}

defineProps<{
  contacts: Contact[]
}>()
</script>

<template>
  <main class="contacts">
    <h1>Contacts</h1>
    <Link href="/contacts/create" class="button">Create contact</Link>
    <ul>
      <li v-for="c in contacts" :key="c.id">
        {{ c.name }}
        <Link :href="`/contacts/${c.id}/edit`">Edit</Link>
      </li>
    </ul>
  </main>
</template>

<style scoped>
.contacts {
  max-width: 640px;
  margin: 2rem auto;
}
.button {
  display: inline-block;
  margin-bottom: 1rem;
}
</style>
```

```vue
<!-- Vue 3 posting to the routes: webui/src/pages/Contacts/Create.vue -->
<script setup lang="ts">
import { Form } from '@inertiajs/vue3'
import { ref } from 'vue'

const name = ref<string>('')
</script>

<template>
  <main class="contacts">
    <h1>Create contact</h1>
    <Form action="/contacts" method="post" v-slot="{ errors, processing }">
      <label for="name">Name</label>
      <input id="name" v-model="name" name="name" placeholder="Name" />
      <div v-if="errors.name" class="error">{{ errors.name }}</div>
      <button type="submit" :disabled="processing">Save</button>
    </Form>
  </main>
</template>

<style scoped>
.contacts {
  max-width: 640px;
  margin: 2rem auto;
}
.error {
  color: #c00;
}
</style>
```

```tsx
// React 19 using the routes: webui/src/pages/Contacts/Index.tsx
import { Link } from '@inertiajs/react'

export default function Index({ contacts }: { contacts: { id: number; name: string }[] }) {
  return (
    <>
      <Link href="/contacts/create">Create contact</Link>
      <ul>
        {contacts.map((c) => (
          <li key={c.id}>
            {c.name} <Link href={`/contacts/${c.id}/edit`}>Edit</Link>
          </li>
        ))}
      </ul>
    </>
  )
}
```

```tsx
// React 19 posting to the routes: webui/src/pages/Contacts/Create.tsx
import { useForm } from '@inertiajs/react'

export default function Create() {
  const { data, setData, post, errors, processing } = useForm({ name: '' })
  return (
    <form onSubmit={(e) => { e.preventDefault(); post('/contacts') }}>
      <input value={data.name} onChange={(e) => setData('name', e.target.value)} placeholder="Name" />
      {errors.name && <div>{errors.name}</div>}
      <button type="submit" disabled={processing}>Save</button>
    </form>
  )
}
```

That's the whole loop on either stack: the route renders props, the component
renders them. Links and form submits become XHR visits, so navigation feels
instant — while you keep writing plain Spring MVC or Quarkus resources,
sessions and validation on the server.

## Get started

**Spring Boot 4.1** (`io.github.dg.spring.inertia:spring-inertia`) and
**Quarkus 3.38** (`io.github.dg.quarkus.inertia:quarkus-inertia`) require Java 21+:

```xml
<dependency>
    <groupId>io.github.dg.spring.inertia</groupId>
    <artifactId>spring-inertia</artifactId>
    <version>0.0.1</version>
</dependency>
```

```xml
<dependency>
    <groupId>io.github.dg.quarkus.inertia</groupId>
    <artifactId>quarkus-inertia</artifactId>
    <version>0.0.1</version>
</dependency>
```

Quarkus also needs one transport extension, matching the controller style you
choose:

```xml
<!-- JAX-RS style (@Path): RESTEasy Reactive -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rest-jackson</artifactId>
</dependency>
```

```xml
<!-- @Router style (@RouteBase/@Route): Reactive Routes -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-reactive-routes</artifactId>
</dependency>
```

```properties
inertia.root-template=index.html
```

All 23 `inertia.*` settings with defaults and per-framework availability:
[configuration reference](docs/configuration.md).

Or start from a working app with one command — **starter kits** with Vue 3 or
React 19, TypeScript, Vite, tests and an optional native `Dockerfile`:

| Starter | Command |
|---|---|
| Spring Boot + Vue 3 | `mvn -B archetype:generate -DarchetypeGroupId=io.github.dg -DarchetypeArtifactId=inertia-spring-vue-archetype -DarchetypeVersion=0.0.1 -DgroupId=com.example -DartifactId=hello-inertia -Dpackage=com.example.hello` |
| Spring Boot + React 19 | Same with `-DarchetypeArtifactId=inertia-spring-react-archetype` |
| Quarkus + Vue 3 | Same with `-DarchetypeArtifactId=inertia-quarkus-vue-archetype` |
| Quarkus + React 19 | Same with `-DarchetypeArtifactId=inertia-quarkus-react-archetype` |

Full walkthroughs: [Spring](docs/getting-started-spring.md) and
[Quarkus](docs/getting-started-quarkus.md).

## Built for real Java apps

| | |
|---|---|
| **Forms that work** | Validation errors flow to your components automatically. |
| **Testing DSL** | `InertiaPage` assertions for MockMvc and REST-assured. |
| **Partial reloads, deferred/once/merge props** | The full Inertia v3 prop model on both adapters. |
| **Shared data SPI** | Current user, flash and errors on every page. |
| **SSR + fallback** | Optional Node.js sidecar, graceful client-only fallback. |
| **GraalVM Native** | Both adapters ship native hints; starters include a native `Dockerfile`. |

Compatibility is evidence-based: see [protocol compatibility](docs/protocol-compatibility.md),
linked row-by-row to contract tests that CI verifies on every push.

## Documentation

- **Start:** [Spring Boot](docs/getting-started-spring.md) · [Quarkus](docs/getting-started-quarkus.md)
- **Reference:** [Configuration](docs/configuration.md) (all 23 `inertia.*` settings) · [Protocol compatibility](docs/protocol-compatibility.md) · [Migration](docs/migration.md)
- **Features:** [Shared data and props](docs/shared-data-and-props.md) · [View data](docs/viewdata-guide.md) · [Testing](docs/testing-guide.md) · [SSR setup](docs/ssr-setup.md) · [Native image](docs/native-image.md)
- **Design decisions:** [ADR-001](docs/adr/001-native-builder-jdk-25.md) · [ADR-002](docs/adr/002-partial-null-props.md)

## Contribute and run tests

```bash
mvn clean test            # adapters: Spring + Quarkus suites
mvn clean test -Pexamples # demo applications
```

Bug reports and pull requests are welcome. Please read [CONTRIBUTING.md](CONTRIBUTING.md)
and follow the [Code of Conduct](CODE_OF_CONDUCT.md). **Do not report security
vulnerabilities in public issues** — see [SECURITY.md](.github/SECURITY.md).

## Credits

Community project for the Java ecosystem. Released under the [MIT License](LICENSE).
Inertia.js is created by Jonathan Reinink and contributors; Java, Spring, Quarkus,
Vue.js and React trademarks belong to their respective owners (see
[THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md) where applicable).
