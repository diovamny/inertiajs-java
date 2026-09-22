# Spring Boot

Spring Boot 4.1 + Spring MVC adapter (`spring-inertia`), Java 21+.

## Install

```xml
<dependency>
  <groupId>io.github.diovamny.spring.inertia</groupId>
  <artifactId>spring-inertia</artifactId>
  <version>0.0.4</version>
</dependency>
```

```properties
inertia.root-template=index.html
```

## Render, redirect, flash

```java
@GetMapping("/contacts")
public Object index() {
    return inertia.render("Contacts/Index", Map.of("contacts", contacts.findAll()));
}

@PostMapping("/contacts")
public Object store(@Valid @RequestBody ContactForm form) {
    var contact = contacts.create(form);
    return inertia.redirect("/contacts/" + contact.id)
        .with("message", "Contact created.");
}
```

Validation failures flash errors and redirect back (`303`); precognition
answers `422`. Named bags via `X-Inertia-Error-Bag`.

## Root template

```html
<div id="app">__INERTIA_SSR_BODY__</div>
<script type="application/json" data-page="app">__INERTIA_PAGE_JSON__</script>
```

## Configuration

All 24 `inertia.*` settings: [configuration reference](https://github.com/diovamny/inertiajs-java/blob/release-0.0.4/docs/configuration.md).
