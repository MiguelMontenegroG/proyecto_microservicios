package com.microservicios.perfiles_service.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import io.swagger.v3.oas.models.Components
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    
    @Bean
    OpenAPI customOpenAPI() {
        new OpenAPI(
            info: new Info(
                title: 'Perfiles Service API',
                version: '1.0.0',
                description: '''Servicio RESTful para la gestión de perfiles de empleados.
                    
                    ## Características principales:
                    - Consulta y actualización de perfiles de empleados
                    - Información detallada de contacto y biografía
                    - Validación de datos de entrada
                    - Respuestas estructuradas con códigos HTTP apropiados
                    
                    ## Uso:
                    Este servicio es consumido por otros microservicios para obtener
                    información detallada de los empleados.
                    
                    ## Endpoints disponibles:
                    - GET /perfiles - Listar todos los perfiles
                    - GET /perfiles/{empleadoId} - Obtener perfil por empleado
                    - PUT /perfiles/{empleadoId} - Actualizar perfil existente
                    
                    ## Códigos de respuesta HTTP:
                    - 200: Operación exitosa
                    - 404: Perfil no encontrado
                    - 500: Error interno del servidor
                    
                    ## Seguridad:
                    Todos los endpoints requieren autenticación JWT.
                    Incluir header: Authorization: Bearer <token>
                ''',
                contact: new Contact(name: 'Equipo de Desarrollo')
            )
        )
            .addSecurityItem(new SecurityRequirement().addList('Bearer Authentication'))
            .components(new Components()
                .addSecuritySchemes('Bearer Authentication', new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme('bearer')
                    .bearerFormat('JWT')
                    .description('Ingrese el token JWT obtenido desde /auth/login')))
    }
}
