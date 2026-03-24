package com.microservicios.empleados_service.event

import groovy.transform.ToString
import java.io.Serializable

@ToString(includeNames = true)
class EmpleadoCreadoEvent implements Serializable {
    String id
    String nombre
    String email
    String departamentoId
    String fechaIngreso
    String tipo = 'empleado.creado'
}
