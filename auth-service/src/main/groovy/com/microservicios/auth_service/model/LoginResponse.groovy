package com.microservicios.auth_service.model

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Respuesta de autenticación con token JWT")
class LoginResponse {
    
    @Schema(description = "Token JWT de acceso", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    String token
    
    @Schema(description = "Tipo de token", example = "Bearer")
    String type = "Bearer"
    
    @Schema(description = "Nombre de usuario autenticado", example = "juan.perez")
    String username
    
    @Schema(description = "Rol del usuario", example = "USER")
    String rol
    
    @Schema(description = "Tiempo de expiración en milisegundos", example = "3600000")
    long expiresIn
    
    LoginResponse(String token, String username, String rol, long expiresIn) {
        this.token = token
        this.username = username
        this.rol = rol
        this.expiresIn = expiresIn
    }
}
