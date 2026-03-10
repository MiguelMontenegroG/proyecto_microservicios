package com.microservicios.departamentos_service.service

import com.microservicios.departamentos_service.model.Departamento
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate

import java.util.function.Supplier

@Service
class DepartamentoCommunicationService {

    @Autowired
    RestTemplate restTemplate

    @Autowired
    CircuitBreaker circuitBreaker

    def validarDepartamento(String departamentoId) {
        Supplier<ResponseEntity<Map>> decoratedSupplier = CircuitBreaker.decorateSupplier(
            circuitBreaker,
            () -> restTemplate.getForEntity(
                "http://localhost:8081/departamentos/${departamentoId}",
                Map.class
            )
        )

        try {
            ResponseEntity<Map> response = decoratedSupplier.get()
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Departamento no encontrado: ${departamentoId}")
            }
            return response.getBody()
        } catch (CallNotPermittedException e) {
            // Circuit breaker está abierto
            throw new RuntimeException("Servicio de departamentos temporalmente no disponible. Por favor intente más tarde.")
        } catch (ResourceAccessException e) {
            // Timeout o servicio no disponible
            throw new RuntimeException("No se pudo conectar con el servicio de departamentos. Verifique que esté en ejecución.")
        } catch (RestClientException e) {
            // Otros errores de cliente REST
            throw new RuntimeException("Error de comunicación con el servicio de departamentos: ${e.getMessage()}")
        } catch (Exception e) {
            // Errores inesperados
            throw new RuntimeException("Error inesperado al validar departamento: ${e.getMessage()}")
        }
    }

    boolean isCircuitBreakerClosed() {
        return circuitBreaker.getState() == CircuitBreaker.State.CLOSED
    }

    String getCircuitBreakerState() {
        return circuitBreaker.getState().toString()
    }
}