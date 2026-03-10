package com.microservicios.departamentos_service

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class DepartamentoControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc

    @Test
    void'testCrearDepartamentoRetorna201CuandoDatosValidos'() throws Exception {
        // Usamos un ID único para evitar conflictos con otros tests en MongoDB
        String uniqueId = "TEST_IT_${System.currentTimeMillis()}"
        String departamentoJson = """
            {
                "id": "${uniqueId}",
                "nombre": "Tecnología",
                "descripcion": "Departamento de Tecnología e Innovación"
            }
        """

        mockMvc.perform(post("/departamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(departamentoJson))
            .andExpect(status().isCreated())
            .andExpect(jsonPath('$.id').value(uniqueId))
            .andExpect(jsonPath('$.nombre').value("Tecnología"))
    }

    @Test
    void'testObtenerDepartamentoRetorna200CuandoExiste'() throws Exception {
        // Primero creamos un departamento con ID único
        String uniqueId = "HR_TEST_${System.currentTimeMillis()}"
        String departamentoJson = """
            {
                "id": "${uniqueId}",
                "nombre": "Recursos Humanos",
                "descripcion": "Gestión de talento, nómina y cultura organizacional"
            }
        """

        mockMvc.perform(post("/departamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(departamentoJson))

        // Luego lo consultamos
        mockMvc.perform(get("/departamentos/${uniqueId}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath('$.id').value(uniqueId))
            .andExpect(jsonPath('$.nombre').value("Recursos Humanos"))
            .andExpect(jsonPath('$.descripcion').value("Gestión de talento, nómina y cultura organizacional"))
    }

    @Test
    void 'testObtenerDepartamentoRetorna404CuandoNoExiste'() throws Exception {
        mockMvc.perform(get("/departamentos/DEPT_INEXISTENTE"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath('$.error').value("NOT_FOUND"))
    }

    @Test
    void 'testListarDepartamentosRetorna200YListaVaciaInicialmente'() throws Exception {
        mockMvc.perform(get("/departamentos"))
            .andExpect(status().isOk())
            .andExpect(jsonPath('$').isArray())
    }

    @Test
    void'testActualizarDepartamentoRetorna200CuandoExiste'() throws Exception {
        // Primero creamos un departamento con ID único
        String uniqueId = "FIN_TEST_${System.currentTimeMillis()}"
        String departamentoJson = """
            {
                "id": "${uniqueId}",
                "nombre": "Finanzas",
                "descripcion": "Administración financiera"
            }
        """

        mockMvc.perform(post("/departamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(departamentoJson))

        // Actualizamos el departamento
        String departamentoActualizadoJson = """
            {
                "id": "${uniqueId}",
                "nombre": "Finanzas Actualizado",
                "descripcion": "Nueva descripción"
            }
        """

        mockMvc.perform(put("/departamentos/${uniqueId}")
                .contentType(MediaType.APPLICATION_JSON)
                .content(departamentoActualizadoJson))
            .andExpect(status().isOk())
            .andExpect(jsonPath('$.nombre').value("Finanzas Actualizado"))
    }

    @Test
    void 'testEliminarDepartamentoRetorna204CuandoExiste'() throws Exception {
        // Primero creamos un departamento con ID único
        String uniqueId = "MKT_TEST_${System.currentTimeMillis()}"
        String departamentoJson = """
            {
                "id": "${uniqueId}",
                "nombre": "Marketing",
                "descripcion": "Publicidad y mercadeo"
            }
        """

        mockMvc.perform(post("/departamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(departamentoJson))

        // Eliminamos el departamento
        mockMvc.perform(delete("/departamentos/${uniqueId}"))
            .andExpect(status().isNoContent())
    }

    @Test
    void 'testEliminarDepartamentoRetorna404CuandoNoExiste'() throws Exception {
        mockMvc.perform(delete("/departamentos/DEPT_INEXISTENTE"))
            .andExpect(status().isNotFound())
    }

    @Test
    void 'testCrearDepartamentoRepetidoRetorna409'() throws Exception {
        String uniqueId = "OPS_TEST_${System.currentTimeMillis()}"
        String departamentoJson = """
            {
                "id": "${uniqueId}",
                "nombre": "Operaciones",
                "descripcion": "Operaciones del negocio"
            }
        """

        // Creamos el departamento por primera vez
        mockMvc.perform(post("/departamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(departamentoJson))
            .andExpect(status().isCreated())

        // Intentamos crearlo de nuevo
        mockMvc.perform(post("/departamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(departamentoJson))
            .andExpect(status().isConflict())
            .andExpect(jsonPath('$.error').value('CONFLICT'))
    }

    @Test
    void 'testCrearDepartamentoConDatosInvalidosRetorna400'() throws Exception {
        String departamentoInvalidoJson = '''
            {
                "id": "",
                "nombre": "",
                "descripcion": ""
            }
        '''

        mockMvc.perform(post("/departamentos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(departamentoInvalidoJson))
            .andExpect(status().isBadRequest())
    }

    @Test
    void 'testSwaggerUIEstaDisponible'() throws Exception {
        // Swagger UI hace redirect de /swagger-ui.html a /swagger-ui/index.html (302)
        // Verificamos que redirija correctamente
        mockMvc.perform(get("/swagger-ui.html"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/swagger-ui/index.html"))
    }

    @Test
    void'testApiDocsEstaDisponible'() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isOk())
    }
}
