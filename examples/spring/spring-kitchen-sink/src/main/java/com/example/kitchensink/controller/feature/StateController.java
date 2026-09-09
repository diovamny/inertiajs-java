package com.example.kitchensink.controller.feature;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.diovamny.spring.inertia.api.Inertia;

@RestController
public class StateController {

    private final Inertia inertia;

    public StateController(Inertia inertia) {
        this.inertia = inertia;
    }

    @GetMapping("/features/state/remember")
    public Object remember() {
        return inertia.render("Features/State/Remember");
    }

    @GetMapping("/features/state/flash-data")
    public Object flashData() {
        return inertia.render("Features/State/FlashData");
    }

    @PostMapping("/features/state/flash-data")
    public Object storeFlashData() {
        inertia.flash("message", "This is a flash message from the server!");
        inertia.flash("type", "success");
        return inertia.back();
    }

    @PostMapping("/features/state/flash-data/error")
    public Object storeFlashDataError() {
        inertia.flash("message", "Something went wrong!");
        inertia.flash("type", "error");
        return inertia.back();
    }

    @PostMapping("/features/state/flash-data/warning")
    public Object storeFlashDataWarning() {
        inertia.flash("message", "Please check your input.");
        inertia.flash("type", "warning");
        return inertia.back();
    }

    @GetMapping("/features/state/shared-props")
    public Object sharedProps() {
        return inertia.render("Features/State/SharedProps");
    }
}
