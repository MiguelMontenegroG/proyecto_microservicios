package com.microservicios.auth_service.service

import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserDetailsImpl implements UserDetails {
    
    String username
    String password
    Collection<? extends GrantedAuthority> authorities
    boolean enabled
    
    UserDetailsImpl(String username, String password, 
                   Collection<? extends GrantedAuthority> authorities, 
                   boolean enabled) {
        this.username = username
        this.password = password
        this.authorities = authorities
        this.enabled = enabled
    }
    
    @Override
    Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities
    }
    
    @Override
    String getPassword() {
        return password
    }
    
    @Override
    String getUsername() {
        return username
    }
    
    @Override
    boolean isAccountNonExpired() {
        return true
    }
    
    @Override
    boolean isAccountNonLocked() {
        return true
    }
    
    @Override
    boolean isCredentialsNonExpired() {
        return true
    }
    
    @Override
    boolean isEnabled() {
        return enabled
    }
}
