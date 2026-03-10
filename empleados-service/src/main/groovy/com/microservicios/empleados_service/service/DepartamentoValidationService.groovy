package com.microservicios.empleados_service.service

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException
import org.springframework.http.HttpStatus

import java.util.function.Supplier

@Service
class DepartamentoValidationService {

    @Autowired
    RestTemplate restTemplate

    @Autowired
    CircuitBreaker circuitBreaker

    @Value('${departamento.service.url:http://departamentos-service:8081}')
    private String departamentosServiceUrl

    def validarDepartamento(String departamentoId) {
        Supplier<ResponseEntity<Map>> decoratedSupplier = CircuitBreaker.decorateSupplier(
            circuitBreaker,
            () -> restTemplate.getForEntity(
                "${departamentosServiceUrl}/departamentos/${departamentoId}",
                Map.class
            )
        )

        try {
            ResponseEntity<Map> response = decoratedSupplier.get()
            
            if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
                // Departamento no existe - error de validación (400)
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departamento '${departamentoId}' no encontrado. Por favor verifique el ID del departamento.")
            }
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                // Otros errores del servicio de departamentos
                throw new RuntimeException("Error al comunicarse con el servicio de departamentos. Código: ${response.getStatusCode()}")
            }
            
            return response.getBody()
            
        } catch (CallNotPermittedException e) {
            // Circuit breaker está abierto - error de servicio (503)
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Servicio de departamentos temporalmente no disponible. Por favor intente más tarde.")
        } catch (ResourceAccessException e) {
            // Timeout o servicio no disponible - error de servicio (503)
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo conectar con el servicio de departamentos. Verifique que esté en ejecución.")
        } catch (RestClientException e) {
            // Otros errores de cliente REST - error interno (500)
            throw new RuntimeException("Error de comunicación con el servicio de departamentos: ${e.getMessage()}")
        } catch (ResponseStatusException e) {
            // Relanzar excepciones ResponseStatusException tal cual
            throw e
        } catch (Exception e) {
            // Errores inesperados - error interno (500)
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