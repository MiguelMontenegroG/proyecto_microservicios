package com.microservicios.auth_service.repository

import com.microservicios.auth_service.model.Usuario
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface UsuarioRepository extends MongoRepository<Usuario, String> {
    
    Page<Usuario> findByActivo(Boolean activo, Pageable pageable)
    
    Page<Usuario> findByRol(String rol, Pageable pageable)
    
    Page<Usuario> findByUsernameContainingIgnoreCase(String username, Pageable pageable)
    
    Optional<Usuario> findByUsername(String username)
    
    Optional<Usuario> findByEmail(String email)
    
    boolean existsByUsername(String username)
    
    boolean existsByEmail(String email)
    
    Optional<Usuario> findByResetToken(String resetToken)
}
