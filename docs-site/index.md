---
layout: home
hero:
  name: Inertia.js Java
  text: Server-side v3 adapters for Spring Boot and Quarkus
  tagline: Controllers stay on the server. Pages stay reactive. No API required.
  actions:
    - theme: brand
      text: Spring Boot guide
      link: /guide/spring-boot
    - theme: alt
      text: Quarkus guide
      link: /guide/quarkus
features:
  - title: Protocol-first
    details: 58/59 normative requirements verified by an executable TCK on Spring MVC, Quarkus REST and Reactive Routes.
  - title: v3-pure bootstrap
    details: The page object travels once, in a script tag. 59% smaller initial HTML than the legacy double payload.
  - title: Native-ready
    details: GraalVM smoke tests on every release for Spring and Quarkus starters.
---

## Quickstart (under 10 minutes)

::: code-group

```xml [Spring Boot]
<dependency>
  <groupId>io.github.diovamny.spring.inertia</groupId>
  <artifactId>spring-inertia</artifactId>
  <version>0.0.4</version>
</dependency>
```

```xml [Quarkus]
<dependency>
  <groupId>io.github.diovamny.quarkus.inertia</groupId>
  <artifactId>quarkus-inertia</artifactId>
  <version>0.0.4</version>
</dependency>
```

:::

```java
// Spring Boot
@GetMapping("/contacts")
public Object index() {
    return inertia.render("Contacts/Index", Map.of("contacts", contacts.findAll()));
}
```

```java
// Quarkus
@GET @Path("/contacts")
public Uni<Object> index() {
    return inertia.render("Contacts/Index", Map.of("contacts", contacts.findAll()));
}
```

Or generate a working app (Vue 3 / React 19 / Svelte 5 starters):

```bash
mvn -B archetype:generate \
  -DarchetypeGroupId=io.github.diovamny \
  -DarchetypeArtifactId=inertia-spring-vue-archetype \
  -DarchetypeVersion=0.0.4 \
  -DgroupId=com.example -DartifactId=hello-inertia -Dpackage=com.example.hello
```

Next: [Spring Boot](/guide/spring-boot) · [Quarkus](/guide/quarkus) · [Compatibility](/compatibility)
