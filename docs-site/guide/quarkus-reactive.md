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

> Quarkus Reactive Routes is a stable target in `0.0.5`: same contract as
> Spring MVC and Quarkus REST. The G-17 request-context fix ships with a
> permanent 20×100 concurrency regression suite plus the `reactive-stress`
> profile and the 9-cell E2E matrix (see Compatibility policy).

Add `@Blocking` only for blocking I/O (classic JPA/JDBC); leave it off with
reactive clients. In `framework` security mode, declare mutating reactive
prefixes via `inertia.security.reactive-csrf-paths`.
