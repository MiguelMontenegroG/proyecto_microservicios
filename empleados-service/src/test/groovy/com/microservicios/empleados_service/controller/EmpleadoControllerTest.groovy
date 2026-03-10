package com.microservicios.empleados_service.controller

import com.microservicios.empleados_service.model.Empleado
import com.microservicios.empleados_service.model.Usuario
import com.microservicios.empleados_service.repository.UsuarioRepository
import com.microservicios.empleados_service.service.DepartamentoValidationService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.server.ResponseStatusException

import java.util.Optional

import static org.assertj.core.api.Assertions.*
import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

@ExtendWith(MockitoExtension.class)
class EmpleadoControllerTest {

    @Mock
    UsuarioRepository usuarioRepository

    @Mock
    DepartamentoValidationService departamentoValidationService

    @InjectMocks
    EmpleadoController empleadoController

    Empleado empleadoPrueba
    Usuario usuarioPrueba

    @BeforeEach
    void setUp() {
        empleadoPrueba = new Empleado(
            id: "EMP001",
            nombre: "Juan Pérez",
            email: "juan.perez@empresa.com",
            departamentoId: "IT",
            fechaIngreso: new Date(),
            fechaCreacion: new Date()
        )

        usuarioPrueba = new Usuario(
            id: "EMP001",
            username: "juan.perez",
            nombreCompleto: "Juan Pérez",
            email: "juan.perez@empresa.com",
            rol: "Desarrollador",
            departamentoId: "IT",
            edad: 28,
            activo: true,
            fechaCreacion: new Date(),
            fechaActualizacion: new Date()
        )
    }

    @Test
    void 'registrar_empleado_exitoso_cuando_datos_validos'() {
        given(departamentoValidationService.validarDepartamento("IT")).willReturn(null)
        given(usuarioRepository.existsById("EMP001")).willReturn(false)
        given(usuarioRepository.save(any(Usuario.class))).willReturn(usuarioPrueba)

        ResponseEntity<Empleado> response = empleadoController.registrar(empleadoPrueba)

        then(response.getStatusCode()).isEqualTo(HttpStatus.CREATED)
        then(response.getBody().id).isEqualTo("EMP001")
        then(response.getBody().nombre).isEqualTo("Juan Pérez")
        then(departamentoValidationService.validarDepartamento("IT")).should(times(1))
        then(usuarioRepository.save(any(Usuario.class))).should(times(1))
    }

