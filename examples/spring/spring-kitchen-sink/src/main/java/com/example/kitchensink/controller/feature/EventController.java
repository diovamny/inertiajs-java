package com.example.kitchensink.controller.feature;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kitchensink.service.Demo;
import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class EventController {

    private final Inertia inertia;

    public EventController(Inertia inertia) {
        this.inertia = inertia;
    }

    @GetMapping("/features/events/global-events")
    public Object globalEvents() {
        return inertia.render("Features/Events/GlobalEvents");
    }

    @PostMapping("/features/events/global-events/action")
    public Object globalEventsAction() {
        inertia.flash("message", "Action completed successfully!");
        return inertia.back();
    }

    @GetMapping("/features/events/once-events")
    public Object onceEvents() {
        return inertia.render("Features/Events/OnceEvents");
    }

    @PostMapping("/features/events/once-events")
    public Object onceEventsAction() {
        inertia.flash("message", "One-shot action fired!");
        return inertia.back();
    }

    @GetMapping("/features/events/visit-callbacks")
    public Object visitCallbacks() {
        return inertia.render("Features/Events/VisitCallbacks");
    }

    @PostMapping("/features/events/visit-callbacks/action")
    public Object visitCallbacksAction() {
        inertia.flash("message", "Visit callback action completed!");
        return inertia.back();
    }

    @GetMapping("/features/events/progress")
    public Object progress() {
        return inertia.render("Features/Events/Progress");
    }

    @GetMapping("/features/events/progress/slow")
    public Object progressSlow() {
        Demo.sleepSeconds(2);
        return inertia.render("Features/Events/Progress");
    }

    @GetMapping("/features/events/location-event")
    public Object locationEvent() {
        return inertia.render("Features/Events/LocationEvent");
    }

    @GetMapping("/features/events/location-event/deploy")
    public Object deployNewVersion() {
        inertia.setVersion("new-asset-version-hash");
        return inertia.back();
    }
}