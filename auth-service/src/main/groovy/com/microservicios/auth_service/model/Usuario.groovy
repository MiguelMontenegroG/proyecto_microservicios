package com.microservicios.auth_service.model

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.index.Indexed
import jakarta.validation.constraints.*

@Document(collection = "usuarios")
@Schema(description = "Representa un usuario en el sistema para autenticación")
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
    
    @NotBlank(message = "La contraseña es requerida")
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    @Schema(description = "Contraseña del usuario (encriptada)", example = '$2a$10$...', required = true)
    String password
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "Formato de email inválido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    @Indexed(unique = true)
    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@empresa.com", required = true)
    String email
    
    @Schema(description = "Nombre completo del usuario", example = "Juan Pérez", required = false)
    String nombreCompleto
    
    @Schema(description = "Rol del usuario (ADMIN o USER)", example = "USER", required = true, allowableValues = ["ADMIN", "USER"])
    String rol = "USER"
    
    @Schema(description = "Indica si el usuario está activo", example = "true", required = true)
    boolean activo = true
    
    @Schema(description = "ID del empleado asociado", example = "EMP001", required = false)
    String empleadoId
    
    @Schema(description = "Token de recuperación de contraseña", required = false, accessMode = Schema.AccessMode.WRITE_ONLY)
    String resetToken
    
    @Schema(description = "Fecha de expiración del token de recuperación", required = false, accessMode = Schema.AccessMode.WRITE_ONLY)
    Date resetTokenExpiry
    
    @Schema(description = "Fecha de creación del usuario", example = "2024-01-15T10:30:00Z", required = false, accessMode = Schema.AccessMode.READ_ONLY)
    Date fechaCreacion = new Date()
    
    @Schema(description = "Fecha de última modificación", example = "2024-01-15T10:30:00Z", required = false, accessMode = Schema.AccessMode.READ_ONLY)
    Date fechaModificacion = new Date()
}
