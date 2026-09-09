package com.example.kitchensink.controller.feature;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class LayoutController {

    private final Inertia inertia;

    public LayoutController(Inertia inertia) {
        this.inertia = inertia;
    }

    @GetMapping("/features/layouts/persistent-layouts")
    public Object persistentLayouts() {
        return inertia.render("Features/Layouts/PersistentLayouts");
    }

    @GetMapping("/features/layouts/persistent-layouts/page-2")
    public Object persistentLayoutsPageTwo() {
        return inertia.render("Features/Layouts/PersistentLayoutsPageTwo");
    }

    @GetMapping("/features/layouts/nested-layouts")
    public Object nestedLayouts() {
        return inertia.render("Features/Layouts/NestedLayouts");
    }

    @GetMapping("/features/layouts/head")
    public Object head() {
        return inertia.render("Features/Layouts/Head");
    }

    @GetMapping("/features/layouts/layout-props")
    public Object layoutProps() {
        return inertia.render("Features/Layouts/LayoutProps");
    }
}
