package com.example.pingcrm.dto;

import jakarta.validation.constraints.NotBlank;

public class OrganizationForm {
    @NotBlank(message = "required")
    public String name;

    public String email;

    public String phone;

    public String address;

    public String city;

    public String region;

    public String country;

    public String postal_code;
}
