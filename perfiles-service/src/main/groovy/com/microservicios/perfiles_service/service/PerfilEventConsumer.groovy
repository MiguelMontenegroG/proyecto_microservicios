package com.microservicios.perfiles_service.service

import com.microservicios.perfiles_service.model.Perfil
import com.microservicios.perfiles_service.repository.PerfilRepository
import groovy.transform.ToString
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Service

@Service
@ToString(includeNames = true)
class PerfilEventConsumer {
    
    private static final Logger log = LoggerFactory.getLogger(PerfilEventConsumer.class)
    
    private final PerfilRepository perfilRepository
    
    PerfilEventConsumer(PerfilRepository perfilRepository) {
        this.perfilRepository = perfilRepository
    }
    
    @RabbitListener(queues = ['cola.perfiles'])
    void procesarEventoEmpleado(Map evento) {
        log.info("Evento recibido en cola.perfiles: {}", evento)
        
        String tipoEvento = evento.get('tipo')
        
        if (tipoEvento == 'empleado.creado') {
            crearPerfilDesdeEvento(evento)
        } else {
            log.info("Evento {} no procesado por este consumidor", tipoEvento)
        }
    }
    
    private void crearPerfilDesdeEvento(Map evento) {
        String empleadoId = evento.get('id')
        
        if (perfilRepository.existsByEmpleadoId(empleadoId)) {
            log.warn("Ya existe un perfil para el empleado {}", empleadoId)
            return
        }
        
        Perfil perfil = new Perfil(
            empleadoId: empleadoId,
            nombre: evento.get('nombre'),
            email: evento.get('email'),
            telefono: '',
            direccion: '',
            ciudad: '',
            biografia: ''
        )
        
        perfilRepository.save(perfil)
        log.info("Perfil creado automáticamente para empleado {}", empleadoId)
    }
}
