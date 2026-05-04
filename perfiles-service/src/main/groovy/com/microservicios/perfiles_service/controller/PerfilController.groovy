package com.microservicios.perfiles_service.controller

import com.microservicios.perfiles_service.model.Perfil
import com.microservicios.perfiles_service.repository.PerfilRepository
import com.microservicios.perfiles_service.service.PerfilEventPublisher
import com.microservicios.perfiles_service.event.PerfilCreadoEvent
import com.microservicios.perfiles_service.event.PerfilActualizadoEvent
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/perfiles")
@Tag(name = "Perfiles", description = "Gestión de perfiles de empleados")
@SecurityRequirement(name = "Bearer Authentication")
class PerfilController {
    
    private static final Logger log = LoggerFactory.getLogger(PerfilController.class)
    
    private final PerfilRepository perfilRepository
    private final PerfilEventPublisher eventPublisher
    
    PerfilController(PerfilRepository perfilRepository, PerfilEventPublisher eventPublisher) {
        this.perfilRepository = perfilRepository
        this.eventPublisher = eventPublisher
    }
    
    @GetMapping
    @Operation(summary = 'Listar todos los perfiles', description = 'Retorna una lista con todos los perfiles registrados')
    @ApiResponse(responseCode = '200', description = 'Lista de perfiles obtenida exitosamente')
    ResponseEntity<List<Perfil>> listarPerfiles() {
        log.info("Listando todos los perfiles")
        ResponseEntity.ok(perfilRepository.findAll())
    }
    
    @PostMapping
    @Operation(summary = 'Crear nuevo perfil', description = 'Crea un nuevo perfil para un empleado')
    @ApiResponse(responseCode = '201', description = 'Perfil creado exitosamente')
    ResponseEntity<Perfil> crearPerfil(@RequestBody Perfil perfil) {
        log.info("Creando perfil para empleado {}", perfil.empleadoId)
        
        // Verificar si ya existe un perfil para este empleado
        if (perfilRepository.findByEmpleadoId(perfil.empleadoId).isPresent()) {
            log.warn("Ya existe un perfil para el empleado {}", perfil.empleadoId)
            return new ResponseEntity<>(HttpStatus.CONFLICT)
        }
        
        Perfil perfilCreado = perfilRepository.save(perfil)
        
        // Publicar evento después de crear exitosamente
        PerfilCreadoEvent evento = new PerfilCreadoEvent(
            empleadoId: perfilCreado.empleadoId,
            nombre: perfilCreado.nombre,
            email: perfilCreado.email
        )
        eventPublisher.publicarPerfilCreado(evento)
        
        log.info("Perfil creado exitosamente para empleado {}", perfil.empleadoId)
        return new ResponseEntity<>(perfilCreado, HttpStatus.CREATED)
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
                
                // Publicar evento después de actualizar exitosamente
                PerfilActualizadoEvent evento = new PerfilActualizadoEvent(
                    empleadoId: perfilExistente.empleadoId,
                    nombre: perfilExistente.nombre,
                    email: perfilExistente.email,
                    telefono: perfilExistente.telefono,
                    ciudad: perfilExistente.ciudad
                )
                eventPublisher.publicarPerfilActualizado(evento)
                
                log.info("Perfil actualizado exitosamente para empleado {}", empleadoId)
                ResponseEntity.ok(perfilExistente)
            })
            .orElse(ResponseEntity.notFound().build())
    }
}
