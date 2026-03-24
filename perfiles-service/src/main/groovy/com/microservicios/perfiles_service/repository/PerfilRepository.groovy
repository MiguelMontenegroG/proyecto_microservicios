package com.microservicios.perfiles_service.repository

import com.microservicios.perfiles_service.model.Perfil
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

import java.util.Optional

@Repository
interface PerfilRepository extends MongoRepository<Perfil, String> {
    
    Optional<Perfil> findByEmpleadoId(String empleadoId)
    
    boolean existsByEmpleadoId(String empleadoId)
}
