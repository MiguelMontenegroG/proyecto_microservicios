package com.microservicios.empleados_service

import com.microservicios.empleados_service.model.Empleado
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = [
   "departamento.service.url=http://localhost:9999",  // Puerto que no existe para forzar error controlado
   "spring.cloud.compatibility-verifier.enabled=false"
])
class EmpleadoControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc

    @Test
    void 'testCrearEmpleadoRetorna201CuandoDatosValidos'() throws Exception {
        String empleadoJson = '''
            {
                "id": "EMP001",
                "nombre": "Juan Pérez",
                "email": "juan.perez@empresa.com",
                "departamentoId": "IT"
            }
        '''

        mockMvc.perform(post("/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empleadoJson))
            .andExpect(status().isCreated())
    }

    @Test
    void 'testObtenerEmpleadoRetorna200CuandoExiste'() throws Exception {
        // Primero creamos un empleado
        String empleadoJson = '''
            {
                "id": "EMP002",
                "nombre": "María García",
                "email": "maria.garcia@empresa.com",
                "departamentoId": "IT"
            }
        '''

        mockMvc.perform(post("/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empleadoJson))

        // Luego lo consultamos
        mockMvc.perform(get("/empleados/EMP002"))
            .andExpect(status().isOk())
            .andExpect(jsonPath('$.id').value('EMP002'))
            .andExpect(jsonPath('$.nombre').value('María García'))
    }

    @Test
    void 'testObtenerEmpleadoRetorna404CuandoNoExiste'() throws Exception {
        mockMvc.perform(get("/empleados/EMP_INEXISTENTE"))
            .andExpect(status().isNotFound())
    }

    @Test
    void 'testListarEmpleadosRetorna200YListaVaciaInicialmente'() throws Exception {
        mockMvc.perform(get("/empleados")
                .param("pagina", "0")
                .param("tamano", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath('$').isArray())
    }
    
    @Test
    void 'testListarEmpleadosPorDepartamentoRetorna200YLista'() throws Exception {
        // Primero creamos un empleado en IT
        String empleadoJson = '''
            {
                "id": "EMP005",
                "nombre": "Pedro García",
                "email": "pedro.garcia@empresa.com",
                "departamentoId": "IT"
            }
        '''

        mockMvc.perform(post("/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empleadoJson))

        // Listamos empleados del departamento IT
        mockMvc.perform(get("/empleados/departamento/IT")
                .param("pagina", "0")
                .param("tamano", "10"))
            .andExpect(status().isOk())
            .andExpect(jsonPath('$').isArray())
    }
    
    @Test
    void 'testListarEmpleadosPorDepartamentoRetorna400CuandoDepartamentoNoExiste'() throws Exception {
        mockMvc.perform(get("/empleados/departamento/DEPT_INEXISTENTE")
                .param("pagina", "0")
                .param("tamano", "10"))
            .andExpect(status().isBadRequest())
    }

    @Test
    void 'testActualizarEmpleadoRetorna200CuandoExiste'() throws Exception {
        // Primero creamos un empleado
        String empleadoJson = '''
            {
                "id": "EMP003",
                "nombre": "Carlos López",
                "email": "carlos.lopez@empresa.com",
                "departamentoId": "IT"
            }
        '''

        mockMvc.perform(post("/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empleadoJson))

        // Actualizamos el empleado
        String empleadoActualizadoJson = '''
            {
                "id": "EMP003",
                "nombre": "Carlos López Actualizado",
                "email": "carlos.actualizado@empresa.com",
                "departamentoId": "IT"
            }
        '''

        mockMvc.perform(put("/empleados/EMP003")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empleadoActualizadoJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath('$.nombre').value('Carlos López Actualizado'))
    }

    @Test
    void 'testEliminarEmpleadoRetorna405MetodoNoPermitido'() throws Exception {
        mockMvc.perform(delete("/empleados/EMP001"))
            .andExpect(status().isMethodNotAllowed())
    }

    @Test
    void 'testCrearEmpleadoConDatosInvalidosRetorna400'() throws Exception {
        String empleadoInvalidoJson = '''
            {
                "id": "",
                "nombre": "",
                "email": "email-invalido",
                "departamentoId": ""
            }
        '''

        mockMvc.perform(post("/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empleadoInvalidoJson))
            .andExpect(status().isBadRequest())
    }

    @Test
    void 'testCrearEmpleadoRepetidoRetorna409'() throws Exception {
        String empleadoJson = '''
            {
                "id": "EMP004",
                "nombre": "Empleado Repetido",
                "email": "repetido@empresa.com",
                "departamentoId": "IT"
            }
        '''

        // Creamos el empleado por primera vez
        mockMvc.perform(post("/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empleadoJson))
            .andExpect(status().isCreated())

        // Intentamos crearlo de nuevo
        mockMvc.perform(post("/empleados")
                .contentType(MediaType.APPLICATION_JSON)
                .content(empleadoJson))
            .andExpect(status().isConflict())
    }
}
