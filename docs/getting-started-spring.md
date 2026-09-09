# Getting Started — Spring Boot

Minimal path from zero to your first Inertia v3 page with Spring Boot 4.1.
Requires JDK 21+, Maven 3.9+ and Node.js 22+.

## Option A — starter kit (recommended)

Generate a runnable app (Vue 3 or React 19, TypeScript, Vite, tests):

```bash
mvn -B archetype:generate \
  -DarchetypeGroupId=io.github.diovamny \
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
    <groupId>io.github.diovamny.spring.inertia</groupId>
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

## Full CRUD example — ContactsController

The same four methods serve Vue and React alike.

```java
package com.example.crm;

import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import io.github.diovamny.spring.inertia.api.Inertia;

@RestController
public class ContactsController {

    private final Inertia inertia;
    private final ContactRepository contacts;

    public ContactsController(Inertia inertia, ContactRepository contacts) {
        this.inertia = inertia;
        this.contacts = contacts;
    }

    @GetMapping("/contacts")
    public Object index() {
        // GET /contacts  →  page "Contacts/Index"
        return inertia.render("Contacts/Index",
            Map.of("contacts", contacts.findAll()));
    }

    @GetMapping("/contacts/create")
    public Object create() {
        // GET /contacts/create  →  page "Contacts/Create"
        return inertia.render("Contacts/Create",
            Map.of("organizations", organizations.findAll()));
    }

    @PostMapping(value = "/contacts", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object store(@RequestBody ContactForm form) {
        // POST /contacts  →  validate, then 303 redirect with flash data
        var contact = contacts.create(form);
        return inertia.redirect("/contacts/" + contact.id)
            .with("message", "Contact created.");
    }

    @GetMapping("/contacts/{id}/edit")
    public Object edit(@PathVariable long id) {
        // GET /contacts/{id}/edit  →  page "Contacts/Edit"
        return inertia.render("Contacts/Edit",
            Map.of("contact", contacts.findById(id)));
    }
}
```

## Vue.js pages

```vue
<!-- webui/src/pages/Contacts/Index.vue -->
<script setup lang="ts">
import { Link } from '@inertiajs/vue3'

interface Contact {
  id: number
  name: string
}

defineProps<{
  contacts: Contact[]
}>()
</script>

<template>
  <main>
    <h1>Contacts</h1>
    <Link href="/contacts/create">Create contact</Link>
    <ul>
      <li v-for="c in contacts" :key="c.id">
        {{ c.name }}
        <Link :href="`/contacts/${c.id}/edit`">Edit</Link>
      </li>
    </ul>
  </main>
</template>
```

```vue
<!-- webui/src/pages/Contacts/Create.vue -->
<script setup lang="ts">
import { Form } from '@inertiajs/vue3'
import { ref } from 'vue'

const name = ref<string>('')
</script>

<template>
  <main>
    <h1>Create contact</h1>
    <Form action="/contacts" method="post" v-slot="{ errors, processing }">
      <label for="name">Name</label>
      <input id="name" v-model="name" name="name" placeholder="Name" />
      <div v-if="errors.name">{{ errors.name }}</div>
      <button type="submit" :disabled="processing">Save</button>
    </Form>
  </main>
</template>
```

## React.js pages

```tsx
// webui/src/pages/Contacts/Index.tsx
import { Link } from '@inertiajs/react'

interface Contact {
  id: number
  name: string
}

export default function Index({ contacts }: { contacts: Contact[] }) {
  return (
    <main>
      <h1>Contacts</h1>
      <Link href="/contacts/create">Create contact</Link>
      <ul>
        {contacts.map((c) => (
          <li key={c.id}>
            {c.name} <Link href={`/contacts/${c.id}/edit`}>Edit</Link>
          </li>
        ))}
      </ul>
    </main>
  )
}
```

```tsx
// webui/src/pages/Contacts/Create.tsx
import { useForm } from '@inertiajs/react'

export default function Create() {
  const { data, setData, post, errors, processing } = useForm({ name: '' })

  return (
    <main>
      <h1>Create contact</h1>
      <form onSubmit={(e) => { e.preventDefault(); post('/contacts') }}>
        <label htmlFor="name">Name</label>
        <input
          id="name"
          value={data.name}
          onChange={(e) => setData('name', e.target.value)}
          placeholder="Name"
        />
        {errors.name && <div>{errors.name}</div>}
        <button type="submit" disabled={processing}>Save</button>
      </form>
    </main>
  )
}
```

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
