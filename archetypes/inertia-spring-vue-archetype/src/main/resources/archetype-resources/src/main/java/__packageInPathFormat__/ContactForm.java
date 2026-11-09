package ${package};

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Form payload for creating and updating a contact. Bound from the request
 * by name; the {@code -parameters} compiler flag keeps the record component
 * names available for constructor binding.
 *
 * @param name  the contact name (required)
 * @param email the contact email (required, valid address)
 * @param phone the contact phone (optional, max 20 characters)
 */
public record ContactForm(
        @NotBlank(message = "El nombre es obligatorio")
        String name,

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El email no es válido")
        String email,

        @Size(max = 20, message = "El teléfono no puede superar los 20 caracteres")
        String phone) {
}
