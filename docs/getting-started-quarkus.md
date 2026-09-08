# Getting Started — Quarkus

Minimal path from zero to your first Inertia v3 page with Quarkus 3.39 (reactive).
Requires JDK 21+, Maven 3.9+ and Node.js 22+.

## Option A — starter kit (recommended)

Generate a runnable app (Vue 3 or React 19, TypeScript, Vite, tests):

```bash
mvn -B archetype:generate \
  -DarchetypeGroupId=io.github.dg \
  -DarchetypeArtifactId=inertia-quarkus-vue-archetype \
  -DarchetypeVersion=0.0.1 \
  -DgroupId=com.example \
  -DartifactId=hello-inertia \
  -Dpackage=com.example.hello
```

(For React use `-DarchetypeArtifactId=inertia-quarkus-react-archetype`.)

```bash
cd hello-inertia
npm --prefix src/main/webui install
npm --prefix src/main/webui run dev   # terminal 1: Vite
mvn quarkus:dev                       # terminal 2: backend
```

Open `http://localhost:8080/`.

## Option B — add to an existing app

```xml
<dependency>
    <groupId>io.github.dg.quarkus.inertia</groupId>
    <artifactId>quarkus-inertia</artifactId>
    <version>0.0.1</version>
</dependency>
```

```properties
# application.properties
inertia.root-template=index.html
inertia.use-qute=true
```

```java
@Path("/")
public class WelcomeController {

    @Inject Inertia inertia;

    @GET
    public Uni<Object> welcome() {
        return inertia.render("Welcome", Map.of("appName", "Hello Inertia"));
    }
}
```

Add a Qute root template at `src/main/resources/templates/index.html` (see the
starter kit for a copy-paste template), a Vite frontend resolving the
`Welcome` page, then `npm run build` + `mvn package`.

## Full CRUD example — ContactsController

The same four methods serve Vue and React alike. (Prefer `@RouteBase` /
`@Route` reactive routes? See the `@Router` variant in the main [README](../README.md).)

```java
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

@Path("/contacts")
@Blocking
public class ContactsController {

    @Inject Inertia inertia;
    @Inject ContactRepository contacts;

    @GET
    @Blocking
    public Uni<Object> index() {
        // GET /contacts  →  page "Contacts/Index"
        return inertia.render("Contacts/Index",
            Map.of("contacts", contacts.listAll()));
    }

    @GET
    @Path("create")
    @Blocking
    public Uni<Object> create() {
        // GET /contacts/create  →  page "Contacts/Create"
        return inertia.render("Contacts/Create",
            Map.of("organizations", organizations.listAll()));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> store(ContactForm form) {
        // POST /contacts  →  validate, then 303 redirect with flash data
        var contact = contacts.create(form);
        return inertia.redirect("/contacts/" + contact.id)
            .with("message", "Contact created.");
    }

    @GET
    @Path("{id}/edit")
    @Blocking
    public Uni<Object> edit(@PathParam("id") long id) {
        // GET /contacts/{id}/edit  →  page "Contacts/Edit"
        return inertia.render("Contacts/Edit",
            Map.of("contact", contacts.findById(id)));
    }
}
```

> **Note:** `@Blocking` is shown because classic repository access blocks. If
> your database client is reactive (Hibernate Reactive, MongoDB reactive),
> drop `@Blocking` and stay on the event loop.

## Test it

```java
@QuarkusTest
class WelcomeControllerTest {

    @Test
    void initialVisitReturnsHtml() {
        given().when().get("/")
            .then().statusCode(200)
            .body(containsString("id=\"app\""));
    }

    @Test
    void inertiaVisitReturnsJson() {
        given().header("X-Inertia", "true").when().get("/")
            .then().statusCode(200)
            .header("X-Inertia", equalTo("true"));
    }
}
```

See [testing](testing-guide.md), [configuration](configuration.md) and
[protocol compatibility](protocol-compatibility.md).
