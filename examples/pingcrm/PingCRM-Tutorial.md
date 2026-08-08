# PingCRM — Tutorial ilustrado (Quarkus + Inertia.js)

> Para desarrolladores Java que **no conocen Inertia.js**.
> Tiempo de lectura: ~15 min. Stack: **Quarkus 3.38 · Vue 3 · Tailwind 4 · H2 · quarkus-inertia 0.0.1**.

PingCRM es el demo de referencia de Inertia.js: un mini-CRM (organizaciones,
contactos, usuarios, reportes) que tradicionalmente se escribe en Laravel + Vue.
Aquí lo portamos a **Quarkus** usando la librería local `quarkus-inertia`, sin
perder la fidelidad al original. Este documento reconstruye el port paso a paso,
capa por capa, con diagramas ASCII y **snippets verbatim del repositorio**.

---

## 0. TL;DR

```
┌──────────────────────────────────────────────────────────────────┐
│  Quarkus 3.38 (Java 21)        │     Vue 3.5 + Tailwind 4       │
│  ──────────────────────        │     ─────────────────────       │
│  JAX-RS controllers            │     Pages/*.vue (useForm)       │
│  Panache repos + H2 file       │     Shared/*.vue (Layout, Menu) │
│  Vert.x sessions + AuthFilter  │     composables/useSyncErrors   │
│  Flyway migrations             │     vite + @inertiajs/vite      │
│  quarkus-inertia 0.0.1 ────────┼──── @inertiajs/vue3 3.6 ──────  │
│         render("Org/Index", props)  ←→  page.props                │
└──────────────────────────────────────────────────────────────────┘
```

Como correrlo (si ya tenés JDK 21 + Maven):

```bash
mvn -q install -DskipTests                     # instala quarkus-inertia (raíz)
cd examples/pingcrm/src/main/webui && npm i && npm run build && cd ../../..
mvn quarkus:dev                                # http://localhost:8080
# login: johndoe@example.com / secret
```

---

## 1. ¿Qué es Inertia.js? El modelo "no-SPA tradicional"

Una SPA clásica tiene un backend que **solo expone JSON** y un frontend que
**rende todo**. Inertia propone un punto intermedio: **el backend decide la
página** (como en Rails/Laravel) pero **la sirve como JSON** al frontend, que ya
tiene montado un componente Vue/React. No hay router client-side ni un store
global; la navegación es *como* en una app server-rendered, solo que las
responses son JSON que Inertia monta en el componente apropiado.

```
   Browser (Vue app ya montada)
        │
        │  1. GET /organizations            (X-Inertia: true)
        ▼
   ┌────────────────────────────┐
   │  Quarkus controller        │
   │ (inertia.render(...))      │
   │  → arma PageObject JSON:   │
   │    { component, props,     │
   │      url, version }        │
   └────────────────────────────┘
        │
        │  2. 200 OK  (Content-Type: application/json)
        │     { "component":"Organizations/Index",
        │       "props":{ "organizations":{...}, "filters":{...} },
        │       "url":"/organizations", "version":"1.0.0" }
        ▼
   Inertia client → resolve('Organizations/Index')
        → monta <Organizations/Index :props=...>  (sin recargar HTML shell)
```

**Clave**: no es una API REST pública. El client **siempre** pide "una página"
(un componente + sus props), y el server siempre devuelve eso. La primera
visita (sin header `X-Inertia`) devuelve el **shell HTML** (`templates/index.html`)
con un `<script data-page>` incrustado; las visitas siguientes del client
Inertia reciben JSON directo y montan el componente **sin recargar** la página.

---

## 2. El pingcrm original vs. este port

| Aspecto                | pingcrm oficial (Laravel)       | Este port (Quarkus)                 |
| ---------------------- | ------------------------------- | ----------------------------------- |
| Backend                | PHP 8 / Laravel 11              | Java 21 / Quarkus 3.38              |
| Persistencia           | MySQL                           | H2 archivo (`./data/pingcrm`)      |
| Migraciones            | Laravel migrations              | Flyway (`db/migration/V1..V5`)     |
| ORM                    | Eloquent                        | Hibernate ORM + Panache (imperativo)|
| Sesión                 | Laravel session cookie          | Vert.x `vertx-web.session`         |
| Filtro de auth         | `auth` middleware               | `AuthFilter` (JAX-RS)               |
| SSR de imágenes        | League Glide                    | `ImagesController` (ImageIO propio)|
| Frontend               | Vue 3 + Tailwind                | **idéntico** (port 1:1)             |
| CSRF                   | sí (Laravel)                    | **no** (fetch v3 no envía X-XSRF)   |
| Validación             | FormRequest + redirects         | Bean Validation + `PrecognitionExceptionMapper` |
| Redirect con flash     | `Redirect::back()->with(...)->withErrors(...)` | `inertia.back().with(...)->withErrors(...)` (`InertiaRedirect`); igual con `inertia.redirect(url)` |

El objetivo del port es que el **frontend del pingcrm oficial corra sin
modificaciones** salvo el wiring de Inertia v3 (`useForm`, `router.*`).

---

## 3. Requisitos y arranque

```bash
# JDK 21, Maven 3.9+, Node 20.11+ / npm

# 1) instalar la librería (raíz del repo)
mvn -q install -DskipTests

# 2) (solo si cambias el frontend) reconstruir assets
cd examples/pingcrm/src/main/webui
npm install
npm run build       # → src/main/resources/META-INF/resources/assets/
cd ../../..

# 3) arrancar el backend (primer arranque: crea data/ y siembra datos demo)
mvn -q -f examples/pingcrm quarkus:dev     # http://localhost:8080
```

**Credenciales demo** (creadas por `DataSeeder`):

| Usuario             | Password | Rol   | Cuenta           |
| ------------------- | -------- | ----- | ---------------- |
| `johndoe@example.com` | `secret` | owner | Acme Corporation |

