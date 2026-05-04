package com.microservicios.perfiles_service.event

import groovy.transform.ToString
import java.io.Serializable

@ToString(includeNames = true)
class PerfilEliminadoEvent implements Serializable {
    String empleadoId
    String nombre
    String tipo = 'perfil.eliminado'
}
