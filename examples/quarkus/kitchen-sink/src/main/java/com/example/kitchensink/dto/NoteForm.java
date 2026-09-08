package com.example.kitchensink.dto;

import jakarta.validation.constraints.NotBlank;

public class NoteForm {

    @NotBlank(message = "required")
    public String body;
}
