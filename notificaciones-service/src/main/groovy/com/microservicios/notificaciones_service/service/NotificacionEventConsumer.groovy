package com.microservicios.notificaciones_service.service

import com.microservicios.notificaciones_service.model.Notificacion
import com.microservicios.notificaciones_service.repository.NotificacionRepository
import groovy.transform.ToString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
@ToString(includeNames = true)
class NotificacionEventConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(NotificacionEventConsumer.class)
    
    private final NotificacionRepository notificacionRepository
    
    NotificacionEventConsumer(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository
    }
    
    @RabbitListener(queues = ['cola.notificaciones'])
    void procesarEventoEmpleado(Map evento) {
        log.info("Evento recibido en cola.notificaciones: {}", evento)
        
        String tipoEvento = evento.get('tipo')
        
        if (tipoEvento == 'empleado.creado') {
            procesarEmpleadoCreado(evento)
        } else if (tipoEvento == 'empleado.eliminado') {
            procesarEmpleadoEliminado(evento)
        } else {
            log.info("Evento {} no reconocido", tipoEvento)
        }
    }
    
    private void procesarEmpleadoCreado(Map evento) {
        String empleadoId = evento.get('id')
        String nombre = evento.get('nombre')
        String email = evento.get('email')
        
        String mensaje = "Bienvenido ${nombre}"
        
        Notificacion notificacion = new Notificacion(
            tipo: 'BIENVENIDA',
            destinatario: email,
            mensaje: mensaje,
            empleadoId: empleadoId
        )
        
        notificacionRepository.save(notificacion)
        
        log.info("""
[NOTIFICACIÓN]
Tipo: BIENVENIDA
Para: ${email}
Mensaje: ${mensaje}
""")
    }
    
    private void procesarEmpleadoEliminado(Map evento) {
        String empleadoId = evento.get('id')
        String nombre = evento.get('nombre')
        String email = evento.get('email')
        
        String mensaje = "Notificación de desvinculación: ${nombre}"
        
        Notificacion notificacion = new Notificacion(
            tipo: 'DESVINCULACION',
            destinatario: email,
            mensaje: mensaje,
            empleadoId: empleadoId
        )
        
        notificacionRepository.save(notificacion)
        
        log.info("""
[NOTIFICACIÓN]
Tipo: DESVINCULACION
Para: ${email}
Mensaje: ${mensaje}
""")
    }
}