Otros datos: 100 organizaciones + 100 contactos (Faker seed 42, determinista).

---

## 4. Mapa del proyecto

```
examples/pingcrm/
├── pom.xml                                  ← dependencias (quarkus-inertia, h2, flyway…)
├── README.md
├── PingCRM-Tutorial.md                      ← este documento
│
├── src/main/resources/
│   ├── application.properties              ← config principal (H2 file, Flyway, CSRF off)
│   ├── db/migration/
│   │   ├── V1__create_accounts.sql         ← CREATE SEQUENCE accounts_seq + TABLE accounts
│   │   ├── V2__create_users.sql
│   │   ├── V3__create_organizations.sql
│   │   ├── V4__create_contacts.sql
│   │   └── V5__add_indexes.sql             ← unique(email), idx(account_id)…
│   ├── templates/index.html                ← shell HTML (Qute) para la primera visita
│   └── META-INF/resources/assets/          ← build de Vite (gitignored; npm run build)
│
├── src/main/java/com/example/pingcrm/
│   ├── entity/        Account, User, Organization, Contact
│   ├── repository/     AccountRepository, UserRepository, OrganizationRepository, ContactRepository
│   ├── service/        AuthService, OrganizationService, ContactService, UserService, Pagination
│   ├── controller/     Auth, Dashboard, Organizations, Contacts, Users, Reports, Images
│   ├── dto/            LoginForm, OrganizationForm, ContactForm, UserForm, FormValidator
│   ├── config/         AuthFilter, SessionConfig
│   └── seed/           DataSeeder
│
├── src/main/webui/                          ← frontend Vue (port del pingcrm oficial)
│   ├── package.json  vite.config.js
│   └── resources/
│       ├── css/app.css  buttons.css  form.css      ← Tailwind 4 (@theme indigo custom)
│       └── js/
│           ├── app.js  ssr.js                     ← bootstrap de Inertia
│           ├── composables/useSyncErrors.js
│           ├── Pages/Auth/Login.vue
│           ├── Pages/{Dashboard,Organizations,Contacts,Users,Reports}/*.vue
│           └── Shared/{Layout,MainMenu,FlashMessages,…}.vue
│
└── src/test/java/com/example/pingcrm/PingCrmTest.java   ← 25 tests (REST Assured)
    └── src/test/resources/application.properties        ← H2 mem, puerto 8081, seed on
```

Las **4 capas** (`entity → repository → service → controller`) son delgadas:
cada controller es fino, toda la lógica vive en services, las entidades son
structs Panache y los repos heredan helpers de `PanacheRepository`.

---

## 5. Capa 1 — Persistencia: H2 + Flyway + Panache

Cada entidad extiende `PanacheEntityBase` (no `PanacheEntity`) **para definir su
propio `@Id` con secuencia**. Hibernate 7 no pluraliza ni usa IDENTITY igual que
Laravel; aquí forzamos `@Table` + `@SequenceGenerator(allocationSize=1)`.

`entity/Organization.java` (verbatim):

```java
@Entity
@Table(name = "organizations")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Organization extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "organizationSeq",
                       sequenceName = "organizations_seq",
                       allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "organizationSeq")
    public Long id;

    public Long accountId;
    public String name;
    public String email;
    public String phone;
    public String address;
    public String city;
    public String region;
    public String country;
    public String postalCode;
    public Instant createdAt;
    public Instant updatedAt;
    public Instant deletedAt;
}
```

Diagrama de la relación entidad ↔ tabla ↔ secuencia:

```
   Java field            Column (por naming snake_case)        DB
   ──────────────        ──────────────────────────────        ───────────────
   id              ─┐    id                          ◄──┐   organizations_seq
   accountId        ├─►  account_id                     │   (CREATE SEQUENCE
   postalCode       ─►   postal_code                     │    organizations_seq
   deletedAt        ─►   deleted_at     (soft delete)     │    START WITH 1)
   createdAt        ─►   created_at                       │
                                                              │
                                                              │ nextval()
                                                              └──► id BIGINT
```

**Por qué `@JsonNaming(SnakeCaseStrategy.class)`**: el frontend Vue viene del
pingcrm Laravel, donde los props son snake_case (`postal_code`, `deleted_at`).
Esto serializa `postalCode` → `postal_code` sin tocar el field Java.

Migración V3 (resumen):

```sql
CREATE SEQUENCE organizations_seq START WITH 1;
CREATE TABLE organizations (
    id BIGINT NOT NULL PRIMARY KEY,
    account_id BIGINT NOT NULL,
    name VARCHAR(255),
    ...
    deleted_at TIMESTAMP
);
```

`allocationSize=1` ↔ la secuencia incrementa de a 1 (retro-mapping al
`INCREMENT 1` por defecto de H2). Si usás `allocationSize=50` (default JPA) la
secuencia real queda distante del siguiente `id` y los inserts en batch del
seeder chocan con `EntityExistsException`.

`application.properties` (config que afecta la persistencia):

```properties
quarkus.datasource.jdbc.url=jdbc:h2:file:./data/pingcrm
quarkus.flyway.migrate-at-start=true
quarkus.hibernate-orm.database.generation=none
quarkus.hibernate-orm.physical-naming-strategy=
    org.hibernate.boot.model.naming.CamelCaseToUnderscoresNamingStrategy
```

El `physical-naming-strategy` mapea `postalCode` → `postal_code` a nivel
**Hibernate** (para que las queries JPQL/Criteria lo encuentren). La
`@JsonNaming` hace lo mismo a nivel **JSON**.

---

## 6. Capa 2 — Sesión y `AuthFilter` (el corazón del multi-usuario)

Aquí es donde el port se separa más de Laravel. Laravel usa su middleware
`auth`; nosotros usamos **sesión Vert.x + un `ContainerRequestFilter` JAX-RS**.

