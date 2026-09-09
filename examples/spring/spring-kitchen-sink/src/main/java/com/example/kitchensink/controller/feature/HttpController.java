package com.example.kitchensink.controller.feature;

import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.kitchensink.service.Demo;
import io.github.diovamny.spring.inertia.api.Inertia;

@RestController
public class HttpController {

    private final Inertia inertia;

    public HttpController(Inertia inertia) {
        this.inertia = inertia;
    }

    @GetMapping("/features/http/use-http")
    public Object useHttp() {
        return inertia.render("Features/Http/UseHttp");
    }

    @PostMapping(value = "/features/http/use-http/api", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Map<String, Object> useHttpApi(@RequestBody(required = false) Map<String, String> body) {
        var name = body != null && body.get("name") != null && !body.get("name").isBlank()
            ? body.get("name")
            : "World";
        return Map.of(
            "message", "Hello, " + name + "!",
            "timestamp", Demo.iso());
    }
}
