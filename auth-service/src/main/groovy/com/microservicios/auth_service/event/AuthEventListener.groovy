package com.microservicios.auth_service.event

import com.microservicios.auth_service.service.AuthService
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class AuthEventListener {
    
    private static final Logger log = LoggerFactory.getLogger(AuthEventListener.class)
    private static final ObjectMapper objectMapper = new ObjectMapper()
    
    private final AuthService authService
    
    AuthEventListener(AuthService authService) {
        this.authService = authService
    }
    
    @RabbitListener(queues = ['#{authServiceQueue.name}'], containerFactory = 'rabbitListenerContainerFactory')
    void procesarEvento(byte[] messageBody) {
        try {
            // Convertir byte[] a String y luego a Map manualmente
            String jsonMessage = new String(messageBody, 'UTF-8')
            log.info("Mensaje recibido (raw): {}", jsonMessage)
            
            Map evento = objectMapper.readValue(jsonMessage, Map.class)
            log.info("Evento parseado: {}", evento)
            
            String tipoEvento = evento.get('tipo')
            
            if (tipoEvento == 'empleado.creado') {
                handleEmpleadoCreado(evento)
            } else if (tipoEvento == 'empleado.eliminado') {
                handleEmpleadoEliminado(evento)
            } else if (tipoEvento == 'empleado.actualizado') {
                handleEmpleadoActualizado(evento)
            } else if (tipoEvento == 'departamento.creado') {
                log.info("Evento departamento.creado ignorado (no requiere acción)")
            } else if (tipoEvento == 'departamento.eliminado') {
                log.info("Evento departamento.eliminado ignorado (no requiere acción)")
            } else if (tipoEvento == 'perfil.creado') {
                log.info("Evento perfil.creado ignorado (no requiere acción)")
            } else {
                log.warn("Evento no reconocido: {}", tipoEvento)
            }
        } catch (Exception e) {
            log.error("Error al procesar evento: {}", e.message, e)
        }
    }
    
    private void handleEmpleadoCreado(Map evento) {
        log.info("Procesando evento empleado.creado: {}", evento)
        
        authService.crearUsuarioDesdeEmpleado(
            evento.get('id'),
            evento.get('nombre'),
            evento.get('email')
        )
    }
    
    private void handleEmpleadoEliminado(Map evento) {
        log.info("Procesando evento empleado.eliminado: {}", evento)
        
        authService.inhabilitarUsuario(evento.get('id'))
    }
    
    private void handleEmpleadoActualizado(Map evento) {
        log.info("Procesando evento empleado.actualizado: {}", evento)
        
        authService.actualizarUsuario(
            evento.get('id'),
            evento.get('nombre'),
            evento.get('email'),
            evento.get('departamentoId')
        )
    }
}
