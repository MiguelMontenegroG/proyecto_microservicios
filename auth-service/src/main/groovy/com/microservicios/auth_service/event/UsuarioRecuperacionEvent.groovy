package com.microservicios.auth_service.event

import groovy.transform.ToString
import java.io.Serializable

@ToString(includeNames = true)
class UsuarioRecuperacionEvent implements Serializable {
    String username
    String email
    String resetToken
    String tipo = 'usuario.recuperacion'
}
