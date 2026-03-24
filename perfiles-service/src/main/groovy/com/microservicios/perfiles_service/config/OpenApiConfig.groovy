package com.microservicios.perfiles_service.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
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
                description: 'Servicio de gestión de perfiles para empleados - Reto 3 Microservicios',
                contact: new Contact(name: 'Equipo de Desarrollo')
            )
        )
    }
}
