# Quarkus Reactive Routes

`@RouteBase` + `@Route` style on the event loop (`quarkus-reactive-routes`).

```java
@RouteBase(path = "/contacts")
public class ContactsRouter {
    @Inject Inertia inertia;

    @Route(path = "", methods = HttpMethod.GET)
    public Uni<Object> index() {
        return inertia.render("Contacts/Index",
            Map.of("contacts", contacts.listAll()));
    }
}
```

> ⚠️ **Stabilization in course (0.0.4).** The G-17 request-context fix ships
> with a permanent 20×100 concurrency regression suite, but the transport has
> less than one release cycle of bake-in. Prefer Spring MVC or Quarkus REST
> for production for now.

Add `@Blocking` only for blocking I/O (classic JPA/JDBC); leave it off with
reactive clients. In `framework` security mode, declare mutating reactive
prefixes via `inertia.security.reactive-csrf-paths`.
