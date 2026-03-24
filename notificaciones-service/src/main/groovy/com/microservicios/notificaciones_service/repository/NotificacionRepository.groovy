package com.microservicios.notificaciones_service.repository

import com.microservicios.notificaciones_service.model.Notificacion
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

import java.util.List

@Repository
interface NotificacionRepository extends MongoRepository<Notificacion, String> {
    
    List<Notificacion> findByEmpleadoId(String empleadoId)
}
