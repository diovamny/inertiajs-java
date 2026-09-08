package com.example.kitchensink.controller.feature;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.github.dg.spring.inertia.api.Inertia;

@RestController
public class NetworkErrorController {

    private final Inertia inertia;

    public NetworkErrorController(Inertia inertia) {
        this.inertia = inertia;
    }

    @GetMapping("/features/errors/http-exceptions")
    public Object httpExceptions() {
        return inertia.render("Features/Errors/HttpExceptions");
    }

    @GetMapping("/features/errors/http-exceptions/403")
    public Object httpException403() {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
    }

    @GetMapping("/features/errors/http-exceptions/404")
    public Object httpException404() {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Not Found");
    }

    @GetMapping("/features/errors/http-exceptions/500")
    public Object httpException500() {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Server Error");
    }

    @GetMapping("/features/errors/http-exceptions/unhandled")
    public Object httpExceptionUnhandled() {
        throw new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot");
    }

    @GetMapping("/features/errors/java-exceptions/400")
    public Object javaException400() {
        throw new IllegalArgumentException("Datos inválidos recibidos");
    }

    @GetMapping("/features/errors/java-exceptions/403")
    public Object javaException403() {
        throw new SecurityException("Acceso denegado");
    }

    @GetMapping("/features/errors/java-exceptions/409")
    public Object javaException409() {
        throw new IllegalStateException("Estado inválido para la operación");
    }

    @GetMapping("/features/errors/java-exceptions/422")
    public Object javaException422() {
        throw new jakarta.validation.ValidationException("Regla de negocio violada");
    }

    @GetMapping("/features/errors/network-errors")
    public Object networkErrors() {
        return inertia.render("Features/Errors/NetworkErrors");
    }
}