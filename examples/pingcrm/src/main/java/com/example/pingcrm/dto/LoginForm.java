package com.example.pingcrm.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class LoginForm {

    @NotBlank(message = "required")
    @Email(message = "invalid")
    public String email;

    @NotBlank(message = "required")
    public String password;
}
