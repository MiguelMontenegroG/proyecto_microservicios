package com.microservicios.perfiles_service.model

import groovy.transform.ToString
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document(collection = 'perfiles')
@ToString(includeNames = true)
class Perfil {
    
    @Id
    String id
    
    String empleadoId
    String nombre
    String email
    String telefono
    String direccion
    String ciudad
    String biografia
    LocalDateTime fechaCreacion
    
    Perfil() {
        this.fechaCreacion = LocalDateTime.now()
    }
}
