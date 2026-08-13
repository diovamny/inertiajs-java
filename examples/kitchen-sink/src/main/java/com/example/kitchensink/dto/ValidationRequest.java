package com.example.kitchensink.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ValidationRequest {

    @NotBlank(message = "Please enter your full name.")
    @Size(max = 255, message = "max")
    public String name;

    @NotBlank(message = "We need your email address.")
    @Email(message = "That doesn't look like a valid email.")
    @Size(max = 255, message = "max")
    public String email;

    @Min(value = 18, message = "You must be at least 18 years old.")
    @Max(value = 120, message = "Please enter a valid age.")
    public Integer age;

    @jakarta.validation.constraints.Pattern(
        regexp = "^(https?://.*)?$", message = "Please enter a valid URL (e.g., https://example.com).")
    public String website;
}