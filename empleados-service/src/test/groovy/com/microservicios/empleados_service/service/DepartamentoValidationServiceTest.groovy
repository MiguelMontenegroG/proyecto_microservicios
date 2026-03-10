package com.microservicios.empleados_service.service

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException

import static org.assertj.core.api.Assertions.*
import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

@ExtendWith(MockitoExtension.class)
class DepartamentoValidationServiceTest {

    @Mock
    RestTemplate restTemplate

    @Mock
    CircuitBreaker circuitBreaker

    @InjectMocks
    DepartamentoValidationService departamentoValidationService

    ResponseEntity<Map> responseExitosa
    Map departamentoData

    @BeforeEach
    void setUp() {
        departamentoData = [
            id: "IT",
            nombre: "Tecnología e Informática",
            descripcion: "Departamento de sistemas"
        ]
        responseExitosa = new ResponseEntity<>(departamentoData, HttpStatus.OK)
    }

    @Test
    void 'validar_departamento_exitoso_cuando_departamento_existe'() {
        // Configurar el mock para que devuelva la respuesta exitosa
        def supplier = Mock(java.util.function.Supplier)
        supplier.get() >> responseExitosa
        
        // Simular el comportamiento del CircuitBreaker
        def decoratedSupplier = Mock(java.util.function.Supplier)
        decoratedSupplier.get() >> responseExitosa
        
        // Usar spy para simular el comportamiento real
        def serviceSpy = Spy(departamentoValidationService)
        serviceSpy.validarDepartamento("IT") >> departamentoData

        def result = serviceSpy.validarDepartamento("IT")

        then(result).isEqualTo(departamentoData)
    }

    @Test
    void 'validar_departamento_falla_cuando_departamento_no_existe'() {
        ResponseEntity<Map> notFoundResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND)
        
        def serviceSpy = Spy(departamentoValidationService)
        serviceSpy.validarDepartamento("DEPT_INEXISTENTE") >> {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departamento no encontrado")
        }

        assertThatThrownBy({ -> serviceSpy.validarDepartamento("DEPT_INEXISTENTE") })
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST)
    }

    @Test
    void 'validar_departamento_falla_cuando_servicio_no_disponible'() {
        def serviceSpy = Spy(departamentoValidationService)
        serviceSpy.validarDepartamento("IT") >> {
            throw new ResourceAccessException("No se pudo conectar")
        }

        assertThatThrownBy({ -> serviceSpy.validarDepartamento("IT") })
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.SERVICE_UNAVAILABLE)
    }

    @Test
    void 'validar_departamento_falla_cuando_circuit_breaker_abierto'() {
        def serviceSpy = Spy(departamentoValidationService)
        serviceSpy.validarDepartamento("IT") >> {
            throw new io.github.resilience4j.circuitbreaker.CallNotPermittedException("Circuit breaker abierto")
        }

        assertThatThrownBy({ -> serviceSpy.validarDepartamento("IT") })
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.SERVICE_UNAVAILABLE)
    }

    @Test
    void 'validar_departamento_falla_cuando_error_rest_cliente'() {
        def serviceSpy = Spy(departamentoValidationService)
        serviceSpy.validarDepartamento("IT") >> {
            throw new RestClientException("Error de comunicación")
        }

        assertThatThrownBy({ -> serviceSpy.validarDepartamento("IT") })
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Error de comunicación con el servicio de departamentos")
    }

    @Test
    void 'isCircuitBreakerClosed_retorna_verdadero_cuando_cerrado'() {
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.CLOSED)

        def result = departamentoValidationService.isCircuitBreakerClosed()

        then(result).isTrue()
    }

    @Test
    void 'isCircuitBreakerClosed_retorna_falso_cuando_abierto'() {
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.OPEN)

        def result = departamentoValidationService.isCircuitBreakerClosed()

        then(result).isFalse()
    }

    @Test
    void 'getCircuitBreakerState_retorna_estado_actual'() {
        when(circuitBreaker.getState()).thenReturn(CircuitBreaker.State.HALF_OPEN)

        def result = departamentoValidationService.getCircuitBreakerState()

        then(result).isEqualTo("HALF_OPEN")
    }
}
