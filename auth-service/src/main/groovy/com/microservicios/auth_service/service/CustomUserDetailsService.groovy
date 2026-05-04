package com.microservicios.auth_service.service

import com.microservicios.auth_service.model.Usuario
import com.microservicios.auth_service.repository.UsuarioRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CustomUserDetailsService implements UserDetailsService {
    
    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class)
    
    private final UsuarioRepository usuarioRepository
    
    CustomUserDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository
    }
    
    @Override
    @Transactional(readOnly = true)
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Cargando usuario: {}", username)
        
        Usuario usuario = usuarioRepository.findByUsername(username)
            .orElseThrow { 
                log.error("Usuario no encontrado: {}", username)
                new UsernameNotFoundException("Usuario no encontrado: ${username}")
            }
        
        if (!usuario.activo) {
            log.error("Usuario inactivo: {}", username)
            throw new UsernameNotFoundException("Usuario inactivo: ${username}")
        }
        
        List<SimpleGrantedAuthority> authorities = [new SimpleGrantedAuthority(usuario.rol)]
        
        return new UserDetailsImpl(
            usuario.username,
            usuario.password,
            authorities,
            usuario.activo
        )
    }
}
