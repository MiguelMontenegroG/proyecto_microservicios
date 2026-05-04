package com.microservicios.auth_service.config

import com.microservicios.auth_service.model.Usuario
import com.microservicios.auth_service.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DataInitializer implements CommandLineRunner {

    @Autowired
    UsuarioRepository usuarioRepository

    @Autowired
    PasswordEncoder passwordEncoder

    @Override
    void run(String... args) throws Exception {
        println "=== Inicializando datos ==="

        // Verificar si existe el admin
        def adminExistente = usuarioRepository.findByUsername('admin').orElse(null)
        
        if (adminExistente == null) {
            println "🔧 Creando usuario ADMIN..."
            
            // Crear admin
            Usuario admin = new Usuario(
                username: 'admin',
                password: passwordEncoder.encode('password123'),
                email: 'admin@empresa.com',
                rol: 'ADMIN',
                activo: true,
                fechaCreacion: new Date(),
                fechaModificacion: new Date()
            )
            usuarioRepository.save(admin)
            println "✅ Admin creado: admin / password123"
        } else {
            println "ℹ️ Admin ya existe, saltando creación"
        }

        // Si no hay ningún usuario, crear usuario básico de prueba
        if (usuarioRepository.count() <= 1) {
            println "🔧 Creando usuario USER de prueba..."
            
            Usuario user = new Usuario(
                username: 'user',
                password: passwordEncoder.encode('password123'),
                email: 'user@empresa.com',
                rol: 'USER',
                activo: true,
                fechaCreacion: new Date(),
                fechaModificacion: new Date()
            )
            usuarioRepository.save(user)
            println "✅ User creado: user / password123"
        }

        println "\n=== Inicialización completada ==="
    }
}
