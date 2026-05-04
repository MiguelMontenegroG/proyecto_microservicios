package com.microservicios.notificaciones_service.config

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
                title: 'Notificaciones Service API',
                version: '1.0.0',
                description: '''Servicio RESTful para la gestión de notificaciones de empleados.
                    
                    ## Características principales:
                    - Consulta de notificaciones por empleado
                    - Listado general de notificaciones
                    - Integración con eventos del sistema via RabbitMQ
                    - Respuestas estructuradas con códigos HTTP apropiados
                    
                    ## Uso:
                    Este servicio recibe eventos de otros microservicios (empleados, departamentos)
                    y genera notificaciones correspondientes para los empleados.
                    
                    ## Endpoints disponibles:
                    - GET /notificaciones - Listar todas las notificaciones
                    - GET /notificaciones/{empleadoId} - Obtener notificaciones por empleado
                    
                    ## Códigos de respuesta HTTP:
                    - 200: Operación exitosa
                    - 404: Notificaciones no encontradas
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
