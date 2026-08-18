package com.example.kitchensink.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

public class DottedKeysRequest {

    @Valid
    public UserPart user;

    @Valid
    public AddressPart address;

    @Size(max = 50, message = "max")
    public java.util.List<@Size(max = 50, message = "max") String> tags;

    public static class UserPart {
        @jakarta.validation.constraints.NotBlank(message = "The name field is required.")
        @Size(max = 255, message = "max")
        public String name;

        @jakarta.validation.constraints.NotBlank(message = "The email field is required.")
        @jakarta.validation.constraints.Email(message = "The email field must be a valid email.")
        @Size(max = 255, message = "max")
        public String email;
    }

    public static class AddressPart {
        @jakarta.validation.constraints.NotBlank(message = "The street field is required.")
        @Size(max = 255, message = "max")
        public String street;

        @jakarta.validation.constraints.NotBlank(message = "The city field is required.")
        @Size(max = 255, message = "max")
        public String city;
    }
}