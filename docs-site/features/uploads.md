# Uploads

Multipart forms flow through CSRF and redirect like any Inertia visit:

```java
// Spring
@PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
public Object upload(@RequestParam("file") MultipartFile file) { ... }
```

```java
// Quarkus
@POST @Consumes(MULTIPART_FORM_DATA)
public Uni<Object> upload(@RestForm("file") FileUpload file) { ... }
```
