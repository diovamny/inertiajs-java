package com.example.pingcrm.dto;

import jakarta.ws.rs.core.MediaType;

import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

public class UserForm {

    @RestForm("first_name")
    public String first_name;

    @RestForm("last_name")
    public String last_name;

    @RestForm("email")
    public String email;

    @RestForm("password")
    public String password;

    @RestForm("owner")
    public String owner;

    @RestForm("photo")
    @PartType(MediaType.APPLICATION_OCTET_STREAM)
    public FileUpload photo;

    @RestForm("_method")
    public String _method;
}
