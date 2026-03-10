package com.microservicios.empleados_service.model

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Email
import io.swagger.v3.oas.annotations.media.Schema

@Document(collection = "empleados")
@Schema(description = "Representa un empleado en el sistema (vista simplificada)")
class Empleado {
    @Id
    @Schema(description = "Identificador único del empleado", example = "EMP001", required = true)
    String id
    
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre completo del empleado", example = "Juan Pérez", required = true)
    String nombre
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "Formato de email inválido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    @Schema(description = "Correo electrónico del empleado", example = "juan.perez@empresa.com", required = true)
    String email
    
    @NotBlank(message = "El departamentoId es requerido")
    @Size(min = 1, max = 50, message = "El departamentoId debe tener entre 1 y 50 caracteres")
    @Pattern(regexp = "^[A-Za-z0-9_-]+", message = "El departamentoId solo puede contener letras, números, guiones y guiones bajos")
    @Schema(description = "Identificador del departamento al que pertenece el empleado", example = "IT", required = true)
    String departamentoId
    
    @Schema(description = "Fecha de ingreso del empleado a la empresa", example = "2026-02-21T20:15:30.097Z")
    Date fechaIngreso = new Date()
    
    @Schema(description = "Fecha de creación del registro", example = "2026-02-21T20:15:30.097Z", accessMode = Schema.AccessMode.READ_ONLY)
    Date fechaCreacion = new Date()
}
