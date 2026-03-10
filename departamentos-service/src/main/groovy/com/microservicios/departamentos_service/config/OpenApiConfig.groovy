package com.microservicios.departamentos_service.config

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
                .title("API de Gestión de Departamentos")
                .version("1.0.0")
                .description("""
                    Servicio RESTful para la gestión de departamentos en la organización.
                    
                    ## Características principales:
                    - Registro, consulta, actualización y eliminación de departamentos (CRUD completo)
                    - Validación de datos de entrada
                    - Prevención de departamentos duplicados
                    - Respuestas estructuradas con códigos HTTP apropiados
                    - Resilience4j para tolerancia a fallos
                    
                    ## Uso:
                    Este servicio es consumido por otros microservicios (como empleados-service)
                    para validar la existencia de departamentos antes de realizar operaciones.
                    
                    ## Endpoints disponibles:
                    - POST /departamentos - Registrar nuevo departamento
                    - GET /departamentos/{id} - Obtener departamento por ID
                    - PUT /departamentos/{id} - Actualizar departamento existente
                    - DELETE /departamentos/{id} - Eliminar departamento
                    - GET /departamentos - Listar todos los departamentos
                    
                    ## Códigos de respuesta HTTP:
                    - 200: Operación exitosa
                    - 201: Departamento creado
                    - 204: Departamento eliminado
                    - 400: Datos inválidos o solicitud incorrecta
                    - 404: Departamento no encontrado
                    - 409: Departamento duplicado o conflicto
                    - 500: Error interno del servidor
                """)

                .license(new License()
                    .name("Apache 2.0")
                    .url("http://springdoc.org")))
            .addServersItem(new Server()
                .url("http://localhost:8081")
                .description("Servidor de desarrollo local"))
    }
}