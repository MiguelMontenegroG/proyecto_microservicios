package com.microservicios.departamentos_service.controller

import com.microservicios.departamentos_service.model.Departamento
import com.microservicios.departamentos_service.repository.DepartamentoRepository
import jakarta.validation.Valid
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/departamentos")
@Tag(name = "Departamentos", description = "API para gestión de departamentos de la organización")
class DepartamentoController {

    @Autowired
    DepartamentoRepository departamentoRepository

    @PostMapping
    @Operation(
        summary = "Registrar nuevo departamento",
        description = "Crea un nuevo departamento en el sistema"
    )
    @ApiResponse(responseCode = "201", description = "Departamento creado exitosamente",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Departamento)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos proporcionados")
    @ApiResponse(responseCode = "409", description = "Departamento con ese ID ya existe")
    ResponseEntity<Departamento> crearDepartamento(@Valid @RequestBody Departamento departamento) {
        try {
            // Validar que no exista un departamento con el mismo ID
            if (departamentoRepository.existsById(departamento.id)) {
                Map<String, String> errorResponse = [
                    "error": "CONFLICT",
                    "message": "Departamento con ID '" + departamento.id + "' ya existe",
                    "timestamp": new Date().toString()
                ]
                return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT)
            }
            
            Departamento savedDepartamento = departamentoRepository.save(departamento)
            return new ResponseEntity<>(savedDepartamento, HttpStatus.CREATED)
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener departamento por ID",
        description = "Recupera la información de un departamento específico por su identificador"
    )
    @Parameter(name = "id", description = "Identificador único del departamento", required = true)
    @ApiResponse(responseCode = "200", description = "Departamento encontrado",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Departamento)))
    @ApiResponse(responseCode = "404", description = "Departamento no encontrado")
    ResponseEntity<Departamento> obtenerDepartamentoPorId(@PathVariable("id") String id) {
        Optional<Departamento> departamento = departamentoRepository.findById(id)
        if (departamento.isPresent()) {
            return new ResponseEntity<>(departamento.get(), HttpStatus.OK)
        } else {
            Map<String, String> errorResponse = [
                "error": "NOT_FOUND",
                "message": "Departamento con ID '" + id + "' no encontrado",
                "timestamp": new Date().toString()
            ]
            return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND)
        }
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar departamento",
        description = "Actualiza la información de un departamento existente"
    )
    @Parameter(name = "id", description = "Identificador único del departamento a actualizar", required = true)
    @ApiResponse(responseCode = "200", description = "Departamento actualizado exitosamente",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Departamento)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos o ID no proporcionado")
    @ApiResponse(responseCode = "404", description = "Departamento no encontrado")
    @ApiResponse(responseCode = "409", description = "Conflicto en actualización")
    ResponseEntity<Departamento> actualizarDepartamento(@PathVariable("id") String id, @Valid @RequestBody Departamento departamento) {
        try {
            if (id == null || id.trim().isEmpty()) {
                Map<String, String> errorResponse = [
                    "error": "BAD_REQUEST",
                    "message": "ID de departamento no proporcionado",
                    "timestamp": new Date().toString()
                ]
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST)
            }
            
            if (departamento == null) {
                Map<String, String> errorResponse = [
                    "error": "BAD_REQUEST",
                    "message": "JSON incompleto o mal formado. Se requiere un objeto departamento válido",
                    "timestamp": new Date().toString()
                ]
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST)
            }
            
            // Verificar que el departamento exista
            Optional<Departamento> departamentoExistente = departamentoRepository.findById(id)
            if (!departamentoExistente.isPresent()) {
                Map<String, String> errorResponse = [
                    "error": "NOT_FOUND",
                    "message": "Departamento con ID '" + id + "' no encontrado",
                    "timestamp": new Date().toString()
                ]
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND)
            }
            
            // Actualizar datos manteniendo el ID original
            Departamento deptToUpdate = departamentoExistente.get()
            deptToUpdate.nombre = departamento.nombre ?: deptToUpdate.nombre
            deptToUpdate.descripcion = departamento.descripcion ?: deptToUpdate.descripcion
            
            Departamento updatedDepartamento = departamentoRepository.save(deptToUpdate)
            return new ResponseEntity<>(updatedDepartamento, HttpStatus.OK)
            
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar departamento",
        description = "Elimina un departamento del sistema"
    )
    @Parameter(name = "id", description = "Identificador único del departamento a eliminar", required = true)
    @ApiResponse(responseCode = "204", description = "Departamento eliminado exitosamente")
    @ApiResponse(responseCode = "400", description = "ID no proporcionado")
    @ApiResponse(responseCode = "404", description = "Departamento no encontrado")
    @ApiResponse(responseCode = "409", description = "No se puede eliminar departamento con empleados asociados")
    ResponseEntity<?> eliminarDepartamento(@PathVariable("id") String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                Map<String, String> errorResponse = [
                    "error": "BAD_REQUEST",
                    "message": "ID de departamento no proporcionado",
                    "timestamp": new Date().toString()
                ]
                return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST)
            }
            
            // Verificar que el departamento exista
            if (!departamentoRepository.existsById(id)) {
                Map<String, String> errorResponse = [
                    "error": "NOT_FOUND",
                    "message": "Departamento con ID '" + id + "' no encontrado",
                    "timestamp": new Date().toString()
                ]
                return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND)
            }
            
            // Esta validación requeriría comunicación con el servicio de empleados
            
            departamentoRepository.deleteById(id)
            return new ResponseEntity<>(HttpStatus.NO_CONTENT)
            
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
    
    @GetMapping
    @Operation(
        summary = "Listar todos los departamentos",
        description = "Recupera una lista de todos los departamentos registrados en el sistema"
    )
    @ApiResponse(responseCode = "200", description = "Lista de departamentos recuperada exitosamente",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Departamento, type = "array")))
    ResponseEntity<List<Departamento>> listarTodosLosDepartamentos() {
        try {
            List<Departamento> departamentos = departamentoRepository.findAll()
            return new ResponseEntity<>(departamentos, HttpStatus.OK)
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }
}