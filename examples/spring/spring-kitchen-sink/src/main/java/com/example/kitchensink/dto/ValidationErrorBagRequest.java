package com.example.kitchensink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ValidationErrorBagRequest {

    @NotBlank(message = "required")
    @Size(max = 255, message = "max")
    public String title;

    @NotBlank(message = "required")
    @Size(min = 10, message = "min")
    public String body;
}
