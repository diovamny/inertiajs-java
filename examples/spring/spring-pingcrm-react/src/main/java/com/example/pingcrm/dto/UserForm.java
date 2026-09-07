package com.example.pingcrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class UserForm {
    @NotBlank(message = "required")
    public String first_name;

    @NotBlank(message = "required")
    public String last_name;

    @NotBlank(message = "required")
    public String email;

    public String password;

    public Boolean owner;

    public MultipartFile photo;

    public String _method;
}