### 6.1 Setup de la sesión — `config/SessionConfig.java` (verbatim)

```java
@ApplicationScoped
public class SessionConfig {

    void setupSessionHandler(@Observes Router router, Vertx vertx) {
        var store = LocalSessionStore.create(vertx);
        var sessionHandler = SessionHandler.create(store);
        sessionHandler.setLazySession(false);
        router.route().order(0).handler(sessionHandler);
    }
}
```

- `LocalSessionStore` → sesiones **en memoria** (suficiente para demo; en
  prod usarías un `SessionStore` distribuido como Redis o una BD).
- `setLazySession(false)` → crea la sesión **inmediate** en cada request
  (necesario para el `AuthFilter`, ver 6.4).
- `order(0)` → el `SessionHandler` se registra **antes** que cualquier
  handler/filter (incluido el `AuthFilter`), para que `routingContext.session()`
  siempre exista.

### 6.2 `AuthService` — quién es el usuario actual (verbatim)

```java
@RequestScoped
public class AuthService {

    static final String SESSION_USER_KEY = "pingcrm.userId";
    private User cached;
    private boolean cachedSet;

    public User currentUser() {
        if (cachedSet) {
            return cached;
        }
        cachedSet = true;
        var rc = resolve();                                  // RoutingContext
        var session = rc != null ? rc.session() : null;
        var id = session != null ? session.get(SESSION_USER_KEY) : null;
        if (!(id instanceof Long userId)) {
            cached = null;
            return null;
        }
        cached = userRepository.findById(userId);
        return cached;
    }

    public void login(User user) {
        var session = requireSession();
        session.put(SESSION_USER_KEY, user.id);
        cached = user;
        cachedSet = true;
    }

    public Long accountId() {
        var account = currentAccount();                     // findById(user.accountId)
        return account != null ? account.id : null;
    }
    // …hash/matches (PBKDF2-HMAC-SHA256, 210k iteraciones)…
}
```

**Idea**: el `userId` vive en la sesión Vert.x (clave `pingcrm.userId`). El bean
es `@RequestScoped` y cachea la resolución por request — si en la misma request
llamás `currentUser()` tres veces, solo se hace **un** `findById`.

### 6.3 ¿Soporta varios usuarios? — sí, por sesión

```
  Usuario A (Chrome)               Usuario B (Firefox)
  cookie: vertx-web.session=AAA     cookie: vertx-web.session=BBB
          │                                  │
          ▼                                  ▼
   ┌────────────────────────────────────────────────────────┐
   │ Vert.x SessionHandler                                 │
   │  LocalSessionStore                                    │
   │   session[AAA] = { pingcrm.userId: 1 }                │
   │   session[BBB] = { pingcrm.userId: 2 }                │
   └────────────────────────────────────────────────────────┘
          │                                  │
          ▼                                  ▼
   AuthService.currentUser()         AuthService.currentUser()
     → findById(1)  = John Doe         → findById(2)  = Jane Roe
```

Cada browser mantiene su propia cookie `vertx-web.session`; el id de sesión se
mapea a un diccionario en memoria que guarda `pingcrm.userId`. `currentUser()`
lo lee y hace el `findById` → **cada request resuelve a su propio usuario**.
N usuarios concurrentes, sin pisarse.

### 6.4 El filtro de auth — `config/AuthFilter.java` (verbatim, recortado)

```java
@Provider
@Priority(Priorities.AUTHORIZATION)
public class AuthFilter implements ContainerRequestFilter {

    private static final List<String> PUBLIC_PREFIXES = List.of("assets", "img", "build");
    private static final List<String> PUBLIC_EXACT = List.of("login", "logout", "favicon.svg");

    @Override
    public void filter(ContainerRequestContext context) {
        var user = auth.currentUser();
        inertia.share("auth", auth.authProps());             // (1) shared prop en TODA request

        var normalized = ...;                                // path sin "/" inicial
        if (isPublic(normalized)) return;                    // (2) /assets, /img, /login, …

        if ("login".equals(normalized) && user != null) {    // (3) ya logueado → /
            context.abortWith(Response.status(Response.Status.FOUND)
                .header("Location", "/").build());
            return;
        }
        if (user == null) {                                  // (4) guest → /login
            var isInertia = "true".equalsIgnoreCase(context.getHeaderString("X-Inertia"));
            if (isInertia && !"GET".equalsIgnoreCase(context.getMethod())) {
                context.abortWith(Response.status(Response.Status.SEE_OTHER)   // 303 + header
                    .header("Location", "/login")
                    .header("X-Inertia-Location", "/login")
                    .build());
            } else {
                context.abortWith(Response.status(Response.Status.FOUND)        // 302
                    .header("Location", "/login").build());
            }
        }
    }

    private boolean isPublic(String path) {
        if (PUBLIC_EXACT.contains(path)) return true;
        for (var prefix : PUBLIC_PREFIXES) {
            if (path.startsWith(prefix)) return true;
        }
        return false;
    }
}
```

Cuatro comportamientos clave:

1. **`inertia.share("auth", authProps())`** en **cada** request, se ejecute o no
   el resto del filtro. Así las páginas siempre reciben `auth` (con `user` o
   `null`) sin que cada controller se preocupe.
2. **Paths públicos**: `assets`, `img`, `build` (prefijos) + `login`, `logout`,
   `favicon.svg` (exactos). Eso evita loops con el login y deja servir imágenes
   sin sesión.
3. Si estás logueado y entrás a `/login` → 302 `/` (evitás ver el login de nuevo).
4. **Guest**:
   - GET (o GET Inertia) → **302** a `/login` (redirección "normal").
   - **Non-GET Inertia** (POST/PUT/DELETE con header `X-Inertia: true`) → **303**
     + `X-Inertia-Location`. El cliente Inertia sigue el `X-Inertia-Location`
     con un GET y renderiza la página de login en el modal de estado, **sin
     recargar el shell**. Esto es parte del protocolo Inertia.

