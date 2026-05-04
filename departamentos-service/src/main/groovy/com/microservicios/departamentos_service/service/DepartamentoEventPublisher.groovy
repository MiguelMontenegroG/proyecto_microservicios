package com.microservicios.departamentos_service.service

import com.microservicios.departamentos_service.config.RabbitMQConfig
import com.microservicios.departamentos_service.event.DepartamentoCreadoEvent
import com.microservicios.departamentos_service.event.DepartamentoActualizadoEvent
import com.microservicios.departamentos_service.event.DepartamentoEliminadoEvent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class DepartamentoEventPublisher {
    
    private static final Logger log = LoggerFactory.getLogger(DepartamentoEventPublisher.class)
    
    private final RabbitTemplate rabbitTemplate
    
    DepartamentoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate
    }
    
    void publicarDepartamentoCreado(DepartamentoCreadoEvent evento) {
        try {
            log.info("Publicando evento departamento.creado: {}", evento)
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, '', evento)
            log.info("Evento departamento.creado publicado exitosamente")
        } catch (Exception e) {
            log.error("Error al publicar evento departamento.creado: {}", e.message, e)
        }
    }
    
    void publicarDepartamentoActualizado(DepartamentoActualizadoEvent evento) {
        try {
            log.info("Publicando evento departamento.actualizado: {}", evento)
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, '', evento)
            log.info("Evento departamento.actualizado publicado exitosamente")
        } catch (Exception e) {
            log.error("Error al publicar evento departamento.actualizado: {}", e.message, e)
        }
    }
    
    void publicarDepartamentoEliminado(DepartamentoEliminadoEvent evento) {
        try {
            log.info("Publicando evento departamento.eliminado: {}", evento)
            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, '', evento)
            log.info("Evento departamento.eliminado publicado exitosamente")
        } catch (Exception e) {
            log.error("Error al publicar evento departamento.eliminado: {}", e.message, e)
        }
    }
}
