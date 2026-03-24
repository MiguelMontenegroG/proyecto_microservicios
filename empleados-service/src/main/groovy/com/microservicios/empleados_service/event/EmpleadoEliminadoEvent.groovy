package com.microservicios.empleados_service.event

import groovy.transform.ToString
import java.io.Serializable

@ToString(includeNames = true)
class EmpleadoEliminadoEvent implements Serializable {
    String id
    String nombre
    String email
    String tipo = 'empleado.eliminado'
}
