package com.example.pingcrm.dto;

import jakarta.validation.constraints.NotBlank;

public class ContactForm {
    @NotBlank(message = "required")
    public String first_name;

    @NotBlank(message = "required")
    public String last_name;

    public String email;

    public String phone;

    public String address;

    public String city;

    public String region;

    public String country;

    public String postal_code;

    public String organization_id;
}
