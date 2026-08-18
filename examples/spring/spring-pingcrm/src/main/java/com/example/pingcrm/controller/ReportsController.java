package com.example.pingcrm.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class ReportsController {

    private final Inertia inertia;

    public ReportsController(Inertia inertia) {
        this.inertia = inertia;
    }

    @GetMapping("/reports")
    public Object index() {
        return inertia.render("Reports/Index", Map.of());
    }
}