### 6.5 `@Blocking` — por qué todos los controllers lo tienen

`AuthFilter` corre en el thread IO/event-loop de Vert.x. Como
`AuthService.currentUser()` hace un `findById` (consulta **bloqueante** con
Panache imperativo), cualquier endpoint sin `@Blocking` moriría con:

```
ERROR: A blocking operation occurred on the IO thread.
       Annotate …Controller#index() with @io.smallrye.common.annotation.Blocking.
```

Solución: **anotás la clase entera** con `@Blocking`. RESTEasy Reactive mueve
entonces **toda la cadena (filter + resource)** a un worker thread, donde la
consulta es legitima.

```java
@Path("/organizations")
@Blocking
public class OrganizationsController { … }
```

---

## 7. Capa 3 — Services (Criteria API + paginación "Laravel-style")

`OrganizationService.page()` es el ejemplo más claro. Lee con CriteriaBuilder,
filtra por `accountId` + opcional `search` + `trashed`, pagina y devuelve el
payload en el formato que espera el frontend.

`service/OrganizationService.java` (verbatim, recortado):

```java
public Pagination.Result page(Long accountId, String search, String trashed,
                              int page, int size) {
    var em = organizationRepository.getEntityManager();
    var cb = em.getCriteriaBuilder();

    var cq = cb.createQuery(Organization.class);
    var root = cq.from(Organization.class);
    var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
    predicates.add(cb.equal(root.get("accountId"), accountId));     // scope por cuenta
    applyFilters(cb, root, predicates, search, trashed);            // search + trash
    cq.where(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    cq.orderBy(cb.asc(root.get("name")));

    var query = em.createQuery(cq);
    query.setFirstResult((page - 1) * size);
    query.setMaxResults(size);
    var items = query.getResultList();

    var countCq = cb.createQuery(Long.class);
    var countRoot = countCq.from(Organization.class);
    var countPredicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
    countPredicates.add(cb.equal(countRoot.get("accountId"), accountId));
    applyFilters(cb, countRoot, countPredicates, search, trashed);
    countCq.where(countPredicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    countCq.select(cb.count(countRoot));
    var total = em.createQuery(countCq).getSingleResult();

    var filters = new LinkedHashMap<String, Object>();
    filters.put("search", search);
    filters.put("trashed", trashed);
    return Pagination.of("/organizations",
        items.stream().map(this::toListItem).toList(),
        total, page, size, filters);
}
```

El payload que el frontend recibe en `props.organizations`:

```
props.organizations =
{
  "data": [
    { "id": 12, "name": "Acme Ltd", "phone": "…", "city": "…", "deleted_at": null },
    { "id": 13, "name": "…",        "phone": "…", "city": "…", "deleted_at": null },
    … 10 items …
  ],
  "links": [
    { "url": null,                "label": "« Previous", "active": false },
    { "url": "/organizations?page=1", "label": "1",     "active": true  },
    { "url": "/organizations?page=2", "label": "2",     "active": false },
    { "url": null,                "label": "…",         "active": false },   // ventana
    { "url": "/organizations?page=10","label": "10",   "active": false },
    { "url": "/organizations?page=2", "label": "Next »","active": false }
  ]
}
```

`Pagination.of()` produce esa estructura (`Pagination.java` verbatim):

```java
public record Result(List<Object> data, List<Map<String, Object>> links) {}

public static Result of(String path, List<?> items, long total,
                        int page, int size, Map<String, Object> query) {
    var totalPages = Math.max(1, (int) Math.ceil(total / (double) size));
    var safePage   = Math.min(Math.max(1, page), totalPages);

    var links = new ArrayList<Map<String, Object>>();
    links.add(link(url(path, safePage - 1, query), "&laquo; Previous", false));

    for (var p : window(safePage, totalPages)) {     // ventana max 9 + ellipsis (-1)
        if (p == -1L) {
            links.add(link(null, "...", false));
        } else {
            links.add(link(url(path, p, query), String.valueOf(p), p == safePage));
        }
    }

    links.add(link(url(path, safePage + 1, query), "Next &raquo;", false));
    return new Result(new ArrayList<>(items), links);
}
```

`ContactService.page()` (similar) agrega un **subquery EXISTS** para buscar
contactos cuyo `organization.name` matchee el `search`:

```java
Subquery<Long> orgSub = cb.createQuery().subquery(Long.class);
var orgRoot = orgSub.from(Organization.class);
orgSub.select(orgRoot.get("id"));
orgSub.where(cb.and(
    cb.equal(orgRoot.get("accountId"), root.get("accountId")),
    cb.like(cb.lower(orgRoot.get("name")), pattern)));
predicates.add(cb.or(
    cb.like(cb.lower(root.get("firstName")), pattern),
    cb.like(cb.lower(root.get("lastName")),  pattern),
    cb.like(cb.lower(root.get("email")),     pattern),
    cb.exists(orgSub)));
```

---

## 8. Capa 4 — Controllers (`Uni<Object>` + `@Blocking` + `inertia.render`)

Los controllers son **fachadas delgadas**: validar el path, delegar al service, devolver
una "página". Tres pieces no triviales:

1. Devuelven `Uni<Object>` (Mutiny reactive) — el tipo que devuelve
   `inertia.render(...)`. RESTEasy Reactive se encarga de subscribirse.
2. Llevan `@Blocking` (a nivel clase, ya lo vimos) porque leen la sesión y/o BD.
3. `inertia.render(component, props)` arma el PageObject JSON.

`controller/OrganizationsController.java` (verbatim, recortado):

