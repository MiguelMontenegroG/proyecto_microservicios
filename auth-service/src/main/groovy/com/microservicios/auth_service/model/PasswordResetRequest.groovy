package com.microservicios.auth_service.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

@Schema(description = "Solicitud de recuperación de contraseña")
class PasswordResetRequest {
    
    @NotBlank(message = "El email es requerido")
    @Email(message = "Formato de email inválido")
    @Schema(description = "Correo electrónico del usuario", example = "juan.perez@empresa.com", required = true)
    String email
}
