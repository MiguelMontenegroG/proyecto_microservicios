package com.microservicios.empleados_service.service

import com.microservicios.empleados_service.config.RabbitMQConfig
import com.microservicios.empleados_service.event.EmpleadoCreadoEvent
import com.microservicios.empleados_service.event.EmpleadoEliminadoEvent
import groovy.transform.ToString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
@ToString(includeNames = true)
class EmpleadoEventPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(EmpleadoEventPublisher.class)
    
    private final RabbitTemplate rabbitTemplate
    
    EmpleadoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate
    }
    
    void publicarEmpleadoCreado(EmpleadoCreadoEvent evento) {
        try {
            log.info("Publicando evento empleado.creado: {}", evento)
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, '', evento)
            log.info("Evento empleado.creado publicado exitosamente")
        } catch (Exception e) {
            log.error("Error al publicar evento empleado.creado: {}", e.message, e)
            // NO revertir la base de datos, solo registrar el error
        }
    }
    
    void publicarEmpleadoEliminado(EmpleadoEliminadoEvent evento) {
        try {
            log.info("Publicando evento empleado.eliminado: {}", evento)
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, '', evento)
            log.info("Evento empleado.eliminado publicado exitosamente")
        } catch (Exception e) {
            log.error("Error al publicar evento empleado.eliminado: {}", e.message, e)
            // NO revertir la base de datos, solo registrar el error
        }
    }
}
