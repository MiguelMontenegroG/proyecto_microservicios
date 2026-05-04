package com.microservicios.auth_service.event

import groovy.transform.ToString
import java.io.Serializable

@ToString(includeNames = true)
class PerfilCreadoEvent implements Serializable {
    String empleadoId
    String nombre
    String email
    String tipo = 'perfil.creado'
}
