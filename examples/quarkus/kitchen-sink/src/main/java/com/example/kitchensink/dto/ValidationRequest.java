package com.example.kitchensink.dto;

import jakarta.validation.constraints.Email;
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

    // String (not Integer): JSON-B cannot coerce "" to a number while
    // Jackson can, so the demo parses explicitly (same messages as Spring).
    public String age;

    @jakarta.validation.constraints.Pattern(
        regexp = "^(https?://.*)?$", message = "Please enter a valid URL (e.g., https://example.com).")
    public String website;
}
