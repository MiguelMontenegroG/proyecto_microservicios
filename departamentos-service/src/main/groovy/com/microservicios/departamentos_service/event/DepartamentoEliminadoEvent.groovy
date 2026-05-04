package com.microservicios.departamentos_service.event

import groovy.transform.ToString
import java.io.Serializable

@ToString(includeNames = true)
class DepartamentoEliminadoEvent implements Serializable {
    String id
    String nombre
    String tipo = 'departamento.eliminado'
}
