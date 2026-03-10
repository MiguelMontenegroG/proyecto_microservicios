package com.microservicios.empleados_service.controller

import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestController
import org.springframework.http.ResponseEntity
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.bind.MethodArgumentNotValidException
import jakarta.validation.ConstraintViolationException

@ControllerAdvice
@RestController
class GlobalErrorHandler {

    @ExceptionHandler(ResponseStatusException)
    ResponseEntity<Map<String, Object>> handleResponseStatusException(ResponseStatusException ex) {

        Map<String, Object> errorResponse = [
                timestamp: new Date(),
                status   : ex.statusCode.value(),
                error    : ex.statusCode.reasonPhrase,
                message  : ex.reason ?: "Error en la solicitud",
                tipo     : clasificarTipoError(ex.reason)
        ]

        return ResponseEntity.status(ex.statusCode).body(errorResponse)
    }

    @ExceptionHandler(MethodArgumentNotValidException)
    ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex) {

        def firstError = ex.bindingResult.fieldErrors.first()

        String campo = firstError.field
        String mensajeCampo = firstError.defaultMessage

        Map<String, Object> errorResponse = [
                timestamp: new Date(),
                status   : HttpStatus.BAD_REQUEST.value(),
                error    : "Bad Request",
                message  : mensajeCampo,   // Usa exactamente el mensaje del @NotBlank
                tipo     : "CAMPO_FALTANTE"
        ]

        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(ConstraintViolationException)
    ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex) {

        def firstViolation = ex.constraintViolations.first()

        Map<String, Object> errorResponse = [
                timestamp: new Date(),
                status   : HttpStatus.BAD_REQUEST.value(),
                error    : "Bad Request",
                message  : firstViolation.message,
                tipo     : "CAMPO_FALTANTE"
        ]

        return ResponseEntity.badRequest().body(errorResponse)
    }

    @ExceptionHandler(Exception)
    ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {

        Map<String, Object> errorResponse = [
                timestamp: new Date(),
                status   : HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error    : "Internal Server Error",
                message  : "Ocurrió un error inesperado",
                tipo     : "ERROR_SISTEMA"
        ]

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    private String clasificarTipoError(String mensaje) {
        if (mensaje?.contains("repetido") || mensaje?.contains("ya existe")) {
            return "EMPLEADO_REPETIDO"
        } else if (mensaje?.contains("requerido")) {
            return "CAMPO_FALTANTE"
        } else if (mensaje?.contains("no existe") || mensaje?.contains("no encontrado")) {
            return "EMPLEADO_NO_ENCONTRADO"
        } else if (mensaje?.contains("no está soportada") || mensaje?.contains("no permitida")) {
            return "OPERACION_NO_SOPORTADA"
        } else {
            return "ERROR_GENERICO"
        }
    }
}
