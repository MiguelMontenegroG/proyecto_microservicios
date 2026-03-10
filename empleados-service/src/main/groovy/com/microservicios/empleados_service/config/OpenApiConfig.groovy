package com.microservicios.empleados_service.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {
    
    @Bean
    OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API de Gestión de Empleados")
                .version("1.0.0")
                .description("""
                    Servicio RESTful para la gestión de empleados en el sistema de onboarding/offboarding.
                    
                    ## Características principales:
                    - Registro, consulta, actualización y eliminación de empleados (CRUD completo)
                    - Validación de departamentos mediante comunicación con otro microservicio
                    - Paginación de resultados
                    - Manejo de errores con mensajes descriptivos
                    - Resilience4j para tolerancia a fallos
                    
                    ## Validaciones:
                    - Verificación de existencia de departamento antes de crear/actualizar empleado
                    - Validación de datos obligatorios
                    - Prevención de IDs duplicados
                    - Validaciones de tamaño y formato de campos
                    
                    ## Integración:
                    Este servicio se comunica con el servicio de departamentos para validar 
                    la existencia de departamentos antes de registrar empleados.
                    
                    ## Códigos de respuesta HTTP:
                    - 200: Operación exitosa
                    - 201: Recurso creado
                    - 400: Solicitud incorrecta/validación fallida
                    - 404: Recurso no encontrado
                    - 405: Método no permitido
                    - 409: Conflicto (recurso duplicado)
                    - 500: Error interno del servidor
                    - 503: Servicio no disponible (Circuit Breaker)
                """)

                .license(new License()
                    .name("Apache 2.0")
                    .url("http://springdoc.org")))
            .addServersItem(new Server()
                .url("http://localhost:8080")
                .description("Servidor de desarrollo local"))
    }
}