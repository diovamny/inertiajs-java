package com.example.kitchensink.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PrecognitionRequest {

    @NotBlank(message = "The username field is required.")
    @Size(min = 3, max = 20, message = "The username field must be between 3 and 20 characters.")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "The username field format is invalid.")
    public String username;

    @NotBlank(message = "The email field is required.")
    @Email(message = "The email field must be a valid email address.")
    @Size(max = 255, message = "The email field must not be greater than 255 characters.")
    public String email;

    @NotBlank(message = "The password field is required.")
    @Size(min = 8, message = "The password field must be at least 8 characters.")
    public String password;

    @NotBlank(message = "The password confirmation field is required.")
    public String password_confirmation;
}
