package com.example.kitchensink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class OrganizationForm {

    @NotBlank(message = "required")
    @Size(max = 255, message = "max")
    public String name;

    public String _method;
}
