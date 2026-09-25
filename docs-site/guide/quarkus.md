# Quarkus

Quarkus 3.39 JAX-RS adapter (`quarkus-inertia`), Java 21+.

## Install

```xml
<dependency>
  <groupId>io.github.diovamny.quarkus.inertia</groupId>
  <artifactId>quarkus-inertia</artifactId>
  <version>0.0.5</version>
</dependency>
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-rest-jackson</artifactId>
</dependency>
```

```properties
inertia.root-template=index.html
```

## Render, redirect, flash

```java
@GET @Path("/contacts")
public Uni<Object> index() {
    return inertia.render("Contacts/Index", Map.of("contacts", contacts.findAll()));
}

@POST @Path("/contacts")
public Uni<Object> store(@Valid ContactForm form) {
    var contact = contacts.create(form);
    return inertia.redirect("/contacts/" + contact.id)
        .with("message", "Contact created.");
}
```

CSRF failures answer `303 + Location + flash` on Inertia visits — the same
default as Spring. File uploads via `@RestForm FileUpload`.

## Root template (Qute or placeholders)

```html
<div id="app">{ssrBody}</div>
<script type="application/json" data-page="app">{dataPage}</script>
```