```java
@Path("/organizations")
@Blocking
public class OrganizationsController {

    @Inject Inertia inertia;
    @Inject AuthService auth;
    @Inject OrganizationService organizations;
    @Inject Validator validator;

    @GET
    public Uni<Object> index(@QueryParam("search") String search,
                             @QueryParam("trashed") String trashed,
                             @QueryParam("page") @DefaultValue("1") int page) {
        var accountId = auth.accountId();
        var result = organizations.page(accountId, search, trashed, page, 10);
        return inertia.render("Organizations/Index", Map.of(
            "filters",      filters(search, trashed),
            "organizations", result));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Object> store(OrganizationForm form) {
        normalize(form);
        FormValidator.validate(validator, form);             // Bean Validation
        organizations.create(auth.accountId(), toValues(form));
        return inertia.redirect("/organizations").with("success", "Organization created."); // 303 en non-GET
    }

    @PUT
    @Path("{id}")
    public Uni<Object> update(@PathParam("id") long id, OrganizationForm form) {
        var organization = findOwned(id);
        if (organization == null) return notFound();
        normalize(form);
        FormValidator.validate(validator, form);
        organizations.update(organization, toValues(form));
        return inertia.back().with("success", "Organization updated."); // 303 al Referer + flash
    }
    // … destroy, restore, edit …
}
```

Observaciones:

- **`@QueryParam("search")` sin `@DefaultValue`**: llega como `null` si no está
  → `applyFilters` no agrega el predicate. Por eso `filters.put("search", null)`
  necesita un `LinkedHashMap` (no `Map.of`, que rechaza nulls) — fue un bug real
  del port.
- **`inertia.flash("success", ...)`** persiste un mensaje en la sesión que se
  consume en la siguiente request (drained por `PageObjectBuilder`).
- **`inertia.redirect()`** devuelve 303 si el request es non-GET (estándar
  Inertia), 302 si es GET.
- **`inertia.back()`** usa el header `Referer` o un fallback — útil después de
  PUT/DELETE para volver al form. Tanto `back()` como `redirect()` devuelven un
  `InertiaRedirect` (que *es* un `Uni`), por lo que admiten encadenado estilo
  Laravel sin perder compatibilidad:
  `inertia.back().with("success", ...).withErrors(...)` o
  `inertia.redirect("/x").with("success", ...)`.

El demo user está "protegido" en `UsersController` con un guard simple:

```java
if (user.isDemoUser()) {
    return inertia.back().with("error", "Updating the demo user is not allowed.");
}
```

`User.isDemoUser()` viene del email sembrado:

```java
public boolean isDemoUser() {
    return "johndoe@example.com".equals(email);
}
```

---

## 9. El puente: cómo el server genera el JSON de página

El `PageObject` que Inertia client espera es:

```
{
  "component": "Organizations/Index",
  "props":
  {
    "auth":     { "user": { "id":1, "first_name":"John", "owner":true, "account":{...} } },
    "filters":  { "search": null, "trashed": null },
    "organizations": { "data":[ … ], "links":[ … ] }
  },
  "url":     "/organizations",
  "version": "1.0.0"
}
```

`quarkus-inertia` lo construye dentro de su `PageObjectBuilder`, lo serializa a
JSON y decide qué devolver según la request:

- **Primera visita** (sin `X-Inertia`): renderiza el shell HTML
  `templates/index.html` incrustando el PageObject en
  `<script type="application/json" data-page="app">{dataPage}</script>`.
- **Visitas Inertia** (`X-Inertia: true`): devuelve **solo el JSON** con
  `Content-Type: application/json`, `Vary: X-Inertia` y `X-Inertia-Version`.

`templates/index.html` (verbatim):

```html
<!DOCTYPE html>
<html class="h-full bg-gray-100">
<head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <link rel="icon" type="image/svg+xml" href="/favicon.svg" />
    <title>{#if pageTitle}{pageTitle}{#else}Ping CRM{/if}</title>
    {#if ssrHead}{ssrHead}{/if}
    <link rel="stylesheet" href="/assets/app.css" />
</head>
<body class="font-sans leading-none text-gray-700 antialiased">
    <div id="app" {#if ssrBody}data-server-rendered="true"{/if}>{#if ssrBody}{ssrBody}{/if}</div>
    <script type="application/json" data-page="app">{dataPage}</script>
    <script src="/assets/app.js"></script>
</body>
</html>
```

Y `webui/resources/js/app.js` (verbatim) levanta Inertia:

```javascript
import '../css/app.css'
import { createApp, h } from 'vue'
import { createInertiaApp } from '@inertiajs/vue3'

createInertiaApp({
  resolve: name => {
    const pages = import.meta.glob('./Pages/**/*.vue', { eager: true })   // (1)
    return pages[`./Pages/${name}.vue`]
  },
  title: title => title ? `${title} - Ping CRM` : 'Ping CRM',
  setup({ el, App, props, plugin }) {
    createApp({ render: () => h(App, props) })
      .use(plugin)
      .mount(el)
  },
})
```

1. `import.meta.glob('./Pages/**/*.vue', { eager: true })` — Vite importa
   **todos** los `.vue` bajo `Pages/`. El server manda `component: "Auth/Login"`
   → el client busca `./Pages/Auth/Login.vue` en ese mapa. **No hay router
   client-side**: la "ruta" es el `component` que manda el server.

`vite.config.js` (verbatim) decide dónde cae el build:

```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import inertia from '@inertiajs/vite'
import tailwindcss from '@tailwindcss/vite'
import path from 'path'

const root = import.meta.dirname

export default defineConfig({
  plugins: [vue(), inertia(), tailwindcss()],
  resolve: {
    alias: { '@': path.resolve(root, 'resources/js') },
  },
  build: {
    outDir: path.resolve(root, '../../main/resources/META-INF/resources'),
    emptyOutDir: true,
    rollupOptions: {
      input: path.resolve(root, 'resources/js/app.js'),
      output: {
        entryFileNames:  'assets/[name].js',
        chunkFileNames:  'assets/[name].js',
        assetFileNames:  'assets/[name][extname]'
      }
    }
  },
  server: { port: 5173, strictPort: false }
})
```

