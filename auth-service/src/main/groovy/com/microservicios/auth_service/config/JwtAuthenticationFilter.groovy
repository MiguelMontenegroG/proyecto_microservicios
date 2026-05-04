package com.microservicios.auth_service.config

import com.microservicios.auth_service.service.CustomUserDetailsService
import com.microservicios.auth_service.service.JwtProvider
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class)
    
    private final JwtProvider jwtProvider
    private final CustomUserDetailsService userDetailsService
    
    JwtAuthenticationFilter(JwtProvider jwtProvider, CustomUserDetailsService userDetailsService) {
        this.jwtProvider = jwtProvider
        this.userDetailsService = userDetailsService
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request)
            
            if (StringUtils.hasText(jwt) && jwtProvider.validateToken(jwt)) {
                String username = jwtProvider.getUsernameFromToken(jwt)
                
                UserDetails userDetails = userDetailsService.loadUserByUsername(username)
                
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    )
                
                authentication.details = new WebAuthenticationDetailsSource().buildDetails(request)
                
                SecurityContextHolder.context.authentication = authentication
                
                log.debug("Autenticación establecida para usuario: {}", username)
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
