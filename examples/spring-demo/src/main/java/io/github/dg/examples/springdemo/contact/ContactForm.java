package io.github.dg.examples.springdemo.contact;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ContactForm(
        @NotBlank(message = "El nombre es obligatorio") String name,
        @NotBlank(message = "El email es obligatorio") @Email(message = "El email no es válido") String email,
        @NotBlank(message = "El teléfono es obligatorio") String phone) {
}