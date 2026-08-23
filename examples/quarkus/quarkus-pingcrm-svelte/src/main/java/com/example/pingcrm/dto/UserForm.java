package com.example.pingcrm.dto;

import jakarta.validation.constraints.NotBlank;

public class UserForm {
    @NotBlank(message = "required")
    public String first_name;

    @NotBlank(message = "required")
    public String last_name;

    @NotBlank(message = "required")
    public String email;

    public String password;

    public Boolean owner;
}