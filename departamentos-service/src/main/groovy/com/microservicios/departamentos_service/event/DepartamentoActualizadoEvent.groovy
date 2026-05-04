package com.microservicios.departamentos_service.event

import groovy.transform.ToString
import java.io.Serializable

@ToString(includeNames = true)
class DepartamentoActualizadoEvent implements Serializable {
    String id
    String nombre
    String descripcion
    String tipo = 'departamento.actualizado'
}
