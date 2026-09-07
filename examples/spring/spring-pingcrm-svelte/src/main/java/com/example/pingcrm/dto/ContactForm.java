package com.example.pingcrm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ContactForm {

    @NotBlank(message = "required")
    @Size(max = 50, message = "max")
    public String first_name;

    @NotBlank(message = "required")
    @Size(max = 50, message = "max")
    public String last_name;

    public String organization_id;

    @Size(max = 50, message = "max")
    @Email(message = "invalid")
    public String email;

    @Size(max = 50, message = "max")
    public String phone;

    @Size(max = 150, message = "max")
    public String address;

    @Size(max = 50, message = "max")
    public String city;

    @Size(max = 50, message = "max")
    public String region;

    @Size(max = 50, message = "max")
    public String country;

    @Size(max = 25, message = "max")
    public String postal_code;

    public String _method;
}