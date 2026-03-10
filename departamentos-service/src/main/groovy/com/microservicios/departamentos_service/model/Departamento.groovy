package com.microservicios.departamentos_service.model

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import io.swagger.v3.oas.annotations.media.Schema

@Document(collection = "departamentos")
@Schema(description = "Representa un departamento en la organización")
class Departamento {
    @Id
    @Schema(description = "Identificador único del departamento (código alfanumérico)", example = "IT", required = true)
    String id
    
    @NotBlank(message = "El nombre es requerido")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Schema(description = "Nombre corto del departamento", example = "Tecnología", required = true)
    String nombre
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Schema(description = "Descripción detallada de las funciones del departamento", example = "Departamento de Tecnología e Innovación", required = false)
    String descripcion
    
    @Schema(description = "Fecha de creación del registro", example = "2026-02-21T23:59:08.843Z", accessMode = Schema.AccessMode.READ_ONLY)
    Date fechaCreacion = new Date()
}