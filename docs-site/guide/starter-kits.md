# Starter Kits

Every release ships six certified Maven archetypes: Spring Boot or Quarkus,
each with Vue 3, React 19 or Svelte 5. All of them include a working in-memory
**Contact CRUD**, an `ErrorPage`, `useForm` integration, flash messages and
integration tests.

## The six archetypes

| Adapter | Frontend | Archetype artifact ID |
|---|---|---|
| Spring Boot 4.1 | Vue 3 | `inertia-spring-vue-archetype` |
| Spring Boot 4.1 | React 19 | `inertia-spring-react-archetype` |
| Spring Boot 4.1 | Svelte 5 | `inertia-spring-svelte-archetype` |
| Quarkus 3.40 | Vue 3 | `inertia-quarkus-vue-archetype` |
| Quarkus 3.40 | React 19 | `inertia-quarkus-react-archetype` |
| Quarkus 3.40 | Svelte 5 | `inertia-quarkus-svelte-archetype` |

All six are generated and exercised in CI, pinned to adapter `0.0.6` and
official client `3.11.0`.

## Generate a starter

```bash
mvn -B archetype:generate \
  -DarchetypeGroupId=io.github.diovamny \
  -DarchetypeArtifactId=inertia-spring-vue-archetype \
  -DarchetypeVersion=0.0.6 \
  -DgroupId=com.example \
  -DartifactId=hello-inertia \
  -Dpackage=com.example.hello
```

For another frontend change only the artifact ID:

```bash
# React 19
-DarchetypeArtifactId=inertia-spring-react-archetype

# Svelte 5
-DarchetypeArtifactId=inertia-spring-svelte-archetype

# Quarkus (Vue / React / Svelte)
-DarchetypeArtifactId=inertia-quarkus-vue-archetype
-DarchetypeArtifactId=inertia-quarkus-react-archetype
-DarchetypeArtifactId=inertia-quarkus-svelte-archetype
```

Optionally pin the framework version explicitly:

```bash
-DinertiaAdapterVersion=0.0.6 \
-DjavaVersion=21 \
-DframeworkVersion=4.1.1     # or 3.40.1 for Quarkus
```

## Run it

::: code-group

```bash [Spring Boot]
cd hello-inertia
npm --prefix src/main/webui install
npm --prefix src/main/webui run dev   # terminal 1: Vite on :5173
mvn spring-boot:run                   # terminal 2: app on :8080
```

```bash [Quarkus]
cd hello-inertia
npm --prefix src/main/webui install
npm --prefix src/main/webui run dev   # terminal 1: Vite on :5173
mvn quarkus:dev                       # terminal 2: app on :8080
```

:::

Open `http://localhost:8080/` and navigate to `/contacts`.

## What is inside

```text
hello-inertia/
├── pom.xml
├── src/main/java/com/example/hello/
│   ├── WelcomeController.java     # first page
│   ├── ContactsController.java    # Contact CRUD (Quarkus: ContactsResource)
│   ├── ContactForm.java           # validation record
│   ├── Contact.java               # in-memory entity
│   └── ContactRepository.java     # seed data, ConcurrentHashMap
├── src/main/resources/
│   ├── application.properties
│   ├── templates/index.html       # root template (Thymeleaf / Qute)
│   └── static/                    # Vite build output
├── src/main/webui/
│   ├── package.json               # @inertiajs/* 3.11.0, TypeScript 6, Vite 8
│   ├── vite.config.ts
│   └── src/
│       ├── app.ts / app.tsx       # createInertiaApp
│       ├── ssr.ts / ssr.tsx       # official createServer entry
│       └── pages/
│           ├── Layout.vue|tsx|svelte
│           ├── ErrorPage.*
│           └── Contacts/{Index,Create,Edit}.*
└── src/test/java/...              # Welcome + Contacts integration tests
```

### Contact CRUD

The backend implements the complete resource:

