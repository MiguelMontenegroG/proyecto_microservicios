package com.microservicios.departamentos_service.controller

import com.microservicios.departamentos_service.model.Departamento
import com.microservicios.departamentos_service.repository.DepartamentoRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

import java.util.Optional

import static org.assertj.core.api.Assertions.*
import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

@ExtendWith(MockitoExtension.class)
class DepartamentoControllerTest {

    @Mock
    DepartamentoRepository departamentoRepository

    @InjectMocks
    DepartamentoController departamentoController

    Departamento departamentoPrueba

    @BeforeEach
    void setUp() {
        departamentoPrueba = new Departamento(
            id: "IT",
            nombre: "Tecnología",
            descripcion: "Departamento de Tecnología e Innovación",
            fechaCreacion: new Date()
        )
    }

    @Test
    void 'crear_departamento_exitoso_cuando_datos_validos'() {
        given(departamentoRepository.existsById("IT")).willReturn(false)
        given(departamentoRepository.save(any(Departamento.class))).willReturn(departamentoPrueba)

        ResponseEntity<Departamento> response = departamentoController.crearDepartamento(departamentoPrueba)

        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED)
        then(response.getBody().id).isEqualTo("IT")
        then(response.getBody().nombre).isEqualTo("Tecnología")
        then(departamentoRepository.save(any(Departamento.class))).should(times(1))
    }

    @Test
    void 'crear_departamento_falla_cuando_ya_existe'() {
        given(departamentoRepository.existsById("IT")).willReturn(true)

        ResponseEntity<Departamento> response = departamentoController.crearDepartamento(departamentoPrueba)

        then(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT)
        then(((Map) response.getBody()).error).isEqualTo("CONFLICT")
    }

    @Test
    void 'obtener_departamento_exitoso_cuando_existe'() {
        given(departamentoRepository.findById("IT")).willReturn(Optional.of(departamentoPrueba))

        ResponseEntity<Departamento> response = departamentoController.obtenerDepartamentoPorId("IT")

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK)
        then(response.getBody().id).isEqualTo("IT")
        then(response.getBody().nombre).isEqualTo("Tecnología")
    }

    @Test
    void 'obtener_departamento_falla_cuando_no_existe'() {
        given(departamentoRepository.findById("IT")).willReturn(Optional.empty())

        ResponseEntity<Departamento> response = departamentoController.obtenerDepartamentoPorId("IT")

        then(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
        then(((Map) response.getBody()).error).isEqualTo("NOT_FOUND")
    }

    @Test
    void 'actualizar_departamento_exitoso_cuando_existe'() {
        Departamento departamentoActualizado = new Departamento(
            id: "IT",
            nombre: "Tecnología Actualizada",
            descripcion: "Descripción actualizada"
        )

        given(departamentoRepository.findById("IT")).willReturn(Optional.of(departamentoPrueba))
        given(departamentoRepository.save(any(Departamento.class))).willReturn(departamentoActualizado)

        ResponseEntity<Departamento> response = departamentoController.actualizarDepartamento("IT", departamentoActualizado)

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK)
        then(response.getBody().nombre).isEqualTo("Tecnología Actualizada")
    }

    @Test
    void 'actualizar_departamento_falla_cuando_no_existe'() {
        Departamento departamentoActualizado = new Departamento(
            id: "DEPT_INEXISTENTE",
            nombre: "Departamento Inexistente",
            descripcion: "No existe"
        )

        given(departamentoRepository.findById("DEPT_INEXISTENTE")).willReturn(Optional.empty())

        ResponseEntity<Departamento> response = departamentoController.actualizarDepartamento("DEPT_INEXISTENTE", departamentoActualizado)

        then(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    void 'actualizar_departamento_falla_cuando_id_es_nulo'() {
        Departamento departamentoActualizado = new Departamento(
            id: null,
            nombre: "Nombre inválido",
            descripcion: "Sin ID"
        )

        ResponseEntity<Departamento> response = departamentoController.actualizarDepartamento(null, departamentoActualizado)

        then(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    void 'eliminar_departamento_exitoso_cuando_existe'() {
        given(departamentoRepository.existsById("IT")).willReturn(true)

        ResponseEntity<?> response = departamentoController.eliminarDepartamento("IT")

        then(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT)
        then(departamentoRepository.deleteById("IT")).should(times(1))
    }

    @Test
    void 'eliminar_departamento_falla_cuando_no_existe'() {
        given(departamentoRepository.existsById("DEPT_INEXISTENTE")).willReturn(false)

        ResponseEntity<?> response = departamentoController.eliminarDepartamento("DEPT_INEXISTENTE")

        then(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
        then(((Map) response.getBody()).error).isEqualTo("NOT_FOUND")
    }

    @Test
    void 'eliminar_departamento_falla_cuando_id_es_nulo'() {
        ResponseEntity<?> response = departamentoController.eliminarDepartamento(null)

        then(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    void 'listar_todos_departamentos_exitoso'() {
        def departamentos = [departamentoPrueba]
        given(departamentoRepository.findAll()).willReturn(departamentos)

        ResponseEntity<List<Departamento>> response = departamentoController.listarTodosLosDepartamentos()

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK)
        then(response.getBody().size()).isEqualTo(1)
        then(response.getBody()[0].id).isEqualTo("IT")
    }

    @Test
    void 'listar_todos_departamentos_retorna_lista_vacia_cuando_no_hay_datos'() {
        given(departamentoRepository.findAll()).willReturn([])

        ResponseEntity<List<Departamento>> response = departamentoController.listarTodosLosDepartamentos()

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK)
        then(response.getBody().size()).isEqualTo(0)
    }

    @Test
    void 'listar_todos_departamentos_maneja_excepcion'() {
        given(departamentoRepository.findAll()).willThrow(new RuntimeException("Error de BD"))

        ResponseEntity<List<Departamento>> response = departamentoController.listarTodosLosDepartamentos()

        then(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
