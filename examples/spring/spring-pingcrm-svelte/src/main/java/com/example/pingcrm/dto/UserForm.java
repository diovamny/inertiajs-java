package com.example.pingcrm.dto;

import org.springframework.web.multipart.MultipartFile;

public record UserForm(
        String first_name,
        String last_name,
        String email,
        String password,
        String owner,
        MultipartFile photo,
        String _method) {
}