| Method | Path | Result |
|---|---|---|
| `GET` | `/contacts` | `Contacts/Index` with all contacts |
| `GET` | `/contacts/create` | `Contacts/Create` with an empty form |
| `POST` | `/contacts` | Validate, create, `303` to `/contacts` + flash |
| `GET` | `/contacts/{id}/edit` | `Contacts/Edit` with the contact |
| `PUT` | `/contacts/{id}` | Validate, update, `303` + flash |
| `DELETE` | `/contacts/{id}` | Delete, `303` + flash |

::: code-group

```java [Spring Boot]
@Controller
public class ContactsController {

    private final Inertia inertia;
    private final ContactRepository repository;

    public ContactsController(Inertia inertia, ContactRepository repository) {
        this.inertia = inertia;
        this.repository = repository;
    }

    @GetMapping("/contacts")
    public Object index() {
        return inertia.render("Contacts/Index",
            Map.of("contacts", repository.findAll()));
    }

    @PostMapping("/contacts")
    public Object store(@Valid ContactForm form, BindingResult result) {
        if (result.hasErrors()) {
            return inertia.back()
                .withErrors(fieldErrors(result))
                .withInput(inputOf(form));
        }
        repository.create(form);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact created.");
    }
}
```

```java [Quarkus]
@Path("/contacts")
public class ContactsResource {

    @Inject Inertia inertia;
    @Inject ContactRepository repository;

    @GET
    public Uni<Object> index() {
        return inertia.render("Contacts/Index",
            Map.of("contacts", repository.listAll()));
    }

    @POST
    public Uni<Object> store(@Valid ContactForm form) {
        repository.create(form);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contact created.");
    }
}
```

:::

Validation failures return `303` to the previous page with `errors` and
`old` input, so the form can repopulate itself without any client-side schema.

### Why `<parameters>true</parameters>`?

Both generated POMs enable Maven compiler parameter names:

```xml
<plugin>
  <groupId>org.apache.maven.plugins</groupId>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <parameters>true</parameters>
  </configuration>
</plugin>
```

Without it, `@PathVariable long id` and `@PathParam("id") long id` cannot bind
reliably in records and controller methods compiled without debug metadata.
The starters enable it by default.

### Tests included

| Starter | Test | What it covers |
|---|---|---|
| Spring | `WelcomeControllerTest` | HTML shell + `X-Inertia` JSON |
| Spring | `ContactsControllerTest` | CRUD, validation errors, flash |
| Quarkus | `WelcomeControllerTest` | HTML shell + `X-Inertia` JSON |
| Quarkus | `ContactsResourceTest` | CRUD, validation errors, flash |

Run them with `mvn test`; the frontend is built automatically as part of the
Maven lifecycle.

## SSR scripts

The starters also ship the official SSR runtime:

```bash
cd src/main/webui
npm run build       # client bundle
npm run build:ssr   # dist-ssr/ssr.js
npm run ssr         # sidecar on :13714
```

Enable it in the backend with `inertia.ssr-enabled=true` and
`inertia.ssr-url=http://localhost:13714/render`. See [SSR](/guide/ssr) for the
trust boundary and fallback behavior.

## Certification

| Archetype | Generate | Test | Boot | HTML visit | JSON visit |
|---|:---:|:---:|:---:|:---:|:---:|
| Spring Vue | ✅ | ✅ 8/8 | ✅ | ✅ | ✅ |
| Spring React | ✅ | ✅ | ✅ | ✅ | ✅ |
| Spring Svelte | ✅ | ✅ | ✅ | ✅ | ✅ |
| Quarkus Vue | ✅ | ✅ 8/8 | ✅ | ✅ | ✅ |
| Quarkus React | ✅ | ✅ | ✅ | ✅ | ✅ |
| Quarkus Svelte | ✅ | ✅ | ✅ | ✅ | ✅ |

Every generated project also passes the static contract checks: client
`3.11.0`, adapter and `inertia-core` pinned to `0.0.6`, `<parameters>true</parameters>`,
no `ssr-server.mjs`, and a valid TypeScript configuration for its framework.

Next: [Server Setup](/guide/server-setup) · [Client Setup](/guide/client-setup)
· [Spring Boot](/guide/spring-boot) · [Quarkus](/guide/quarkus)
