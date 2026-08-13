package com.example.kitchensink.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SimpleFormRequest {

    @NotBlank(message = "required")
    @Size(max = 255, message = "max")
    public String name;

    @NotBlank(message = "required")
    @Email(message = "invalid")
    @Size(max = 255, message = "max")
    public String email;

    @Size(max = 1000, message = "max")
    public String bio;

    @NotBlank(message = "required")
    public String role;

    public boolean subscribe;
}