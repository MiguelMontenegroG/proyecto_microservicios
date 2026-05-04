package com.microservicios.auth_service.controller

import com.microservicios.auth_service.model.*
import com.microservicios.auth_service.service.AuthService
import com.microservicios.auth_service.service.CustomUserDetailsService
import com.microservicios.auth_service.service.JwtProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticación", description = "API para gestión de autenticación y autorización con JWT")
class AuthController {
    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class)
    
    private final AuthenticationManager authenticationManager
    private final JwtProvider jwtProvider
    private final CustomUserDetailsService userDetailsService
    private final AuthService authService
    
    AuthController(AuthenticationManager authenticationManager,
                   JwtProvider jwtProvider,
                   CustomUserDetailsService userDetailsService,
                   AuthService authService) {
        this.authenticationManager = authenticationManager
        this.jwtProvider = jwtProvider
        this.userDetailsService = userDetailsService
        this.authService = authService
    }
    
    @PostMapping("/login")
    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica un usuario y retorna un token JWT"
    )
    @ApiResponses(value = [
        @ApiResponse(responseCode = "200", description = "Login exitoso",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse))),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o formato incorrecto"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas - usuario o contraseña incorrectos"),
        @ApiResponse(responseCode = "403", description = "Usuario inactivo o bloqueado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor al procesar login")
    ])
    ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Intento de login para usuario: {}", loginRequest.username)
        
        try {
            // Autenticar usuario
            def authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.username,
                    loginRequest.password
                )
            )
            
            // Generar token JWT
            String token = jwtProvider.generateToken(authentication)
            UserDetails userDetails = (UserDetails) authentication.principal
            
            // Obtener rol del token
            String rol = jwtProvider.getRoleFromToken(token)
            
            log.info("Login exitoso para usuario: {}", loginRequest.username)
            
            return ResponseEntity.ok(
                new LoginResponse(token, loginRequest.username, rol, jwtProvider.expirationMillis)
            )
            
        } catch (Exception e) {
            log.error("Error en login: {}", e.message)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas")
        }
    }
    
    @GetMapping("/validate")
    @Operation(
        summary = "Validar token JWT",
        description = "Valida si un token JWT es válido y retorna información del usuario"
    )
    @ApiResponses(value = [
        @ApiResponse(responseCode = "200", description = "Token válido", 
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"valid\": true, \"username\": \"juan.perez\", \"role\": \"USER\"}"))),
        @ApiResponse(responseCode = "400", description = "Token no proporcionado o formato inválido"),
        @ApiResponse(responseCode = "401", description = "Token inválido o expirado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado - usuario no autorizado"),
        @ApiResponse(responseCode = "500", description = "Error interno al validar token")
    ])
    ResponseEntity<Map<String, Object>> validateToken(
        @Parameter(description = "Token JWT a validar", required = true, example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        @RequestParam String token) {
        
        log.info("Validando token...")
        
        if (!jwtProvider.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Token inválido o expirado")
        }
        
        String username = jwtProvider.getUsernameFromToken(token)
        String rol = jwtProvider.getRoleFromToken(token)
        
        Map<String, Object> response = [
            valid: true,
            username: username,
            role: rol
        ]
        
        return ResponseEntity.ok(response)
    }
    
    @PostMapping("/forgot-password")
    @Operation(
        summary = "Solicitar recuperación de contraseña",
        description = "Genera un token de recuperación. PARA PRUEBAS: indica si el email existe y retorna el token directamente"
    )
    @ApiResponses(value = [
        @ApiResponse(responseCode = "200", description = "Token generado exitosamente", 
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"message\": \"Token de recuperación generado\", \"resetToken\": \"abc123...\"}"))),
        @ApiResponse(responseCode = "400", description = "Formato de email inválido o datos incorrectos"),
        @ApiResponse(responseCode = "403", description = "Usuario inactivo o bloqueado - no se permite recuperación"),
        @ApiResponse(responseCode = "500", description = "Error interno al procesar solicitud de recuperación")
    ])
    ResponseEntity<Map<String, Object>> forgotPassword(
        @Valid @RequestBody PasswordResetRequest request) {
        
        log.info("Solicitud de recuperación para email: {}", request.email)
        
        Map<String, Object> resultado = authService.solicitarRecuperacionContrasena(request.email)
        
        if (!resultado.existe) {
            // Para pruebas: indicar claramente que el email no existe
            String motivo = resultado.motivo ?: "email_no_encontrado"
            return ResponseEntity.ok([
                message: "El email '${request.email}' NO está registrado en el sistema",
                existe: false,
                motivo: motivo
            ])
        }
        
        // Email existe - retorna el token directamente (desarrollo/pruebas)
        return ResponseEntity.ok([
            message: "Token de recuperación generado exitosamente",
            existe: true,
            resetToken: resultado.resetToken,
            instruction: "Usa este token en POST /auth/reset-password con tu nueva contraseña"
        ])
    }
    
    @PostMapping("/reset-password")
    @Operation(
        summary = "Restablecer contraseña",
        description = "Establece una nueva contraseña usando el token de recuperación"
    )
    @ApiResponses(value = [
        @ApiResponse(responseCode = "200", description = "Contraseña restablecida exitosamente", 
            content = @Content(mediaType = "application/json",
                schema = @Schema(example = "{\"message\": \"Contraseña restablecida exitosamente\"}"))),
        @ApiResponse(responseCode = "400", description = "Token inválido, expirado o formato de contraseña incorrecto"),
        @ApiResponse(responseCode = "500", description = "Error interno al restablecer contraseña")
    ])
    ResponseEntity<Map<String, String>> resetPassword(
        @Valid @RequestBody SetPasswordRequest request) {
        
        log.info("Intentando restablecer contraseña con token")
        
        boolean exitoso = authService.restablecerContrasena(request.token, request.newPassword)
        
        if (!exitoso) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token inválido o expirado")
        }
        
        return ResponseEntity.ok([message: "Contraseña restablecida exitosamente"])
    }
}
