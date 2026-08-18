package com.example.demo.resource;

import java.util.Map;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.mutiny.Uni;

import com.example.demo.entity.Employee;
import com.example.demo.service.EmployeeService;
import io.github.dg.quarkus.inertia.api.Inertia;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@Path("/employees")
public class EmployeeResource {

    @Inject
    Inertia inertia;

    @Inject
    EmployeeService employeeService;

    @SuppressWarnings("unchecked")
    @GET
    @Blocking
    public Uni<Object> index(
            @QueryParam("filters") String filtersJson,
            @QueryParam("sortField") String sortField,
            @QueryParam("sortOrder") @DefaultValue("1") int sortOrder,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size) {

        Map<String, Object> filters = null;
        if (filtersJson != null && !filtersJson.isBlank()) {
            try {
                var mapper = new ObjectMapper();
                var tree = mapper.readTree(filtersJson);
                if (tree.isArray()) {
                    filters = new java.util.LinkedHashMap<>();
                    for (var elem : tree) {
                        if (elem.has("field")) {
                            var field = elem.get("field").asText();
                            var operator = elem.has("matchMode") ? elem.get("matchMode").asText() :
                                           (elem.has("operator") ? elem.get("operator").asText() : "equals");
                            Object constraint = null;
                            if (elem.has("value")) {
                                constraint = mapper.convertValue(elem.get("value"), Object.class);
                            }
                            if (constraint != null) {
                                filters.put(field, Map.of("value", constraint, "matchMode", operator));
                            }
                        }
                    }
                } else if (tree.isObject()) {
                    filters = mapper.convertValue(tree, Map.class);
                }
            } catch (Exception e) {
                // ignore invalid filter JSON
            }
        }

        var result = employeeService.findLazy(filters, sortField, sortOrder, page, size);

        return inertia.render("Employees/Index", Map.of(
            "data", result.data(),
            "total", result.total(),
            "page", result.page(),
            "size", result.size()
        ));
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @Blocking
    public Response store(@Valid EmployeeForm form) {
        var emp = new Employee();
        emp.firstName = form.firstName;
        emp.lastName = form.lastName;
        emp.email = form.email;
        emp.salary = form.salary;
        emp.active = form.active != null ? form.active : true;
        emp.department = form.department;
        emp.hireDate = form.hireDate;
        employeeService.create(emp);

        inertia.flash("success", "Empleado creado correctamente");
        return (Response) inertia.redirect("/employees").await().indefinitely();
    }

    @GET
    @Path("/create")
    public Uni<Object> createForm() {
        return inertia.render("Employees/Form", Map.of("employee", new Employee(), "editing", false));
    }

    @GET
    @Path("/{id}/edit")
    @Blocking
    public Uni<Object> editForm(@PathParam("id") Long id) {
        var emp = employeeService.findById(id);
        if (emp == null) {
            inertia.flash("error", "Empleado no encontrado");
            return inertia.render("Employees/Index", Map.of(
                "data", java.util.List.of(), "total", 0L, "page", 0, "size", 50));
        }
        return inertia.render("Employees/Form", Map.of("employee", emp, "editing", true));
    }

    @POST
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional
    @Blocking
    public Response update(@PathParam("id") Long id, @Valid EmployeeForm form) {
        var emp = employeeService.findById(id);
        if (emp == null) {
            inertia.flash("error", "Empleado no encontrado");
            return (Response) inertia.redirect("/employees").await().indefinitely();
        }

        emp.firstName = form.firstName;
        emp.lastName = form.lastName;
        emp.email = form.email;
        emp.salary = form.salary;
        emp.active = form.active != null ? form.active : true;
        emp.department = form.department;
        emp.hireDate = form.hireDate;
        employeeService.update(id, emp);

        inertia.flash("success", "Empleado actualizado correctamente");
        return (Response) inertia.redirect("/employees").await().indefinitely();
    }

    @POST
    @Path("/{id}/delete")
    @Transactional
    @Blocking
    public Response delete(@PathParam("id") Long id) {
        employeeService.delete(id);
        inertia.flash("success", "Empleado eliminado correctamente");
        return (Response) inertia.redirect("/employees").await().indefinitely();
    }

    public static class EmployeeForm {
        @jakarta.validation.constraints.NotBlank
        public String firstName;

        @jakarta.validation.constraints.NotBlank
        public String lastName;

        @jakarta.validation.constraints.NotBlank
        @jakarta.validation.constraints.Email
        public String email;

        @jakarta.validation.constraints.NotNull
        @jakarta.validation.constraints.DecimalMin("0.01")
        public java.math.BigDecimal salary;

        public Boolean active;

        public String department;

        public java.time.LocalDate hireDate;
    }
}
