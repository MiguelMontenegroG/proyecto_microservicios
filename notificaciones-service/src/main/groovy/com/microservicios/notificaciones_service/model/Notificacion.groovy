package com.microservicios.notificaciones_service.model

import groovy.transform.ToString
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = 'notificaciones')
@ToString(includeNames = true)
class Notificacion {
    
    @Id
    String id
    
    String tipo
    String destinatario
    String mensaje
    LocalDateTime fechaEnvio
    String empleadoId
    
    Notificacion() {
        this.fechaEnvio = LocalDateTime.now()
    }
}
