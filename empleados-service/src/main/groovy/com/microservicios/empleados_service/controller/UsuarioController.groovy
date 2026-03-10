package com.microservicios.empleados_service.controller

import com.microservicios.empleados_service.model.Usuario
import com.microservicios.empleados_service.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import jakarta.validation.Valid

@RestController
@RequestMapping("/usuarios")
class UsuarioController {

    @Autowired
    UsuarioRepository usuarioRepository

    @PostMapping
    ResponseEntity<Usuario> crearUsuario(@Valid @RequestBody Usuario usuario) {
        // Validaciones adicionales
        if (usuario.username && usuarioRepository.existsByUsername(usuario.username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese username: ${usuario.username}")
        }
        
        if (usuario.email && usuarioRepository.existsByEmail(usuario.email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email: ${usuario.email}")
        }
        
        // Establecer fechas automáticamente
        usuario.fechaCreacion = new Date()
        usuario.fechaActualizacion = new Date()
        
        Usuario savedUsuario = usuarioRepository.save(usuario)
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUsuario)
    }

    @GetMapping("/{id}")
    ResponseEntity<Usuario> obtenerUsuario(@PathVariable String id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow { -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: ${id}") }
        return ResponseEntity.ok(usuario)
    }

    @GetMapping
    ResponseEntity<Map<String, Object>> obtenerTodosUsuarios(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "asc") String sortDir,
        @RequestParam(required = false) String rol,
        @RequestParam(required = false) String nombre
    ) {
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending()
        
        Pageable pageable = PageRequest.of(page, size, sort)
        
        Page<Usuario> usuariosPage
        
        if (rol) {
            usuariosPage = usuarioRepository.findByRol(rol, pageable)
        } else if (nombre) {
            usuariosPage = usuarioRepository.findByNombreCompletoContainingIgnoreCase(nombre, pageable)
        } else {
            usuariosPage = usuarioRepository.findAll(pageable)
        }
        
        Map<String, Object> response = [
            content: usuariosPage.content,
            pageNumber: usuariosPage.number,
            pageSize: usuariosPage.size,
            totalPages: usuariosPage.totalPages,
            totalElements: usuariosPage.totalElements,
            first: usuariosPage.first,
            last: usuariosPage.last,
            hasNext: usuariosPage.hasNext(),
            hasPrevious: usuariosPage.hasPrevious()
        ]
        
        return ResponseEntity.ok(response)
    }

    @PutMapping("/{id}")
    ResponseEntity<Usuario> actualizarUsuario(@PathVariable String id, @Valid @RequestBody Usuario usuarioDetails) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow { -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: ${id}") }
        
        // Verificar unicidad de username y email si se modifican
        if (usuarioDetails.username && !usuarioDetails.username.equals(usuario.username)) {
            if (usuarioRepository.existsByUsername(usuarioDetails.username)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese username: ${usuarioDetails.username}")
            }
            usuario.username = usuarioDetails.username
        }
        
        if (usuarioDetails.email && !usuarioDetails.email.equals(usuario.email)) {
            if (usuarioRepository.existsByEmail(usuarioDetails.email)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con ese email: ${usuarioDetails.email}")
            }
            usuario.email = usuarioDetails.email
        }
        
        usuario.nombreCompleto = usuarioDetails.nombreCompleto ?: usuario.nombreCompleto
        usuario.rol = usuarioDetails.rol ?: usuario.rol
        usuario.edad = usuarioDetails.edad ?: usuario.edad
        usuario.activo = usuarioDetails.activo != null ? usuarioDetails.activo : usuario.activo
        usuario.fechaActualizacion = new Date()
        
        Usuario updatedUsuario = usuarioRepository.save(usuario)
        return ResponseEntity.ok(updatedUsuario)
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> eliminarUsuario(@PathVariable String id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow { -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con ID: ${id}") }
        
        usuarioRepository.delete(usuario)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/username/{username}")
    ResponseEntity<Usuario> obtenerPorUsername(@PathVariable String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow { -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con username: ${username}") }
        return ResponseEntity.ok(usuario)
    }

    @GetMapping("/email/{email}")
    ResponseEntity<Usuario> obtenerPorEmail(@PathVariable String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow { -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con email: ${email}") }
        return ResponseEntity.ok(usuario)
    }
}