El `outDir` cae **dentro** de `src/main/resources/META-INF/resources/`, así el
`quarkus:dev` los sirve como assets estáticos en `/assets/app.js` y
`/assets/app.css`.

---

## 10. Frontend Vue 3 — `useForm`, `router`, `useSyncErrors`

### 10.1 Login — `Pages/Auth/Login.vue`

```vue
<script>
import { Head, useForm } from '@inertiajs/vue3'
import Logo from '@/Shared/Logo.vue'
import TextInput from '@/Shared/TextInput.vue'
import LoadingButton from '@/Shared/LoadingButton.vue'
import { useSyncErrors } from '@/composables/useSyncErrors'

export default {
  components: { Head, LoadingButton, Logo, TextInput },
  setup() {
    const form = useForm({
      email: 'johndoe@example.com',
      password: 'secret',
      remember: false,
    })
    useSyncErrors(form)
    return { form }
  },
  methods: {
    login() {
      this.form.post('/login')
    },
  },
}
</script>
```

`useForm` Inertia v3 crea un objeto reactivo con `errors`, `processing`,
`reset()`, `post/put/delete`/… que **auto-tracks** el estado del envío. El
template bindea `:error="form.errors.email"` y `:loading="form.processing"`:

```vue
<text-input v-model="form.email" :error="form.errors.email" …/>
<text-input v-model="form.password" :error="form.errors.password" …/>
<loading-button :loading="form.processing" class="btn-indigo" type="submit">
  Login
</loading-button>
```

### 10.2 `useSyncErrors.js` — por qué existe

`quarkus-inertia` maneja los errores de validación con el patrón **Laravel**:
**redirect back** + flash `errors`. Con fetch Inertia v3, eso significa:

```
POST /organizations       (form inválido)
  → ConstraintViolationException
  → PrecognitionExceptionMapper
  → inertia.flash("errors", { name:"required", email:"invalid" })
  → 303 → Referer                              ← redirect back
GET /organizations/create
  → props.errors = { name:"required", email:"invalid" }   ← flash drenada
```

El `useForm` de Vue no se entera solo de esos errores (son props top-level de la
**página siguiente**, no en la response del POST). `useSyncErrors` los fusiona:

```javascript
import { watch } from 'vue'
import { usePage } from '@inertiajs/vue3'

/**
 * The server reports validation failures as flash errors that land in
 * top-level `props.errors` after a 303 redirect. This keeps them in sync
 * with a `useForm` instance so fields display their `form.errors`.
 */
export function useSyncErrors(form) {
  const page = usePage()
  watch(
    () => page.props.errors,
    (errors) => {
      if (!errors) return
      for (const [key, value] of Object.entries(errors)) {
        if (typeof value === 'string' && value !== '') {
          form.setError(key, value)
        }
      }
    },
    { immediate: true },
  )
}
```

Cada página Create/Edit lo llama en `setup()`: `useSyncErrors(form)`.

### 10.3 Index — búsqueda "en vivo" con `throttle` + `pickBy`

`Pages/Organizations/Index.vue` (verbatim):

```vue
<script>
import { Head, Link, router } from '@inertiajs/vue3'
import pickBy from 'lodash/pickBy'
import throttle from 'lodash/throttle'
import mapValues from 'lodash/mapValues'
import Pagination from '@/Shared/Pagination.vue'
import SearchFilter from '@/Shared/SearchFilter.vue'

export default {
  components: { Head, Link, Pagination, SearchFilter },
  layout: Layout,
  props: { filters: Object, organizations: Object },
  data() {
    return {
      form: {
        search: this.filters.search,
        trashed: this.filters.trashed,
      },
    }
  },
  watch: {
    form: {
      deep: true,
      handler: throttle(function () {
        router.get('/organizations', pickBy(this.form), { preserveState: true })
      }, 150),
    },
  },
  methods: {
    reset() { this.form = mapValues(this.form, () => null) },
  },
}
</script>
```

Cuando `form.search` o `form.trashed` cambian, se dispara (con **throttle de
150ms**) un `router.get('/organizations', pickBy(form), { preserveState: true })`.
`pickBy` descarta claves `null` para no ensuciar la URL. `preserveState: true`
evita "rescroll" y mantiene los inputs focuseados.

### 10.4 Edit — `put`, `destroy`, `restore`

`Pages/Organizations/Edit.vue` (verbatim, recortado):

```vue
<script>
import { Link, Head, useForm } from '@inertiajs/vue3'
import { useSyncErrors } from '@/composables/useSyncErrors'

export default {
  layout: Layout,
  props: { filters: Object, organization: Object },
  setup() {
    const form = useForm({ /* campos… */ })
    useSyncErrors(form)
    return { form }
  },
  methods: {
    submit()   { this.form.put (`/organizations/${this.organization.id}`) },
    destroy()  { if (confirm('Delete this organization?'))
                   this.$inertia.delete(`/organizations/${this.organization.id}`) },
    restore()  { if (confirm('Restore this organization?'))
                   this.$inertia.put(`/organizations/${this.organization.id}/restore`) },
  },
}
</script>
```

`this.$inertia.<verb>` y `router.<verb>` son equivalentes (v3).

### 10.5 `Shared/FlashMessages.vue` — el banner de success/error

```vue
<template>
  <div>
    <div v-if="$page.props.success && show" class="… bg-green-500 …">
      <div class="py-4 text-white text-sm font-medium">{{ $page.props.success }}</div>
      <button @click="show = false">×</button>
    </div>
    <div v-if="($page.props.error || Object.keys($page.props.errors).length > 0) && show"
         class="… bg-red-500 …">
      <div v-if="$page.props.error">{{ $page.props.error }}</div>
      <div v-else>
        <span v-if="Object.keys($page.props.errors).length === 1">There is one form error.</span>
        <span v-else>There are {{ Object.keys($page.props.errors).length }} form errors.</span>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() { return { show: true } },
  watch: {
    '$page.props': {                  // watcher string-path, deep
      handler() { this.show = true },
      deep: true,
    },
  },
}
</script>
```

