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
        } else if (tipoEvento == 'usuario.creado') {
            procesarUsuarioCreado(evento)
        } else if (tipoEvento == 'usuario.recuperacion') {
            procesarUsuarioRecuperacion(evento)
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
    
    private void procesarUsuarioCreado(Map evento) {
        String username = evento.get('username')
        String email = evento.get('email')
        String resetToken = evento.get('resetToken')
        
        // Simular envío de email con link de activación
        String mensaje = "Bienvenido ${username}. Para activar tu cuenta y establecer tu contraseña, usa el siguiente token: ${resetToken}"
        String linkActivacion = "http://localhost:8085/auth/reset-password (usar token: ${resetToken})"
        
        Notificacion notificacion = new Notificacion(
            tipo: 'ACTIVACION_CUENTA',
            destinatario: email,
            mensaje: mensaje,
            empleadoId: null
        )
        
        notificacionRepository.save(notificacion)
        
        log.info("""
========================================
[SIMULACIÓN DE EMAIL - ACTIVACIÓN DE CUENTA]
========================================
Para: ${email}
Username: ${username}
Token de Activación: ${resetToken}
Link de Activación: ${linkActivacion}

INSTRUCCIONES:
1. Ve a: http://localhost:8085/swagger-ui.html
2. Usa el endpoint POST /auth/reset-password
3. Body: {"token": "${resetToken}", "newPassword": "tu_password"}
4. Luego podrás hacer login con tu nueva contraseña
========================================
""")
    }
    
    private void procesarUsuarioRecuperacion(Map evento) {
        String username = evento.get('username')
        String email = evento.get('email')
        String resetToken = evento.get('resetToken')
        
        // Simular envío de email de recuperación
        String mensaje = "Hola ${username}. Has solicitado recuperar tu contraseña. Usa el siguiente token: ${resetToken}"
        String linkRecuperacion = "http://localhost:8085/auth/reset-password (usar token: ${resetToken})"
        
        Notificacion notificacion = new Notificacion(
            tipo: 'RECUPERACION_PASSWORD',
            destinatario: email,
            mensaje: mensaje,
            empleadoId: null
        )
        
        notificacionRepository.save(notificacion)
        
        log.info("""
========================================
[SIMULACIÓN DE EMAIL - RECUPERACIÓN DE CONTRASEÑA]
========================================
Para: ${email}
Username: ${username}
Token de Recuperación: ${resetToken}
Link de Recuperación: ${linkRecuperacion}

INSTRUCCIONES:
1. Ve a: http://localhost:8085/swagger-ui.html
2. Usa el endpoint POST /auth/reset-password
3. Body: {"token": "${resetToken}", "newPassword": "nueva_password"}
========================================
""")
    }
}
