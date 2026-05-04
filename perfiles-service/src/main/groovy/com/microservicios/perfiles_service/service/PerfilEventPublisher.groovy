package com.microservicios.perfiles_service.service

import com.microservicios.perfiles_service.config.RabbitMQConfig
import com.microservicios.perfiles_service.event.PerfilCreadoEvent
import com.microservicios.perfiles_service.event.PerfilActualizadoEvent
import com.microservicios.perfiles_service.event.PerfilEliminadoEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class PerfilEventPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(PerfilEventPublisher.class)
    
    private final RabbitTemplate rabbitTemplate
    
    PerfilEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate
    }
    
    void publicarPerfilCreado(PerfilCreadoEvent evento) {
        try {
            log.info("Publicando evento perfil.creado: {}", evento)
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, '', evento)
            log.info("Evento perfil.creado publicado exitosamente")
        } catch (Exception e) {
            log.error("Error al publicar evento perfil.creado: {}", e.message, e)
        }
    }
    
    void publicarPerfilActualizado(PerfilActualizadoEvent evento) {
        try {
            log.info("Publicando evento perfil.actualizado: {}", evento)
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, '', evento)
            log.info("Evento perfil.actualizado publicado exitosamente")
        } catch (Exception e) {
            log.error("Error al publicar evento perfil.actualizado: {}", e.message, e)
        }
    }
    
    void publicarPerfilEliminado(PerfilEliminadoEvent evento) {
        try {
            log.info("Publicando evento perfil.eliminado: {}", evento)
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, '', evento)
            log.info("Evento perfil.eliminado publicado exitosamente")
        } catch (Exception e) {
            log.error("Error al publicar evento perfil.eliminado: {}", e.message, e)
        }
    }
}