**Por qué el watch**: cuando la página cambia (por ej., navegás a otra
sección y de vuelta), el banner debe resetearse a visible. El watcher deep
sobre `$page.props` reinicia `show = true` solo cuando *algo* prop cambia.

---

## 11. Flujo completo de un POST de creación

Diagrama de secuencia del `POST /organizations`:

```
 Browser                Inertia client              Quarkus / AuthFilter      OrganizationsController
   │                         │                              │                           │
   │ click "Create"          │                              │                           │
   │ POST /organizations     │                              │                           │
   │ X-Inertia: true         │                              │                           │
   │ Referer: /…/create      │                              │                           │
   │ body: { name, email,… } │                              │                           │
   │ ─────────────────────────┼─────────────────────────────►│                           │
   │                         │                              │ share("auth", authProps) │
   │                         │                              │ path no público          │
   │                         │                              │ user != null            ─►│ (delegate to worker thread)
   │                         │                              │                           │
   │                         │                              │                           │ store(form)
   │                         │                              │                           │  normalize → FormValidator.validate
   │                         │                              │                           │  organizations.create(accountId, …)
   │                         │                              │                           │  inertia.flash("success", "Organization created.")
   │                         │                              │                           │  return inertia.redirect("/organizations")   → 303
   │                         │                              │                           │ ◄────────────────────────
   │                         │  303 SEE_OTHER               │                           │
   │                         │  Location: /organizations    │                           │
   │                         │  ◄────────────────────────────┤                           │
   │                         │                              │                           │
   │   Inertia sigue la 303  │                              │                           │
   │   con un GET            │                              │                           │
   │                         │  GET /organizations           │                           │
   │                         │  X-Inertia: true              │                           │
   │                         │  ─────────────────────────────►│ share("auth", authProps) │
   │                         │                              │ index(search,trashed,page)│
   │                         │                              │  organizations.page(…, 10)│
   │                         │                              │  flash drenada → props.success = "Organization created."
   │                         │                              │  ◄─ render("Organizations/Index", { filters, organizations })
   │                         │  200 OK  application/json     │                           │
   │                         │  Vary: X-Inertia              │                           │
   │                         │  ◄────────────────────────────┤                           │
   │                         │ { component, props:{ success:"…", organizations }, url, version }
   │ monta <Organizations/Index>                           │                           │
   │ ← banner verde "Organization created."                │                           │
```

**Por qué 303 y no 302**: con fetch, una 302 a un POST queda como POST; Inertia
quiere la semántica "PRG" (Post-Redirect-Get). 303 SEE_OTHER fuerza el
siguiente fetch a GET, que es lo que Inertia clientdrivea. (El
`PrecognitionExceptionMapper` también devuelve 303 en non-GET para validación,
paridad con `RedirectProcessor`.)

---

## 12. Imágenes (subclass + serving con resize/crop)

El pingcrm original usa **League Glide** (PHP). Aquí se reemplaza por un
controller propio: `ImagesController`.

`controller/ImagesController.java` (verbatim, recortado):

```java
@Path("/img")
public class ImagesController {

    @ConfigProperty(name = "pingcrm.images.dir", defaultValue = "./data/images")
    String imagesDir;

    @GET
    @Path("{path:.*}")
    @Blocking
    public Response show(@PathParam("path") String path,
                         @QueryParam("w") Integer width,
                         @QueryParam("h") Integer height,
                         @QueryParam("fit") String fit) throws IOException {
        var base   = java.nio.file.Path.of(imagesDir).toAbsolutePath().normalize();
        var target = base.resolve(path).normalize();
        if (!target.startsWith(base) || !Files.isRegularFile(target)) {   // (1) path-traversal guard
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        var bytes = Files.readAllBytes(target);
        var isSvg = path.toLowerCase().endsWith(".svg");
        if (width != null && height != null && width > 0 && height > 0 && !isSvg) {
            var resized = resize(bytes, width, height, "crop".equalsIgnoreCase(fit));
            if (resized != null) bytes = resized;
        }

        var mediaType = Files.probeContentType(target);
        if (mediaType == null) mediaType = MediaType.APPLICATION_OCTET_STREAM;
        return Response.ok(bytes, mediaType)
            .header("Cache-Control", "public, max-age=31536000, immutable")
            .build();
    }
}
```

El **path-traversal guard**: `base` y `target` están ambos `.normalize()`'d,
después `target.startsWith(base)` rechaza cualquier cosa que escape del
directorio de imágenes (por ej., `/img/../application.properties` → 404):

```
   imagesDir = ./data/images
   base    = /app/data/images                (normalize quita el . )
   path    = ../application.properties
   target = /app/data/images/../application.properties
          → normalize → /app/data/application.properties
          → startsWith("/app/data/images") = FALSE  → 404  ✓
```

La URL del avatar la construye `UserService.toListItem`:

```java
item.put("photo", user.photoPath != null
    ? "/img/" + user.photoPath + "?w=40&h=40&fit=crop" : null);
```

…y la subida del form (`UsersController.savePhoto`) guarda el archivo con un
UUID en `data/images/users/{uuid}.{ext}` (`jpg|jpeg|png|gif|webp`), siempre
`.normalize()` + `startsWith` antes de `Files.copy`.

---

## 13. Tests — cómo se "simula un browser" con REST Assured

`PingCrmTest.java` (25 tests ordenados). Trucos no obvios:

