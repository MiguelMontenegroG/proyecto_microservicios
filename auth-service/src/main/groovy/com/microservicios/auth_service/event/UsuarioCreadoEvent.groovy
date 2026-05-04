package com.microservicios.auth_service.event

import groovy.transform.ToString
import java.io.Serializable

@ToString(includeNames = true)
class UsuarioCreadoEvent implements Serializable {
    String username
    String email
    String rol
    String resetToken
    String tipo = 'usuario.creado'
}
