package ${package};

import io.github.diovamny.spring.inertia.api.Inertia;
import jakarta.validation.Valid;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

/**
 * Contact CRUD demonstrating the Inertia starter contract: pages render
 * components, mutations redirect with flash data, and validation failures
 * go back with per-field errors and the submitted input.
 */
@Controller
public class ContactsController {

    private final Inertia inertia;
    private final ContactRepository repository;

    public ContactsController(Inertia inertia, ContactRepository repository) {
        this.inertia = inertia;
        this.repository = repository;
    }

    @GetMapping("/contacts")
    public Object index() {
        return inertia.render("Contacts/Index", Map.of("contacts", repository.findAll()));
    }

    @GetMapping("/contacts/create")
    public Object create() {
        return inertia.render("Contacts/Create", Map.of());
    }

    @PostMapping("/contacts")
    public Object store(@Valid ContactForm form, BindingResult result) {
        if (result.hasErrors()) {
            return inertia.back()
                .withErrors(fieldErrors(result))
                .withInput(inputOf(form));
        }
        repository.create(form);
        return inertia.redirect("/contacts")
            .withFlash("success", "Contacto creado con éxito");
    }

    @GetMapping("/contacts/{id}/edit")
    public Object edit(@PathVariable("id") long id) {
        var contact = repository.find(id);
        if (contact == null) {
            return inertia.back()
                .withFlash("error", "El contacto no existe");
        }
        return inertia.render("Contacts/Edit", Map.of("contact", contact));
    }

    @PutMapping("/contacts/{id}")
    public Object update(@PathVariable("id") long id,
            @Valid ContactForm form, BindingResult result) {
        if (result.hasErrors()) {
            return inertia.back()
                .withErrors(fieldErrors(result))
                .withInput(inputOf(form));
        }
        if (!repository.update(id, form)) {
            return inertia.redirect("/contacts")
                .withFlash("error", "El contacto no existe");
        }
        return inertia.redirect("/contacts")
            .withFlash("success", "Contacto actualizado con éxito");
    }

    @DeleteMapping("/contacts/{id}")
    public Object destroy(@PathVariable("id") long id) {
        if (!repository.delete(id)) {
            return inertia.redirect("/contacts")
                .withFlash("error", "El contacto no existe");
        }
        return inertia.redirect("/contacts")
            .withFlash("success", "Contacto eliminado con éxito");
    }

    private static Map<String, String> fieldErrors(BindingResult result) {
        var errors = new LinkedHashMap<String, String>();
        for (FieldError error : result.getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return errors;
    }

    private static Map<String, Object> inputOf(ContactForm form) {
        var input = new LinkedHashMap<String, Object>();
        input.put("name", form.name());
        input.put("email", form.email());
        input.put("phone", form.phone());
        return input;
    }
}
