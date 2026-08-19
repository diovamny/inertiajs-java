package com.example.kitchensink.controller.feature;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.validation.Validator;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.kitchensink.dto.DottedKeysRequest;
import com.example.kitchensink.dto.FormValidator;
import com.example.kitchensink.dto.PrecognitionRequest;
import com.example.kitchensink.dto.SimpleFormRequest;
import com.example.kitchensink.dto.ValidationErrorBagRequest;
import com.example.kitchensink.dto.ValidationRequest;
import com.example.kitchensink.entity.Contact;
import com.example.kitchensink.repository.ContactRepository;
import com.example.kitchensink.service.CrmQueryService;
import com.example.kitchensink.service.CrmWriteService;
import com.example.kitchensink.service.Demo;
import com.example.kitchensink.service.Resources;
import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class FormController {

    private static final Set<String> PRECOGNITION_FIELDS =
        Set.of("username", "email", "password", "password_confirmation");

    private final Inertia inertia;
    private final ContactRepository contactRepository;
    private final CrmQueryService query;
    private final CrmWriteService write;
    private final Validator validator;

    public FormController(Inertia inertia, ContactRepository contactRepository,
            CrmQueryService query, CrmWriteService write, Validator validator) {
        this.inertia = inertia;
        this.contactRepository = contactRepository;
        this.query = query;
        this.write = write;
        this.validator = validator;
    }

    @GetMapping("/features/forms/use-form")
    public Object useForm() {
        return inertia.render("Features/Forms/UseForm", null);
    }

    @PostMapping(value = "/features/forms/use-form", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object submitUseForm(@RequestBody SimpleFormRequest form) {
        validateSimple(form);
        return inertia.back().with("message", "Form submitted successfully! Name: " + form.name);
    }

    @GetMapping("/features/forms/form-component")
    public Object formComponent() {
        return inertia.render("Features/Forms/FormComponent", null);
    }

    @PostMapping(value = "/features/forms/form-component", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object submitFormComponent(@RequestBody SimpleFormRequest form) {
        validateSimple(form);
        return inertia.back().with("message", "Form submitted successfully! Name: " + form.name);
    }

    @GetMapping("/features/forms/file-uploads")
    public Object fileUploads() {
        return inertia.render("Features/Forms/FileUploads", null);
    }

    @PostMapping(value = "/features/forms/file-uploads", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object submitFileUploads(
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            @RequestPart(value = "photo", required = false) MultipartFile photo) {
        var fileList = files == null ? new MultipartFile[0] : files;
        if (fileList.length > 5) {
            return inertia.back().withErrors(Map.of(
                "files", "The files field must not have more than 5 items."));
        }
        var count = fileList.length + (photo != null ? 1 : 0);
        return inertia.back().with("message", "Uploaded " + count + " file(s) successfully!");
    }

    @GetMapping("/features/forms/validation")
    public Object validation() {
        return inertia.render("Features/Forms/Validation", null);
    }

    @PostMapping(value = "/features/forms/validation", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object submitValidation(@RequestBody ValidationRequest form) {
        form.name = FormValidator.blankToNull(form.name);
        form.email = FormValidator.blankToNull(form.email);
        form.website = FormValidator.blankToNull(form.website);
        FormValidator.validate(validator, form);
        return inertia.back().with("message", "Primary form submitted successfully!");
    }

    @PostMapping(value = "/features/forms/validation/secondary", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object submitValidationSecondary(@RequestBody ValidationErrorBagRequest form) {
        form.title = FormValidator.blankToNull(form.title);
        form.body = FormValidator.blankToNull(form.body);
        FormValidator.validate(validator, form);
        return inertia.back().with("message", "Secondary form submitted successfully!");
    }

    @GetMapping("/features/forms/precognition")
    public Object precognition() {
        return inertia.render("Features/Forms/Precognition", null);
    }

    @PostMapping(value = "/features/forms/precognition", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object storeAccount(
            @RequestHeader(value = "Precognition", required = false) String precognitionHeader,
            @RequestHeader(value = "Precognition-Validate-Only", required = false) String validateOnly,
            @RequestBody PrecognitionRequest form) {
        form.username = FormValidator.blankToNull(form.username);
        form.email = FormValidator.blankToNull(form.email);
        form.password = FormValidator.blankToNull(form.password);
        form.password_confirmation = FormValidator.blankToNull(form.password_confirmation);

        if (isPrecognition(precognitionHeader)) {
            return precognitionResponse(form, validateOnly);
        }

        FormValidator.validate(validator, form);
        if (form.password != null && !form.password.equals(form.password_confirmation)) {
            return inertia.back().withErrors(Map.of(
                "password_confirmation", "The password confirmation does not match."));
        }
        return inertia.back().with("message", "Account created for " + form.username + "!");
    }

    /**
     * Field-by-field validation for precognition requests: only the fields
     * listed in {@code Precognition-Validate-Only} are validated (the
     * {@code password}/{@code password_confirmation} pair always together).
     * Responds 204 when every validated field is valid, or 422 with the
     * errors wrapped as {@code {"errors": {...}}} — the laravel-precognition
     * client contract.
     */
    private Object precognitionResponse(PrecognitionRequest form, String validateOnly) {
        var fields = FormValidator.precognitionFields(validateOnly, PRECOGNITION_FIELDS);
        var errors = FormValidator.fieldErrors(validator, form, fields);
        if (fields.contains("password_confirmation")
                && form.password != null && !form.password.equals(form.password_confirmation)) {
            errors.put("password_confirmation", "The password confirmation does not match.");
        }
        if (errors.isEmpty()) {
            return ResponseEntity.noContent()
                .header("Precognition", "true")
                .header("Precognition-Success", "true")
                .build();
        }
        return ResponseEntity.status(422)
            .contentType(MediaType.APPLICATION_JSON)
            .header("Precognition", "true")
            .header("Vary", "Precognition")
            .body(Map.of("errors", errors));
    }

    private boolean isPrecognition(String header) {
        return header != null && Boolean.parseBoolean(header.trim());
    }

    @GetMapping("/features/forms/optimistic-updates")
    public Object optimisticUpdates() {
        return inertia.render("Features/Forms/OptimisticUpdates", Map.of("contacts", latestContacts()));
    }

    @PostMapping("/features/forms/optimistic-toggle/{contact}")
    public Object toggleFavorite(@PathVariable("contact") long contactId,
            @RequestParam(value = "simulate_error", required = false) String simulateErrorRaw) {
        Demo.sleepSeconds(1);
        var simulateError = simulateErrorRaw != null
            && !simulateErrorRaw.isBlank() && !"0".equals(simulateErrorRaw)
            && !"false".equalsIgnoreCase(simulateErrorRaw);
        if (simulateError) {
            return inertia.back().withErrors(Map.of(
                "contact", "Simulated validation error for optimistic update rollback demo."));
        }
        var contact = contactRepository.findById(contactId).orElse(null);
        if (contact == null) {
            return inertia.back().withErrors(Map.of(
                "contact", "The selected contact is invalid."));
        }
        write.toggleFavorite(contact);
        return inertia.back().with("message",
            contact.isFavorite ? "Added to favorites." : "Removed from favorites.");
    }

    @GetMapping("/features/forms/use-form-context")
    public Object useFormContext() {
        return inertia.render("Features/Forms/UseFormContext", null);
    }

    @GetMapping("/features/forms/dotted-keys")
    public Object dottedKeys() {
        return inertia.render("Features/Forms/DottedKeys", null);
    }

    @PostMapping(value = "/features/forms/dotted-keys", consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object submitDottedKeys(@RequestBody DottedKeysRequest form) {
        FormValidator.validate(validator, form);
        var parsed = new LinkedHashMap<String, Object>();
        if (form.user != null) {
            parsed.put("user", Map.of(
                "name", form.user.name,
                "email", form.user.email));
        }
        if (form.address != null) {
            parsed.put("address", Map.of(
                "street", form.address.street,
                "city", form.address.city));
        }
        parsed.put("tags", form.tags != null ? form.tags : List.of());
        return inertia.back().with("message", "Form submitted successfully!").with("parsedData", parsed);
    }

    private void validateSimple(SimpleFormRequest form) {
        form.name = FormValidator.blankToNull(form.name);
        form.email = FormValidator.blankToNull(form.email);
        form.bio = FormValidator.blankToNull(form.bio);
        form.role = FormValidator.blankToNull(form.role);
        FormValidator.validate(validator, form);
    }

    private Object latestContacts() {
        var list = contactRepository.findAllByOrderByIdDesc();
        var names = query.organizationNames();
        var items = new ArrayList<Object>();
        for (var contact : list.subList(0, Math.min(10, list.size()))) {
            var orgName = contact.organizationId != null ? names.get(contact.organizationId) : null;
            items.add(Resources.contact(contact,
                contact.organizationId != null && orgName != null
                    ? Resources.organization(contact.organizationId, orgName, null) : null));
        }
        return items;
    }
}