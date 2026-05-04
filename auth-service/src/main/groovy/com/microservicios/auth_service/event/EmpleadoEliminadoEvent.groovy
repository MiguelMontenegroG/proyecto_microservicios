package com.microservicios.auth_service.event

import groovy.transform.ToString
import java.io.Serializable

@ToString(includeNames = true)
class EmpleadoEliminadoEvent implements Serializable {
    String id
    String nombre
    String email
    String tipo = 'empleado.eliminado'
}
