# Getting Started — Quarkus

Minimal path from zero to your first Inertia v3 page with Quarkus 3.38 (reactive).
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
