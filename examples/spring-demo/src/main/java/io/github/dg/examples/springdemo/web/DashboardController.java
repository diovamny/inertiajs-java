package io.github.dg.examples.springdemo.web;

import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import io.github.dg.examples.springdemo.contact.ContactRepository;
import io.github.dg.spring.inertia.api.Inertia;

@Controller
public class DashboardController {

    private final Inertia inertia;
    private final ContactRepository contacts;

    public DashboardController(Inertia inertia, ContactRepository contacts) {
        this.inertia = inertia;
        this.contacts = contacts;
    }

    @GetMapping("/")
    public Object dashboard() {
        inertia.deferred("dashboard", "monthlyStats", this::monthlyStats);
        var welcome = inertia.once("welcome", Map.of(
            "title", "Bienvenido a spring-demo",
            "message", "Esta es una demo de Inertia.js v3 con el adaptador Spring Boot."
        ));
        return inertia.render("Dashboard", Map.of(
            "stats", Map.of(
                "contacts", contacts.count(),
                "groups", 3,
                "notes", 12
            ),
            "welcome", welcome
        ));
    }

    private Object monthlyStats() {
        return Map.of("visits", 1234, "growth", 12.5, "topPage", "/contacts");
    }
}