package com.example.pingcrm.controller;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.Validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.pingcrm.dto.FormValidator;
import com.example.pingcrm.dto.UserForm;
import com.example.pingcrm.entity.User;
import com.example.pingcrm.repository.UserRepository;
import com.example.pingcrm.service.AuthService;
import com.example.pingcrm.service.UserService;
import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class UsersController {

    private static final java.util.Set<String> IMAGE_EXTENSIONS =
        java.util.Set.of("jpg", "jpeg", "png", "gif", "webp");

    private final Inertia inertia;
    private final AuthService auth;
    private final UserService users;
    private final UserRepository userRepository;
    private final Validator validator;

    @Value("${pingcrm.images.dir:./data/images}")
    private String imagesDir;

    public UsersController(Inertia inertia, AuthService auth, UserService users,
            UserRepository userRepository, Validator validator) {
        this.inertia = inertia;
        this.auth = auth;
        this.users = users;
        this.userRepository = userRepository;
        this.validator = validator;
    }

    @GetMapping("/users")
    public Object index(@RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String trashed) {
        var list = users.list(auth.accountId(), search, role, trashed);
        var filters = new LinkedHashMap<String, Object>();
        filters.put("search", search);
        filters.put("role", role);
        filters.put("trashed", trashed);
        return inertia.render("Users/Index", Map.of(
            "filters", filters,
            "users", list));
    }

    @GetMapping("/users/create")
    public Object create() {
        return inertia.render("Users/Create", Map.of());
    }

    @PostMapping(value = "/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object store(@ModelAttribute UserForm form) {
        form = normalize(form);
        FormValidator.validate(validator, form);
        if (userRepository.emailExistsForOtherUser(form.email(), null)) {
            return emailTaken();
        }
        var photoPath = savePhoto(form);
        if (photoPath == null && form.photo() != null) {
            return invalidImage();
        }
        users.create(auth.accountId(), form.first_name(), form.last_name(), form.email(),
            form.password(), "true".equals(form.owner()), photoPath);
        return inertia.redirect("/users").with("success", "User created.");
    }

    @GetMapping("/users/{id}/edit")
    public Object edit(@PathVariable long id) {
        var user = findOwned(id);
        if (user == null) {
            return notFound();
        }
        return inertia.render("Users/Edit", Map.of("user", user));
    }

    @PostMapping(value = "/users/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Object update(@PathVariable long id, @ModelAttribute UserForm form) {
        var user = findOwned(id);
        if (user == null) {
            return notFound();
        }
        if (user.isDemoUser()) {
            return inertia.back().with("error", "Updating the demo user is not allowed.");
        }
        normalize(form);
        FormValidator.validate(validator, form);
        if (userRepository.emailExistsForOtherUser(form.email(), user.id)) {
            return emailTaken();
        }
        var photoPath = savePhoto(form);
        if (photoPath == null && form.photo() != null) {
            return invalidImage();
        }
        users.update(user, form.first_name(), form.last_name(), form.email(),
            form.password(), "true".equals(form.owner()), photoPath);
        return inertia.back().with("success", "User updated.");
    }

    @DeleteMapping("/users/{id}")
    public Object destroy(@PathVariable long id) {
        var user = findOwned(id);
        if (user == null) {
            return notFound();
        }
        if (user.isDemoUser()) {
            return inertia.back().with("error", "Deleting the demo user is not allowed.");
        }
        users.softDelete(user);
        return inertia.back().with("success", "User deleted.");
    }

    @PutMapping("/users/{id}/restore")
    public Object restore(@PathVariable long id) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null || !user.accountId.equals(auth.accountId())) {
            return notFound();
        }
        users.restore(user);
        return inertia.back().with("success", "User restored.");
    }

    private User findOwned(long id) {
        var user = userRepository.findById(id).orElse(null);
        if (user == null || !user.accountId.equals(auth.accountId())) {
            return null;
        }
        return user;
    }

    private Object notFound() {
        return inertia.redirect("/users").with("error", "User not found.");
    }

    private Object emailTaken() {
        return inertia.back().withErrors(Map.of("email", "The email has already been taken."));
    }

    private Object invalidImage() {
        return inertia.back().withErrors(Map.of("photo", "The photo must be an image."));
    }

    private UserForm normalize(UserForm form) {
        return new UserForm(
            FormValidator.blankToNull(form.first_name()),
            FormValidator.blankToNull(form.last_name()),
            FormValidator.blankToNull(form.email()),
            FormValidator.blankToNull(form.password()),
            form.owner(),
            form.photo());
    }

    private String savePhoto(UserForm form) {
        var upload = form.photo();
        if (upload == null || upload.isEmpty() || upload.getOriginalFilename() == null
                || upload.getOriginalFilename().isBlank()) {
            return null;
        }
        var ext = extension(upload.getOriginalFilename());
        if (ext == null || !IMAGE_EXTENSIONS.contains(ext.toLowerCase())) {
            return null;
        }
        try {
            var filename = UUID.randomUUID() + "." + ext.toLowerCase();
            var target = java.nio.file.Path.of(imagesDir, "users", filename).toAbsolutePath().normalize();
            var base = java.nio.file.Path.of(imagesDir).toAbsolutePath().normalize();
            if (!target.startsWith(base)) {
                return null;
            }
            java.nio.file.Files.createDirectories(target.getParent());
            upload.transferTo(target);
            return "users/" + filename;
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String extension(String fileName) {
        var dot = fileName.lastIndexOf('.');
        return dot >= 0 ? fileName.substring(dot + 1) : null;
    }
}