package com.microservicios.empleados_service.controller

import com.microservicios.empleados_service.model.Empleado
import com.microservicios.empleados_service.model.Usuario
import com.microservicios.empleados_service.repository.UsuarioRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.http.HttpStatus
import com.microservicios.empleados_service.service.DepartamentoValidationService
import org.springframework.web.server.ResponseStatusException
import org.springframework.context.annotation.Bean
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import java.util.List
@RestController
@RequestMapping("/empleados")
@Tag(name = "Empleados", description = "API para gestión de empleados")
class EmpleadoController {

    @Autowired
    UsuarioRepository usuarioRepository
    
    @Autowired
    DepartamentoValidationService departamentoValidationService
    
    private void validarDepartamento(String departamentoId) {
        departamentoValidationService.validarDepartamento(departamentoId)
    }

    @PostMapping
    @Operation(
        summary = "Registrar nuevo empleado",
        description = "Crea un nuevo empleado asociado a un departamento existente"
    )
    @ApiResponse(responseCode = "201", description = "Empleado creado exitosamente",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Empleado)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos o departamento no encontrado")
    @ApiResponse(responseCode = "409", description = "Empleado con ese ID ya existe")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    ResponseEntity<Empleado> registrar(@Valid @RequestBody Empleado empleado) {
        try {
            if (empleado == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON incompleto o mal formado. Se requiere un objeto empleado válido")
            }
            
            // Validar que no exista un empleado con el mismo ID (repetido)
            if (usuarioRepository.existsById(empleado.id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Empleado repetido: ya existe un empleado con ID '${empleado.id}'")
            }
            
            // Validar departamento
            validarDepartamento(empleado.departamentoId)
            
            // Guardar en MongoDB
            Usuario usuario = new Usuario(
                id: empleado.id,
                nombreCompleto: empleado.nombre,
                email: empleado.email,
                departamentoId: empleado.departamentoId,
                fechaCreacion: empleado.fechaCreacion
            )
            
            usuarioRepository.save(usuario)
            return ResponseEntity.status(HttpStatus.CREATED).body(empleado)
        } catch (ResponseStatusException e) {
            throw e
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al procesar la solicitud: ${e.getMessage()}")
        }
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener empleado por ID",
        description = "Recupera la información de un empleado específico por su identificador"
    )
    @Parameter(name = "id", description = "Identificador único del empleado", required = true)
    @ApiResponse(responseCode = "200", description = "Empleado encontrado",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Empleado)))
    @ApiResponse(responseCode = "400", description = "ID no proporcionado")
    @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    ResponseEntity<?> obtener(@PathVariable("id") String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta el ID del empleado en la URL")
            }
            
            Usuario usuario = usuarioRepository.findById(id).orElse(null)
            if (usuario == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado: no existe empleado con ID '${id}'")
            }
            
            // Convertir Usuario a Empleado para mantener compatibilidad
            Empleado empleado = new Empleado(
                id: usuario.id ?: "",
                nombre: usuario.nombreCompleto ?: "Sin nombre",
                email: usuario.email ?: "sin@email.com",
                departamentoId: usuario.departamentoId ?: "",
                fechaIngreso: usuario.fechaCreacion ?: new Date(),
                fechaCreacion: usuario.fechaCreacion ?: new Date()
            )
            
            return ResponseEntity.ok(empleado)
        } catch (ResponseStatusException e) {
            throw e
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al obtener empleado: ${e.getMessage()}")
        }
    }
    
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar empleado",
        description = "Actualiza la información de un empleado existente"
    )
    @Parameter(name = "id", description = "Identificador único del empleado a actualizar", required = true)
    @ApiResponse(responseCode = "200", description = "Empleado actualizado exitosamente",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Empleado)))
    @ApiResponse(responseCode = "400", description = "Datos inválidos o ID no proporcionado")
    @ApiResponse(responseCode = "404", description = "Empleado no encontrado")
    @ApiResponse(responseCode = "409", description = "Conflicto en actualización")
    ResponseEntity<Empleado> actualizar(@PathVariable("id") String id, @Valid @RequestBody Empleado empleado) {
        try {
            if (id == null || id.trim().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de empleado no proporcionado")
            }
            
            if (empleado == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "JSON incompleto o mal formado. Se requiere un objeto empleado válido")
            }
            
            // Verificar que el empleado exista
            Usuario usuarioExistente = usuarioRepository.findById(id).orElse(null)
            if (usuarioExistente == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Empleado no encontrado: no existe empleado con ID '${id}'")
            }
            
            // Validar departamento si se está cambiando
            if (empleado.departamentoId != null && !empleado.departamentoId.equals(usuarioExistente.departamentoId)) {
                validarDepartamento(empleado.departamentoId)
            }
            
            // Actualizar datos - verificar existencia explícita de cada campo
            if (empleado.nombre != null) {
                usuarioExistente.nombreCompleto = empleado.nombre
            }
            if (empleado.email != null) {
                usuarioExistente.email = empleado.email
            }
            if (empleado.departamentoId != null) {
                usuarioExistente.departamentoId = empleado.departamentoId
            }
            usuarioExistente.fechaActualizacion = new Date()
            
            usuarioRepository.save(usuarioExistente)
            
            // Convertir de vuelta a Empleado para respuesta
            Empleado empleadoActualizado = new Empleado(
                id: usuarioExistente.id,
                nombre: usuarioExistente.nombreCompleto,
                email: usuarioExistente.email,
                departamentoId: usuarioExistente.departamentoId,
                fechaIngreso: usuarioExistente.fechaCreacion,
                fechaCreacion: usuarioExistente.fechaCreacion
            )
            
            return ResponseEntity.ok(empleadoActualizado)
            
        } catch (ResponseStatusException e) {
            throw e
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al actualizar empleado: ${e.getMessage()}")
        }
    }
    
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar empleado",
        description = "Elimina un empleado del sistema (operación no permitida)"
    )
    @Parameter(name = "id", description = "Identificador único del empleado", required = true)
    @ApiResponse(responseCode = "405", description = "Método no permitido - Operación DELETE no soportada")
    ResponseEntity<?> eliminar(@PathVariable("id") String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ID de empleado no válido")
        }

        // Siempre retornar 405 - Método no permitido, independientemente de si existe o no
        throw new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED, "La operación DELETE no está soportada para empleados")
    }
    
    @GetMapping
    @Operation(
        summary = "Listar todos los empleados",
        description = "Recupera una lista paginada de todos los empleados registrados"
    )
    @Parameter(name = "pagina", description = "Número de página (comienza en 0)", example = "0")
    @Parameter(name = "tamano", description = "Cantidad de registros por página", example = "10")
    @ApiResponse(responseCode = "200", description = "Lista de empleados recuperada exitosamente",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Empleado, type = "array")))
    @ApiResponse(responseCode = "400", description = "Parámetros de paginación inválidos")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    ResponseEntity<List<Empleado>> listarTodos(
        @RequestParam(value = "pagina", defaultValue = "0") String paginaStr,
        @RequestParam(value = "tamano", defaultValue = "10") String tamanoStr
    ) {
        try {
            println "=== LISTANDO EMPLEADOS CON PAGINACIÓN ==="
            println "Parámetros recibidos: pagina='$paginaStr', tamano='$tamanoStr'"
            
            // Validar y convertir parámetros
            int pagina = 0
            int tamano = 10
            
            try {
                pagina = Integer.parseInt(paginaStr)
                tamano = Integer.parseInt(tamanoStr)
                
                // Validar rangos
                if (pagina < 0) {
                    pagina = 0
                }
                if (tamano <= 0 || tamano > 100) {
                    tamano = 10
                }
            } catch (NumberFormatException e) {
                println "Parámetros inválidos, usando valores por defecto"
                pagina = 0
                tamano = 10
            }
            
            println "Parámetros procesados: pagina=$pagina, tamano=$tamano"
            
            // Obtener todos los usuarios
            def todosUsuarios = usuarioRepository.findAll()
            println "Total usuarios en DB: ${todosUsuarios.size()}"
            
            // Aplicar paginación manualmente
            int totalRegistros = todosUsuarios.size()
            int inicio = pagina * tamano
            int fin = Math.min(inicio + tamano, totalRegistros)
            
            // Asegurar que los índices sean válidos
            if (inicio >= totalRegistros) {
                inicio = Math.max(0, totalRegistros - tamano)
                fin = totalRegistros
            }
            
            if (inicio < 0) inicio = 0
            if (fin > totalRegistros) fin = totalRegistros
            
            println "Rango de paginación: inicio=$inicio, fin=$fin"
            
            // Obtener usuarios paginados
            def usuariosPaginados = todosUsuarios[inicio..<fin]
            println "Usuarios en página actual: ${usuariosPaginados.size()}"
            
            // Convertir a empleados
            List<Empleado> empleados = usuariosPaginados.collect { usuario ->
                new Empleado(
                    id: usuario.id ?: "",
                    nombre: usuario.nombreCompleto ?: "Sin nombre",
                    email: usuario.email ?: "sin@email.com",
                    departamentoId: usuario.departamentoId ?: "",
                    fechaIngreso: usuario.fechaCreacion ?: new Date(),
                    fechaCreacion: usuario.fechaCreacion ?: new Date()
                )
            }
            
            println "Empleados convertidos: ${empleados.size()}"
            return ResponseEntity.ok(empleados)
            
        } catch (Exception e) {
            println "ERROR AL LISTAR EMPLEADOS: ${e.getClass().name} - ${e.getMessage()}"
            e.printStackTrace()
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al listar empleados: ${e.getMessage()}")
        }
    }
    
    @GetMapping("/departamento/{departamentoId}")
    @Operation(
        summary = "Listar empleados por departamento",
        description = "Recupera una lista de todos los empleados que pertenecen a un departamento específico"
    )
    @Parameter(name = "departamentoId", description = "Identificador del departamento para filtrar empleados", required = true, example = "IT")
    @Parameter(name = "pagina", description = "Número de página (comienza en 0)", example = "0")
    @Parameter(name = "tamano", description = "Cantidad de registros por página", example = "10")
    @ApiResponse(responseCode = "200", description = "Lista de empleados del departamento recuperada exitosamente",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Empleado, type = "array")))
    @ApiResponse(responseCode = "400", description = "Parámetros inválidos o departamento no proporcionado")
    @ApiResponse(responseCode = "404", description = "Departamento no encontrado")
    @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    ResponseEntity<List<Empleado>> listarPorDepartamento(
        @PathVariable("departamentoId") String departamentoId,
        @RequestParam(value = "pagina", defaultValue = "0") String paginaStr,
        @RequestParam(value = "tamano", defaultValue = "10") String tamanoStr
    ) {
        try {
            println "=== LISTANDO EMPLEADOS POR DEPARTAMENTO ==="
            println "Departamento: $departamentoId"
            
            // Validar que el departamento exista
            validarDepartamento(departamentoId)
            
            // Validar y convertir parámetros de paginación
            int pagina = 0
            int tamano = 10
            
            try {
                pagina = Integer.parseInt(paginaStr)
                tamano = Integer.parseInt(tamanoStr)
                
                // Validar rangos
                if (pagina < 0) {
                    pagina = 0
                }
                if (tamano <= 0 || tamano > 100) {
                    tamano = 10
                }
            } catch (NumberFormatException e) {
                println "Parámetros inválidos, usando valores por defecto"
                pagina = 0
                tamano = 10
            }
            
            println "Parámetros procesados: pagina=$pagina, tamano=$tamano"
            
            // Obtener todos los usuarios
            def todosUsuarios = usuarioRepository.findAll()
            println "Total usuarios en DB: ${todosUsuarios.size()}"
            
            // Filtrar usuarios por departamento
            def usuariosFiltrados = todosUsuarios.findAll { usuario ->
                usuario.departamentoId == departamentoId
            }
            println "Empleados en departamento $departamentoId: ${usuariosFiltrados.size()}"
            
            // Aplicar paginación manualmente
            int totalRegistros = usuariosFiltrados.size()
            int inicio = pagina * tamano
            int fin = Math.min(inicio + tamano, totalRegistros)
            
            // Asegurar que los índices sean válidos
            if (inicio >= totalRegistros) {
                inicio = Math.max(0, totalRegistros - tamano)
                fin = totalRegistros
            }
            
            if (inicio < 0) inicio = 0
            if (fin > totalRegistros) fin = totalRegistros
            
            println "Rango de paginación: inicio=$inicio, fin=$fin"
            
            // Obtener usuarios paginados
            def usuariosPaginados = usuariosFiltrados[inicio..<fin]
            println "Empleados en página actual: ${usuariosPaginados.size()}"
            
            // Convertir a empleados
            List<Empleado> empleados = usuariosPaginados.collect { usuario ->
                new Empleado(
                    id: usuario.id ?: "",
                    nombre: usuario.nombreCompleto ?: "Sin nombre",
                    email: usuario.email ?: "sin@email.com",
                    departamentoId: usuario.departamentoId ?: "",
                    fechaIngreso: usuario.fechaCreacion ?: new Date(),
                    fechaCreacion: usuario.fechaCreacion ?: new Date()
                )
            }
            
            println "Empleados convertidos: ${empleados.size()}"
            return ResponseEntity.ok(empleados)
            
        } catch (ResponseStatusException e) {
            throw e
        } catch (Exception e) {
            println "ERROR AL LISTAR EMPLEADOS POR DEPARTAMENTO: ${e.getClass().name} - ${e.getMessage()}"
            e.printStackTrace()
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al listar empleados por departamento: ${e.getMessage()}")
        }
    }
}

// prueba git
