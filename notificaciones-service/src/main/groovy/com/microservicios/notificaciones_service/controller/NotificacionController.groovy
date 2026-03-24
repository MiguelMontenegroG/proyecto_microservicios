package com.microservicios.notificaciones_service.controller

import com.microservicios.notificaciones_service.model.Notificacion
import com.microservicios.notificaciones_service.repository.NotificacionRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping('/notificaciones')
@Tag(name = 'Notificaciones', description = 'Gestión de notificaciones de empleados')
class NotificacionController {
    
    private static final Logger log = LoggerFactory.getLogger(NotificacionController.class)
    
    private final NotificacionRepository notificacionRepository
    
    NotificacionController(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository
    }
    
    @GetMapping
    @Operation(summary = 'Listar todas las notificaciones', description = 'Retorna una lista con todas las notificaciones registradas')
    @ApiResponse(responseCode = '200', description = 'Lista de notificaciones obtenida exitosamente')
    ResponseEntity<List<Notificacion>> listarNotificaciones() {
        log.info("Listando todas las notificaciones")
        ResponseEntity.ok(notificacionRepository.findAll())
    }
    
    @GetMapping('/{empleadoId}')
    @Operation(summary = 'Consultar notificaciones por empleado', description = 'Retorna las notificaciones asociadas a un empleado específico')
    @ApiResponse(responseCode = '200', description = 'Notificaciones encontradas')
    ResponseEntity<List<Notificacion>> obtenerNotificacionesPorEmpleado(@PathVariable(value = 'empleadoId') String empleadoId) {
        log.info("Consultando notificaciones para empleado {}", empleadoId)
        List<Notificacion> notificaciones = notificacionRepository.findByEmpleadoId(empleadoId)
        ResponseEntity.ok(notificaciones)
    }
}
