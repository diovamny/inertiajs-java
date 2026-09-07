package com.example.kitchensink.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ContactForm {

    public String _method;

    @NotBlank(message = "required")
    @Size(max = 255, message = "max")
    public String first_name;

    @NotBlank(message = "required")
    @Size(max = 255, message = "max")
    public String last_name;

    @Email(message = "invalid")
    @Size(max = 255, message = "max")
    public String email;

    @Size(max = 255, message = "max")
    public String phone;

    @Size(max = 255, message = "max")
    public String organization_id;
}