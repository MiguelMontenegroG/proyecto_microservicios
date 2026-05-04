package com.microservicios.auth_service.event

import groovy.transform.ToString
import java.io.Serializable

@ToString(includeNames = true)
class DepartamentoCreadoEvent implements Serializable {
    String id
    String nombre
    String descripcion
    String tipo = 'departamento.creado'
}
