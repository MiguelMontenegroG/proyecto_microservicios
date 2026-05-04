package com.microservicios.auth_service.event

import groovy.transform.ToString
import java.io.Serializable

@ToString(includeNames = true)
class EmpleadoCreadoEvent implements Serializable {
    String id
    String nombre
    String email
    String departamentoId
    Date fechaIngreso
    String tipo = 'empleado.creado'
}
