package com.microservicios.auth_service.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

@Schema(description = "Solicitud para establecer nueva contraseña")
class SetPasswordRequest {
    
    @NotBlank(message = "El token es requerido")
    @Schema(description = "Token de recuperación de contraseña", example = "abc123...", required = true)
    String token
    
    @NotBlank(message = "La nueva contraseña es requerida")
    @Size(min = 6, max = 100, message = "La contraseña debe tener al menos 6 caracteres")
    @Schema(description = "Nueva contraseña del usuario", example = "nuevaPassword123", required = true)
    String newPassword
}
