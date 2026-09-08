# Getting Started — Spring Boot

Minimal path from zero to your first Inertia v3 page with Spring Boot 4.1.
Requires JDK 21+, Maven 3.9+ and Node.js 22+.

## Option A — starter kit (recommended)

Generate a runnable app (Vue 3 or React 19, TypeScript, Vite, tests):

```bash
mvn -B archetype:generate \
  -DarchetypeGroupId=io.github.dg \
  -DarchetypeArtifactId=inertia-spring-vue-archetype \
  -DarchetypeVersion=0.0.1 \
  -DgroupId=com.example \
  -DartifactId=hello-inertia \
  -Dpackage=com.example.hello
```

(For React use `-DarchetypeArtifactId=inertia-spring-react-archetype`.)

```bash
cd hello-inertia
npm --prefix src/main/webui install
npm --prefix src/main/webui run dev   # terminal 1: Vite
mvn spring-boot:run                   # terminal 2: backend
```

Open `http://localhost:8080/`.

## Option B — add to an existing app

```xml
<dependency>
    <groupId>io.github.dg.spring.inertia</groupId>
    <artifactId>spring-inertia</artifactId>
    <version>0.0.1</version>
</dependency>
```

```properties
# application.properties
inertia.root-template=index.html
```

```java
@RestController
public class WelcomeController {

    private final Inertia inertia;

    public WelcomeController(Inertia inertia) {
        this.inertia = inertia;
    }

    @GetMapping("/")
    public Object welcome() {
        return inertia.render("Welcome", Map.of("appName", "Hello Inertia"));
    }
}
```

Add a root template at `src/main/resources/templates/index.html` (see the
starter kit for a copy-paste template), a Vite frontend resolving the
`Welcome` page, then `npm run build` + `mvn package`.

## Test it

```java
@SpringBootTest
@AutoConfigureMockMvc
class WelcomeControllerTest {

    @Autowired MockMvc mockMvc;

    @Test
    void initialVisitReturnsHtml() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk())
            .andExpect(content().string(containsString("id=\"app\"")));
    }

    @Test
    void inertiaVisitReturnsJson() throws Exception {
        mockMvc.perform(get("/").header("X-Inertia", "true"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Inertia", "true"));
    }
}
```

See [testing](testing-guide.md), [configuration](configuration.md) and
[protocol compatibility](protocol-compatibility.md).
