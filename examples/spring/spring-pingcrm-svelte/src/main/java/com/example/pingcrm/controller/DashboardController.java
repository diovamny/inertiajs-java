package com.example.pingcrm.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class DashboardController {

    private final Inertia inertia;

    public DashboardController(Inertia inertia) {
        this.inertia = inertia;
    }

    @GetMapping("/")
    public Object index() {
        return inertia.render("Dashboard/Index", Map.of());
    }
}
