package com.microservicios.notificaciones_service.config

import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.stereotype.Component

import java.security.Key
import java.util.Date

@Component
class JwtProvider {
    
    private static final Logger log = LoggerFactory.getLogger(JwtProvider.class)
    
    @Value('${jwt.secret:mi_clave_secreta_muy_larga_para_jwt_con_mas_de_32_caracteres_123456}')
    private String jwtSecret
    
    @Value('${jwt.expiration.ms:3600000}')
    private long jwtExpirationMs
    
    Key getSigningKey() {
        byte[] keyBytes = jwtSecret.bytes
        return Keys.hmacShaKeyFor(keyBytes)
    }
    
    String getUsernameFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .body
        
        return claims.subject
    }
    
    String getRoleFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .body
        
        return claims.get('role', String)
    }
    
    boolean validateToken(String authToken) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(authToken)
            return true
        } catch (SignatureException e) {
            log.error("Firma JWT inválida: {}", e.message)
        } catch (MalformedJwtException e) {
            log.error("Token JWT inválido: {}", e.message)
        } catch (ExpiredJwtException e) {
            log.error("Token JWT expirado: {}", e.message)
        } catch (UnsupportedJwtException e) {
            log.error("Token JWT no soportado: {}", e.message)
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string vacío: {}", e.message)
        }
        return false
    }
}
