package com.example.kitchensink.controller.crm;

import jakarta.inject.Inject;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;

import com.example.kitchensink.dto.FormValidator;
import com.example.kitchensink.dto.NoteForm;
import com.example.kitchensink.service.AuthService;
import com.example.kitchensink.service.CrmWriteService;
import io.github.dg.quarkus.inertia.api.Inertia;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

@Path("/contacts/{contactId}/notes")
@Blocking
public class NotesController {

    @Inject
    Inertia inertia;

    @Inject
    CrmWriteService write;

    @Inject
    AuthService auth;

    @Inject
    Validator validator;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Blocking
    public Uni<Object> store(@PathParam("contactId") long contactId, NoteForm form) {
        var contact = com.example.kitchensink.entity.Contact.findById(contactId);
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