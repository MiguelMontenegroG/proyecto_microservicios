package com.microservicios.auth_service.service

import com.microservicios.auth_service.event.UsuarioCreadoEvent
import com.microservicios.auth_service.event.UsuarioRecuperacionEvent
import com.microservicios.auth_service.model.Usuario
import com.microservicios.auth_service.repository.UsuarioRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

import java.util.UUID

@Service
class AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthService.class)
    
    private final UsuarioRepository usuarioRepository
    private final PasswordEncoder passwordEncoder
    private final RabbitTemplate rabbitTemplate
    
    AuthService(UsuarioRepository usuarioRepository, 
                PasswordEncoder passwordEncoder,
                RabbitTemplate rabbitTemplate) {
        this.usuarioRepository = usuarioRepository
        this.passwordEncoder = passwordEncoder
        this.rabbitTemplate = rabbitTemplate
    }
    
    @Transactional
    void crearUsuarioDesdeEmpleado(String empleadoId, String nombre, String email) {
        log.info("Creando usuario para empleado: {} - {}", empleadoId, email)
        
        // Verificar si ya existe el usuario
        if (usuarioRepository.existsByUsername(email)) {
            log.warn("Ya existe un usuario con el email: {}", email)
            return
        }
        
        // Generar username desde el email (parte antes del @)
        String username = email.split('@')[0]
        
        // Generar contraseña temporal aleatoria (se requerirá cambio en primer login)
        String tempPassword = UUID.randomUUID().toString().substring(0, 8)
        
        // Generar token de recuperación para activación de cuenta
        String resetToken = UUID.randomUUID().toString()
        Date resetTokenExpiry = new Date(System.currentTimeMillis() + 3600000) // 1 hora
        
        // Crear usuario con rol USER por defecto
        Usuario usuario = new Usuario(
            username: username,
            password: passwordEncoder.encode(tempPassword),
            email: email,
            nombreCompleto: nombre,
            rol: 'USER',
            activo: true,
            empleadoId: empleadoId,
            resetToken: resetToken,
            resetTokenExpiry: resetTokenExpiry,
            fechaCreacion: new Date(),
            fechaModificacion: new Date()
        )
        
        usuarioRepository.save(usuario)
        log.info("Usuario creado exitosamente: {} con token de activación", username)
        
        // Publicar evento de usuario creado CON el reset token
        try {
            def evento = new UsuarioCreadoEvent(
                username: username,
                email: email,
                rol: 'USER',
                resetToken: resetToken
            )
            rabbitTemplate.convertAndSend('auth.events', '', evento)
            log.info("Evento usuario.creado publicado con token de activación: {}", evento)
        } catch (Exception e) {
            log.error("Error al publicar evento usuario.creado: {}", e.message, e)
            // No revertir la transacción, solo registrar el error
        }
    }
    
    @Transactional
    void inhabilitarUsuario(String empleadoId) {
        log.info("Inhabilitando usuario para empleado: {}", empleadoId)
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findAll().find { it.empleadoId == empleadoId }
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get()
            usuario.activo = false
            usuario.fechaModificacion = new Date()
            usuarioRepository.save(usuario)
            log.info("Usuario inhabilitado: {}", usuario.username)
        } else {
            log.warn("No se encontró usuario para empleado: {}", empleadoId)
        }
    }
    
    @Transactional
    void actualizarUsuario(String empleadoId, String nombre, String email, String departamentoId) {
        log.info("Actualizando usuario para empleado: {} - {}", empleadoId, email)
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findAll().find { it.empleadoId == empleadoId }
        
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get()
            
            // Actualizar datos si cambiaron
            if (nombre != null && !nombre.equals(usuario.nombreCompleto)) {
                usuario.nombreCompleto = nombre
                log.debug("Nombre actualizado: {} -> {}", usuario.nombreCompleto, nombre)
            }
            
            if (email != null && !email.equals(usuario.email)) {
                // Actualizar username basado en nuevo email
                String nuevoUsername = email.split('@')[0]
                usuario.username = nuevoUsername
                usuario.email = email
                log.debug("Email actualizado: {} -> {}", usuario.email, email)
            }
            
            usuario.fechaModificacion = new Date()
            usuarioRepository.save(usuario)
            log.info("Usuario actualizado exitosamente: {}", usuario.username)
        } else {
            log.warn("No se encontró usuario para empleado: {}", empleadoId)
        }
    }
    
    @Transactional(readOnly = true)
    Usuario obtenerUsuarioPorUsername(String username) {
        return usuarioRepository.findByUsername(username).orElse(null)
    }
    
    @Transactional
    Map<String, Object> solicitarRecuperacionContrasena(String email) {
        log.info("Solicitando recuperación de contraseña para email: {}", email)
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email)
        
        if (!usuarioOpt.isPresent()) {
            log.warn("No existe usuario con email: {}", email)
            return [existe: false, resetToken: null] // Para pruebas: indicar que no existe
        }
        
        Usuario usuario = usuarioOpt.get()
        
        if (!usuario.activo) {
            log.warn("Usuario inactivo: {}", email)
            return [existe: false, resetToken: null, motivo: "usuario_inactivo"]
        }
        
        // Generar token de recuperación
        String resetToken = UUID.randomUUID().toString()
        usuario.resetToken = resetToken
        usuario.resetTokenExpiry = new Date(System.currentTimeMillis() + 3600000) // 1 hora
        usuario.fechaModificacion = new Date()
        
        usuarioRepository.save(usuario)
        log.info("Token de recuperación generado para usuario: {}", usuario.username)
        
        // Publicar evento para notificación
        try {
            def evento = new UsuarioRecuperacionEvent(
                username: usuario.username,
                email: usuario.email,
                resetToken: resetToken
            )
            rabbitTemplate.convertAndSend('auth.events', '', evento)
            log.info("Evento usuario.recuperacion publicado: {}", evento)
        } catch (Exception e) {
            log.error("Error al publicar evento usuario.recuperacion: {}", e.message, e)
        }
        
        return [existe: true, resetToken: resetToken]
    }
    
    @Transactional
    boolean restablecerContrasena(String token, String nuevaContrasena) {
        log.info("Intentando restablecer contraseña con token")
        
        Optional<Usuario> usuarioOpt = usuarioRepository.findByResetToken(token)
        
        if (!usuarioOpt.isPresent()) {
            log.error("Token de recuperación inválido")
            return false
        }
        
        Usuario usuario = usuarioOpt.get()
        
        // Verificar expiración del token
        if (usuario.resetTokenExpiry.before(new Date())) {
            log.error("Token de recuperación expirado")
            return false
        }
        
        // Actualizar contraseña
        usuario.password = passwordEncoder.encode(nuevaContrasena)
        usuario.resetToken = null
        usuario.resetTokenExpiry = null
        usuario.fechaModificacion = new Date()
        
        usuarioRepository.save(usuario)
        log.info("Contraseña restablecida exitosamente para usuario: {}", usuario.username)
        
        return true
    }
}
