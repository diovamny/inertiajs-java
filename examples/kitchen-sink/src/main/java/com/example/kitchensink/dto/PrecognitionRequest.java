package com.example.kitchensink.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PrecognitionRequest {

    @NotBlank(message = "required")
    @Size(min = 3, max = 20, message = "size")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "format")
    public String username;

    @NotBlank(message = "required")
    @Email(message = "invalid")
    @Size(max = 255, message = "max")
    public String email;

    @NotBlank(message = "required")
    @Size(min = 8, message = "min")
    public String password;

    @NotBlank(message = "required")
    public String password_confirmation;
}