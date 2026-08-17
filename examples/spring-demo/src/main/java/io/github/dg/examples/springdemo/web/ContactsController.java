package io.github.dg.examples.springdemo.web;

import java.util.Map;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.dg.examples.springdemo.contact.Contact;
import io.github.dg.examples.springdemo.contact.ContactForm;
import io.github.dg.examples.springdemo.contact.ContactRepository;
import io.github.dg.spring.inertia.api.Inertia;

@Controller
@RequestMapping("/contacts")
public class ContactsController {

    private final Inertia inertia;
    private final ContactRepository contacts;

    public ContactsController(Inertia inertia, ContactRepository contacts) {
        this.inertia = inertia;
        this.contacts = contacts;
    }

    @GetMapping
    public Object index(@RequestParam(defaultValue = "") String search,
                        @RequestParam(defaultValue = "1") int page,
                        @RequestParam(defaultValue = "10") int perPage) {
        var result = contacts.findAll(search, page, perPage);
        var rows = result.data().stream()
            .map(c -> Map.<String, Object>of(
                "id", c.id(), "name", c.name(), "email", c.email(), "phone", c.phone()))
            .toList();
        inertia.merge("contacts", rows, Inertia.MergeRule.MERGE);
        return inertia.render("Contacts/Index", Map.of(
            "contacts", rows,
            "pagination", Map.of(
                "total", result.total(),
                "page", result.page(),
                "perPage", result.perPage(),
                "lastPage", result.lastPage()
            ),
            "filters", Map.of("search", search)
        ));
    }

    @GetMapping("/create")
    public Object create() {
        return inertia.render("Contacts/Create", Map.of());
    }

    @GetMapping("/{id}/edit")
    public Object edit(@PathVariable long id) {
        var contact = contacts.findById(id).orElseThrow();
        return inertia.render("Contacts/Edit", Map.of("contact", contact));
    }

    @PostMapping
    public Object store(@Valid ContactForm form) {
        contacts.save(new Contact(contacts.nextId(), form.name(), form.email(), form.phone()));
        inertia.flash("success", "Contacto creado correctamente.");
        return inertia.redirect("/contacts");
    }

    @PutMapping("/{id}")
    public Object update(@PathVariable long id, @Valid ContactForm form) {
        contacts.save(new Contact(id, form.name(), form.email(), form.phone()));
        inertia.flash("success", "Contacto actualizado correctamente.");
        return inertia.redirect("/contacts");
    }

    @DeleteMapping("/{id}")
    public Object destroy(@PathVariable long id) {
        contacts.delete(id);
        inertia.flash("success", "Contacto eliminado.");
        return inertia.redirect("/contacts");
    }
}