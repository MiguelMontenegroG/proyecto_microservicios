package com.microservicios.perfiles_service.event

import groovy.transform.ToString
import java.io.Serializable

@ToString(includeNames = true)
class PerfilActualizadoEvent implements Serializable {
    String empleadoId
    String nombre
    String email
    String telefono
    String ciudad
    String tipo = 'perfil.actualizado'
}
