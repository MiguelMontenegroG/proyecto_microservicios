package com.microservicios.departamentos_service.repository

import com.microservicios.departamentos_service.model.Departamento
import org.springframework.data.mongodb.repository.MongoRepository

interface DepartamentoRepository extends MongoRepository<Departamento, String> {
}