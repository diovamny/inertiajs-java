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
// Spring Boot — works with Vue and React alike (the server is client-agnostic)
@RestController
public class DashboardController {

    private final Inertia inertia;

    @GetMapping("/dashboard")
    public Object index() {
        return inertia.render("Dashboard", Map.of("contacts", contacts.list()));
    }
}
```

```java
// Quarkus — reactive resource. The @Path annotations ARE the router
// (the equivalent of Rails' config/routes.rb): each route renders a page,
// redirects, or answers an Inertia visit — no separate API layer.
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
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/contacts")          // <-- base route, like `resources :contacts`
@Blocking
public class ContactsController {

    @Inject Inertia inertia;
    @Inject ContactRepository contacts;

    @GET                     // GET /contacts  →  page "Contacts/Index"
    @Blocking
    public Uni<Object> index() {
        return inertia.render("Contacts/Index",
            Map.of("contacts", contacts.listAll()));
    }

    @GET                     // GET /contacts/create  →  page "Contacts/Create"
    @Path("create")
    @Blocking
    public Uni<Object> create() {
        return inertia.render("Contacts/Create",
            Map.of("organizations", organizations.listAll()));
    }

    @POST                    // POST /contacts  →  validate, then 303 redirect
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> store(ContactForm form) {
        var contact = contacts.create(form);
        return inertia.redirect("/contacts/" + contact.id)
            .with("message", "Contact created.");
    }

    @GET                     // GET /contacts/{id}/edit  →  page "Contacts/Edit"
    @Path("{id}/edit")
    @Blocking
    public Uni<Object> edit(@PathParam("id") long id) {
        return inertia.render("Contacts/Edit",
            Map.of("contact", contacts.findById(id)));
    }
}
```

Every route returns `Uni<Object>`: an HTML shell with the page object on the
first visit, the JSON page object on Inertia visits, a `303` + flash message
on `redirect()`, and a `409` re-visit when the asset version mismatches — all
handled by the adapter from these same methods.

```vue
<!-- Vue 3: webui/src/pages/Dashboard.vue -->
<script setup lang="ts">defineProps<{ contacts: Contact[] }>()</script>
<template>
  <ul><li v-for="c in contacts" :key="c.id">{{ c.name }}</li></ul>
</template>
```

```tsx
// React 19: webui/src/pages/Dashboard.tsx
export default function Dashboard({ contacts }: { contacts: Contact[] }) {
  return (
    <ul>{contacts.map((c) => <li key={c.id}>{c.name}</li>)}</ul>
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

```properties
inertia.root-template=index.html
```

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
