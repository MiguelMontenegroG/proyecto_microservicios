package com.microservicios.empleados_service.config

import com.microservicios.empleados_service.model.Usuario
import com.microservicios.empleados_service.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class DataInitializer implements CommandLineRunner {

    @Autowired
    UsuarioRepository usuarioRepository

    @Override
    void run(String... args) throws Exception {
        // Verificar si ya existen datos
        if (usuarioRepository.count() == 0) {
            println "Inicializando base de datos MongoDB con usuarios de ejemplo..."
            
            List<Usuario> usuarios = [
                crearUsuario("juan.perez", "Juan Pérez", "juan.perez@empresa.com", "Desarrollador", 28),
                crearUsuario("maria.gomez", "María Gómez", "maria.gomez@empresa.com", "Analista", 32),
                crearUsuario("carlos.rodriguez", "Carlos Rodríguez", "carlos.rodriguez@empresa.com", "Gerente", 45),
                crearUsuario("ana.martinez", "Ana Martínez", "ana.martinez@empresa.com", "Diseñadora", 26),
                crearUsuario("luis.hernandez", "Luis Hernández", "luis.hernandez@empresa.com", "Desarrollador", 30),
                crearUsuario("elena.sanchez", "Elena Sánchez", "elena.sanchez@empresa.com", "Tester", 29),
                crearUsuario("pedro.garcia", "Pedro García", "pedro.garcia@empresa.com", "DevOps", 35),
                crearUsuario("sofia.lopez", "Sofía López", "sofia.lopez@empresa.com", "Scrum Master", 31),
                crearUsuario("diego.ruiz", "Diego Ruiz", "diego.ruiz@empresa.com", "Arquitecto", 40),
                crearUsuario("laura.torres", "Laura Torres", "laura.torres@empresa.com", "Product Owner", 33),
                crearUsuario("miguel.castillo", "Miguel Castillo", "miguel.castillo@empresa.com", "Desarrollador", 27),
                crearUsuario("carmen.ramos", "Carmen Ramos", "carmen.ramos@empresa.com", "UX Designer", 29),
                crearUsuario("jorge.mendoza", "Jorge Mendoza", "jorge.mendoza@empresa.com", "QA Engineer", 34),
                crearUsuario("patricia.vasquez", "Patricia Vásquez", "patricia.vasquez@empresa.com", "Business Analyst", 36),
                crearUsuario("ricardo.flores", "Ricardo Flores", "ricardo.flores@empresa.com", "Team Lead", 38)
            ]
            
            usuarioRepository.saveAll(usuarios)
            println "Se han creado ${usuarios.size()} usuarios de ejemplo en MongoDB"
        } else {
            println "La base de datos ya contiene datos (${usuarioRepository.count()} usuarios)"
        }
    }

    private Usuario crearUsuario(String username, String nombreCompleto, String email, String rol, Integer edad) {
        Usuario usuario = new Usuario()
        usuario.username = username
        usuario.nombreCompleto = nombreCompleto
        usuario.email = email
        usuario.rol = rol
        usuario.edad = edad
        usuario.activo = true
        usuario.fechaCreacion = new Date()
        usuario.fechaActualizacion = new Date()
        return usuario
    }
}