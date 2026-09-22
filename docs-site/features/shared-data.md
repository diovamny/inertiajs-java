# Shared data

```java
// Spring (chainable) / Quarkus
inertia.share("appName", "CRM");
inertia.always("auth", currentUser()); // survives partial reloads
```

Shared props ride every full visit (`sharedProps` field) and form the base
of partials together with `always` props. Laravel parity: on `only`
partials, **only `always` props survive** — plain shared props are filtered.