```java
@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PingCrmTest {

    @BeforeAll
    static void configure() {
        RestAssured.config = RestAssured.config()
            .redirect(RedirectConfig.redirectConfig().followRedirects(false));   // (1)
    }

    private static RequestSpecification authedInertia() {
        return given().cookie("vertx-web.session", session)                     // (2)
                     .header("X-Inertia", "true");                              // (3)
    }

    private static void captureSession(ExtractableResponse<Response> res) {
        var cookie = res.detailedCookies().get("vertx-web.session");
        if (cookie != null) session = cookie.getValue();                        // (4)
    }
    // …
}
```

1. **`followRedirects(false)`**: REST Assured por defecto sigue 302/303
   silenciosamente, lo que oculta los comportamientos a testear (¿devolvió 303?
   ¿con qué Location?).
2. La cookie `vertx-web.session` se captura del primer login y se reenvía con
   `authed()` para simular un browser logueado.
3. El header `X-Inertia: true` flipa el server a modo JSON.
4. **Tolerante**: Vert.x solo manda `Set-Cookie` si la sesión cambió, no en
   cada response. La versión tolerante no pisa una sesión válida con null.

Ejemplo (login → dashboard):

```java
@Test @Order(5)
void loginWithValidCredentialsSucceeds() {
    var res = inertia().contentType(ContentType.JSON)
        .body("{\"email\":\"johndoe@example.com\",\"password\":\"secret\"}")
        .when().post("/login")
        .then().statusCode(303).and().header("Location", equalTo("/")).extract();
    captureSession(res);

    authedInertia().when().get("/")
        .then().statusCode(200)
        .and().contentType(ContentType.JSON)
        .and().body("component", equalTo("Dashboard/Index"))
        .and().body("props.auth.user.email", equalTo("johndoe@example.com"))
        .and().body("props.auth.user.account.name", equalTo("Acme Corporation"));
}
```

El `application.properties` de test:

```properties
quarkus.datasource.jdbc.url=jdbc:h2:mem:pingcrmtest;DB_CLOSE_DELAY=-1
quarkus.http.test-port=8081
inertia.csrf-enabled=false
pingcrm.seed.enabled=true            # el seeder corre en cada JVM de tests
pingcrm.images.dir=./target/test-images
```

`DB_CLOSE_DELAY=-1` mantiene la H2 en memoria durante toda la JVM de tests
(el seeder solo siembra una vez por arranque, guardado por el guard
`accountCount > 0`).

---

## 14. Decisiones de port clave (resumen)

| Decisión                  | Por qué                                                                                |
| ------------------------- | -------------------------------------------------------------------------------------- |
| H2 archivo (`./data`)     | Cero dependencias externas vs MySQL del original.                                      |
| `PanacheEntityBase`       | Permitir `@Id` propio + `@SequenceGenerator`; `PanacheEntity` no te deja redefinir id.  |
| `@Table(name=plural)`     | Hibernate 7 **no pluraliza** el nombre de entidad → hay que explicitarlo.               |
| `allocationSize=1`        | Matchorea el `INCREMENT 1` default de H2 y evita `EntityExistsException` en batch.     |
| `CamelCaseToUnderscores…` | Hibernate mapea `postalCode` → `postal_code` sin tocar el field Java.                  |
| Sesiones Vert.x locales   | Demo single-node; en prod usarías Redis/JDBC session store.                            |
| `@Blocking` en controllers | `AuthFilter` consulta BD en `authProps()` → no puede correr en el event-loop.          |
| `merge()` en updates      | Las entidades llegan detached (read sin tx) → `persist()` no, `merge()` sí.              |
| `(?2 is null or id<>?2)`  | JPQL `<> null` evalúa a UNKNOWN → `emailExistsForOtherUser` fallaba.                     |
| CSRF off                  | El client fetch de Inertia v3 no manda automáticamente `X-XSRF-TOKEN`.                 |
| 303 para validación       | `PrecognitionExceptionMapper` ahora devuelve 303 en non-GET, paridad con `RedirectProcessor`. |

---

## 15. Qué explorar después

Puntos extensibles / ejercicios sugeridos:

1. **Agregar una entidad nueva** (ej., `Project`): crea migration V6, entity,
   repo, service, controller + 2 páginas Vue under `Pages/Projects/`. Mirá
   `OrganizationsController` como plantilla.
2. **Multi-tenant real**: hoy `accountId` se hardcodea por una sola cuenta
   sembrada; agregá `signup` + creación de `Account` por usuario owner.
3. **SSR real**: `webui/resources/js/ssr.js` ya existe; activá el handler de
   Inertia y cambiá el template para que rende HTML en el server (mejora SEO
   y primer paint).
4. **Session store distribuida**: cambiá `LocalSessionStore` por
   `RedisSessionStore` o uno JDBC; correción mínima en `SessionConfig`.
5. **Live HMR**: con `mvn quarkus:dev` + `vite dev` (con el proxy de Vite al
   8080) podés editar Vue y ver cambios sin rebuild. Ajustá el `server.proxy`
   en `vite.config.js`.
6. **API pública JSON** (no-Inertia): un subset de endpoints sin `X-Inertia`
   para consumo externo — útil para entender por qué Inertia no es "una API
   REST más".
7. **Compará con `examples/demo-app`**: el demo-app de `quarkus-inertia` usa
   Reactivo puro (sin `@Blocking`) + CSRF activado — buen contrapunto para
   entender las decisiones de cada stack.

---

### Referencias rápidas

- **README**: `examples/pingcrm/README.md` (credenciales, comandos, decisiones).
- **Plan de ejecución**: `Prompt/Plan/PingCRM.md`.
- **Demo original (Laravel) live**: https://pingcrm.inertiajs.com/
- **Docs Inertia.js v3**: https://inertiajs.com/
- **Quarkus**: https://quarkus.io/

---

*Documento generado a partir del código real del repositorio. Todos los snippets
son verbatim de los archivos indicados.*