    @Test
    void 'registrar_empleado_falla_cuando_empleado_ya_existe'() {
        given(usuarioRepository.existsById("EMP001")).willReturn(true)

        assertThatThrownBy({ -> empleadoController.registrar(empleadoPrueba) })
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT)
    }

    @Test
    void 'registrar_empleado_falla_cuando_departamento_no_existe'() {
        given(usuarioRepository.existsById("EMP001")).willReturn(false)
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departamento no encontrado"))
            .when(departamentoValidationService).validarDepartamento("DEPT_INEXISTENTE")

        empleadoPrueba.departamentoId = "DEPT_INEXISTENTE"

        assertThatThrownBy({ -> empleadoController.registrar(empleadoPrueba) })
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST)
    }

    @Test
    void 'obtener_empleado_exitoso_cuando_existe'() {
        given(usuarioRepository.findById("EMP001")).willReturn(Optional.of(usuarioPrueba))

        ResponseEntity<?> response = empleadoController.obtener("EMP001")

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK)
        then(((Empleado) response.getBody()).id).isEqualTo("EMP001")
        then(((Empleado) response.getBody()).nombre).isEqualTo("Juan Pérez")
    }

    @Test
    void 'obtener_empleado_falla_cuando_no_existe'() {
        given(usuarioRepository.findById("EMP001")).willReturn(Optional.empty())

        assertThatThrownBy({ -> empleadoController.obtener("EMP001") })
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND)
    }

    @Test
    void 'obtener_empleado_falla_cuando_id_es_nulo'() {
        assertThatThrownBy({ -> empleadoController.obtener(null) })
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST)
    }

    @Test
    void 'actualizar_empleado_exitoso_cuando_existe'() {
        Empleado empleadoActualizado = new Empleado(
            id: "EMP001",
            nombre: "Juan Pérez Actualizado",
            email: "juan.actualizado@empresa.com",
            departamentoId: "IT"
        )

        given(usuarioRepository.findById("EMP001")).willReturn(Optional.of(usuarioPrueba))
        given(departamentoValidationService.validarDepartamento("IT")).willReturn(null)
        given(usuarioRepository.save(any(Usuario.class))).willReturn(usuarioPrueba)

        ResponseEntity<Empleado> response = empleadoController.actualizar("EMP001", empleadoActualizado)

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK)
        then(response.getBody().nombre).isEqualTo("Juan Pérez Actualizado")
        then(usuarioRepository.save(any(Usuario.class))).should(times(1))
    }

    @Test
    void 'actualizar_empleado_falla_cuando_no_existe'() {
        Empleado empleadoActualizado = new Empleado(
            id: "EMP999",
            nombre: "Empleado Inexistente",
            email: "inexistente@empresa.com",
            departamentoId: "IT"
        )

        given(usuarioRepository.findById("EMP999")).willReturn(Optional.empty())

        assertThatThrownBy({ -> empleadoController.actualizar("EMP999", empleadoActualizado) })
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND)
    }

    @Test
    void 'eliminar_empleado_falla_por_metodo_no_permitido'() {
        given(usuarioRepository.existsById("EMP001")).willReturn(true)

        assertThatThrownBy({ -> empleadoController.eliminar("EMP001") })
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.METHOD_NOT_ALLOWED)
    }

    @Test
    void 'listar_todos_empleados_exitoso'() {
        given(usuarioRepository.findAll()).willReturn([usuarioPrueba])

        ResponseEntity<List<Empleado>> response = empleadoController.listarTodos("0", "10")

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK)
        then(response.getBody().size()).isEqualTo(1)
        then(response.getBody()[0].id).isEqualTo("EMP001")
    }

    @Test
    void 'listar_todos_empleados_con_paginacion'() {
        def usuarios = (1..20).collect { i ->
            new Usuario(
                id: "EMP${String.format('%03d', i)}",
                username: "usuario.${i}",
                nombreCompleto: "Empleado ${i}",
                email: "empleado${i}@empresa.com",
                rol: "Desarrollador",
                departamentoId: "IT",
                edad: 25 + i,
                activo: true,
                fechaCreacion: new Date(),
                fechaActualizacion: new Date()
            )
        }

        given(usuarioRepository.findAll()).willReturn(usuarios)

        ResponseEntity<List<Empleado>> response = empleadoController.listarTodos("1", "5")

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK)
        then(response.getBody().size()).isEqualTo(5)
    }
    
    @Test
    void 'listar_empleados_por_departamento_exitoso'() {
        def usuariosIT = [
            new Usuario(
                id: "EMP001",
                username: "juan.perez",
                nombreCompleto: "Juan Pérez",
                email: "juan.perez@empresa.com",
                rol: "Desarrollador",
                departamentoId: "IT",
                edad: 28,
                activo: true,
                fechaCreacion: new Date(),
                fechaActualizacion: new Date()
            ),
            new Usuario(
                id: "EMP002",
                username: "maria.gomez",
                nombreCompleto: "María Gómez",
                email: "maria.gomez@empresa.com",
                rol: "Analista",
                departamentoId: "IT",
                edad: 32,
                activo: true,
                fechaCreacion: new Date(),
                fechaActualizacion: new Date()
            )
        ]
        
        def usuariosHR = [
            new Usuario(
                id: "EMP003",
                username: "ana.martinez",
                nombreCompleto: "Ana Martínez",
                email: "ana.martinez@empresa.com",
                rol: "Diseñadora",
                departamentoId: "HR",
                edad: 26,
                activo: true,
                fechaCreacion: new Date(),
                fechaActualizacion: new Date()
            )
        ]
        
        def todosUsuarios = usuariosIT + usuariosHR
        given(usuarioRepository.findAll()).willReturn(todosUsuarios)
        given(departamentoValidationService.validarDepartamento("IT")).willReturn(null)

        ResponseEntity<List<Empleado>> response = empleadoController.listarPorDepartamento("IT", "0", "10")

        then(response.getStatusCode()).isEqualTo(HttpStatus.OK)
        then(response.getBody().size()).isEqualTo(2)
        then(response.getBody()[0].departamentoId).isEqualTo("IT")
        then(response.getBody()[1].departamentoId).isEqualTo("IT")
    }
    
    @Test
    void 'listar_empleados_por_departamento_falla_cuando_departamento_no_existe'() {
        doThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departamento no encontrado"))
            .when(departamentoValidationService).validarDepartamento("DEPT_INEXISTENTE")

        assertThatThrownBy({ -> empleadoController.listarPorDepartamento("DEPT_INEXISTENTE", "0", "10") })
            .isInstanceOf(ResponseStatusException.class)
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST)
    }
}
