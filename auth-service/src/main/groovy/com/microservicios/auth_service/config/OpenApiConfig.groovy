package com.microservicios.auth_service.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    
    @Value('${empleado.service.url:http://localhost:8080}')
    String empleadosServiceUrl
    
    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API de Autenticación y Autorización")
                .version("1.0.0")
                .description("""
                    Servicio centralizado de autenticación y autorización con JWT.
                    
                    ## Características principales:
                    - Login y generación de tokens JWT
                    - Recuperación de contraseña
                    - Gestión de usuarios
                    - Integración con eventos del sistema
                    
                    ## Seguridad:
                    Todos los endpoints excepto /auth/login y /auth/reset-password utilizan 
                    autenticación Bearer JWT.
                    
                    ## Flujo de autenticación:
                    1. Usuario realiza login con credenciales
                    2. Sistema valida y retorna JWT token
                    3. Cliente incluye token en header Authorization: Bearer <token>
                    4. Token expira después del tiempo configurado
                    """)
                .contact(new Contact()
                    .name("Equipo de Desarrollo")
                    .email("dev@empresa.com"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")))
            .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
            .components(new io.swagger.v3.oas.models.Components()
                .addSecuritySchemes("Bearer Authentication", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Ingrese el token JWT obtenido desde /auth/login")))
            .servers([
                new Server().url("http://localhost:8085").description("Servidor local"),
            ])
    }
}
