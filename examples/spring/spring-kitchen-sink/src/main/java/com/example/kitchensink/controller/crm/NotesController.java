package com.example.kitchensink.controller.crm;

import jakarta.validation.Validator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.kitchensink.dto.FormValidator;
import com.example.kitchensink.dto.NoteForm;
import com.example.kitchensink.entity.Contact;
import com.example.kitchensink.repository.ContactRepository;
import com.example.kitchensink.service.AuthService;
import com.example.kitchensink.service.CrmWriteService;
import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class NotesController {

    private final Inertia inertia;
    private final ContactRepository contacts;
    private final CrmWriteService write;
    private final AuthService auth;
    private final Validator validator;

    public NotesController(Inertia inertia, ContactRepository contacts, CrmWriteService write,
            AuthService auth, Validator validator) {
        this.inertia = inertia;
        this.contacts = contacts;
        this.write = write;
        this.auth = auth;
        this.validator = validator;
    }

    @PostMapping(value = "/contacts/{contactId}/notes", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object store(@PathVariable("contactId") long contactId, @RequestBody NoteForm form) {
        var contact = contacts.findById(contactId).orElse(null);
        if (contact == null) {
            return inertia.redirect("/contacts").with("message", "Contact not found.");
        }
        form.body = FormValidator.blankToNull(form.body);
        FormValidator.validate(validator, form);
        var user = auth.currentUser();
        write.createNote(contactId, user != null ? user.id : null, form.body);
        return inertia.back().with("message", "Note added.");
    }
}
