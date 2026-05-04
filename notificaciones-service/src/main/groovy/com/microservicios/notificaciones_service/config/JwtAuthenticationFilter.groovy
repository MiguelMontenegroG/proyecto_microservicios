package com.microservicios.notificaciones_service.config

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class)
    
    private final JwtProvider jwtProvider
    
    JwtAuthenticationFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request)
            
            if (StringUtils.hasText(jwt) && jwtProvider.validateToken(jwt)) {
                String username = jwtProvider.getUsernameFromToken(jwt)
                String role = jwtProvider.getRoleFromToken(jwt)
                
                def authorities = [new SimpleGrantedAuthority(role)]
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities
                    )
                
                SecurityContextHolder.context.authentication = authentication
                
                log.debug("Autenticación establecida para usuario: {} con rol: {}", username, role)
            }
        } catch (Exception ex) {
            log.error("No se pudo establecer autenticación del usuario: {}", ex.message)
        }
        
        filterChain.doFilter(request, response)
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader('Authorization')
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith('Bearer ')) {
            return bearerToken.substring(7)
        }
        
        return null
    }
}
