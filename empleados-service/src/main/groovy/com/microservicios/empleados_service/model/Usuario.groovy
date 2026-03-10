package com.microservicios.empleados_service.model

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.index.Indexed
import jakarta.validation.constraints.*

@Document(collection = "usuarios")
@Schema(description = "Representa un usuario en el sistema con información completa del empleado")
class Usuario {
    
    @Id
    @Schema(description = "Identificador único del usuario", example = "699a126276890ac647bf8ff4", required = false, accessMode = Schema.AccessMode.READ_ONLY)
    String id
    
    @NotBlank(message = "El nombre de usuario es requerido")
    @Size(min = 3, max = 50, message = "El nombre de usuario debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+", message = "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos")
    @Indexed(unique = true)
    @Schema(description = "Nombre de usuario único para login", example = "juan.perez", required = true)
    String username
    
    @NotBlank(message = "El nombre completo es requerido")
    @Size(min = 2, max = 100, message = "El nombre completo debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez", required = true)
    String nombreCompleto
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "Formato de email inválido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    @Indexed(unique = true)
    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@empresa.com", required = true)
    String email
    
    @NotBlank(message = "El rol es requerido")
    @Size(max = 50, message = "El rol no puede exceder 50 caracteres")
    @Schema(description = "Rol o cargo del usuario en la organización", example = "Desarrollador", required = true)
    String rol
    
    @Schema(description = "Identificador del departamento al que pertenece el usuario", example = "IT")
    String departamentoId
    
    @Min(value = 18, message = "La edad debe ser mayor o igual a 18")
    @Max(value = 100, message = "La edad debe ser menor o igual a 100")
    @Schema(description = "Edad del usuario", example = "28")
    Integer edad
    
    @Schema(description = "Indica si el usuario está activo en el sistema", example = "true")
    Boolean activo = true
    
    @Schema(description = "Fecha de creación del registro", example = "2026-02-21T20:15:30.097Z", accessMode = Schema.AccessMode.READ_ONLY)
    Date fechaCreacion = new Date()
    
    @Schema(description = "Fecha de última actualización del registro", example = "2026-02-21T20:15:30.097Z", accessMode = Schema.AccessMode.READ_ONLY)
    Date fechaActualizacion = new Date()
}