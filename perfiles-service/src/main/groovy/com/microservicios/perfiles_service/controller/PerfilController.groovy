package com.microservicios.perfiles_service.controller

import com.microservicios.perfiles_service.model.Perfil
import com.microservicios.perfiles_service.repository.PerfilRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping('/perfiles')
@Tag(name = 'Perfiles', description = 'Gestión de perfiles de empleados')
class PerfilController {
    
    private static final Logger log = LoggerFactory.getLogger(PerfilController.class)
    
    private final PerfilRepository perfilRepository
    
    PerfilController(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository
    }
    
    @GetMapping
    @Operation(summary = 'Listar todos los perfiles', description = 'Retorna una lista con todos los perfiles registrados')
    @ApiResponse(responseCode = '200', description = 'Lista de perfiles obtenida exitosamente')
    ResponseEntity<List<Perfil>> listarPerfiles() {
        log.info("Listando todos los perfiles")
        ResponseEntity.ok(perfilRepository.findAll())
    }
    
    @GetMapping('/{empleadoId}')
    @Operation(summary = 'Consultar perfil por empleado', description = 'Retorna el perfil asociado a un empleado específico')
    @ApiResponse(responseCode = '200', description = 'Perfil encontrado')
    @ApiResponse(responseCode = '404', description = 'Perfil no encontrado')
    ResponseEntity<Perfil> obtenerPerfil(@PathVariable(value = 'empleadoId') String empleadoId) {
        log.info("Consultando perfil para empleado {}", empleadoId)
        
        return perfilRepository.findByEmpleadoId(empleadoId)
            .map({ perfil -> 
                log.debug("Perfil encontrado: {}", perfil)
                ResponseEntity.ok(perfil)
            })
            .orElse(ResponseEntity.notFound().build())
    }
    
    @PutMapping('/{empleadoId}')
    @Operation(summary = 'Actualizar perfil', description = 'Actualiza la información del perfil de un empleado')
    @ApiResponse(responseCode = '200', description = 'Perfil actualizado exitosamente')
    @ApiResponse(responseCode = '404', description = 'Perfil no encontrado')
    ResponseEntity<Perfil> actualizarPerfil(
            @PathVariable(value = 'empleadoId') String empleadoId,
            @RequestBody Perfil perfilActualizado) {
        
        log.info("Actualizando perfil para empleado {}", empleadoId)
        
        return perfilRepository.findByEmpleadoId(empleadoId)
            .map({ perfilExistente ->
                perfilExistente.nombre = perfilActualizado.nombre ?: perfilExistente.nombre
                perfilExistente.email = perfilActualizado.email ?: perfilExistente.email
                perfilExistente.telefono = perfilActualizado.telefono != null ? perfilActualizado.telefono : perfilExistente.telefono
                perfilExistente.direccion = perfilActualizado.direccion != null ? perfilActualizado.direccion : perfilExistente.direccion
                perfilExistente.ciudad = perfilActualizado.ciudad != null ? perfilActualizado.ciudad : perfilExistente.ciudad
                perfilExistente.biografia = perfilActualizado.biografia != null ? perfilActualizado.biografia : perfilExistente.biografia
                
                perfilRepository.save(perfilExistente)
                log.info("Perfil actualizado exitosamente para empleado {}", empleadoId)
                ResponseEntity.ok(perfilExistente)
            })
            .orElse(ResponseEntity.notFound().build())
    }
}
