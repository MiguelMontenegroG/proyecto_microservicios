# Notificaciones Service

Servicio de gestión de notificaciones para empleados, parte de la arquitectura de microservicios con comunicación basada en eventos.

## Descripción

Este servicio es responsable de:
- Consumir eventos desde RabbitMQ (cola: `cola.notificaciones`)
- Simular el envío de notificaciones (emails) cuando se crean o eliminan empleados
- Persistir el historial de notificaciones enviadas
- No tiene endpoints de escritura, solo lectura

## Endpoints REST

### GET /notificaciones
Lista todas las notificaciones registradas.

### GET /notificaciones/{empleadoId}
Consulta las notificaciones asociadas a un empleado específico.

## Eventos que consume

### empleado.creado
Cuando recibe este evento, simula el envío de un email de bienvenida y lo persiste.

**Payload esperado:**
```json
{
  "id": "E001",
  "nombre": "Juan Perez",
  "email": "juan@empresa.com",
  "departamentoId": "IT",
  "fechaIngreso": "2026-03-10"
}
```

**Log generado:**
```
[NOTIFICACION]
Tipo: BIENVENIDA
Para: juan@empresa.com
Mensaje: Bienvenido Juan Perez
```

### empleado.eliminado
Cuando recibe este evento, simula una notificación de desvinculación y la persiste.

**Payload esperado:**
```json
{
  "id": "E001",
  "nombre": "Juan Perez",
  "email": "juan@empresa.com"
}
```

**Log generado:**
```
[NOTIFICACION]
Tipo: DESVINCULACION
Para: juan@empresa.com
Mensaje: Notificación de desvinculación: Juan Perez
```

## Modelo de Datos

El modelo `Notificacion` contiene:
- `id`: Identificador único de la notificación
- `tipo`: Tipo de notificación (BIENVENIDA | DESVINCULACION)
- `destinatario`: Email del destinatario
- `mensaje`: Mensaje de la notificación
- `fechaEnvio`: Fecha de envío
- `empleadoId`: ID del empleado asociado

## Configuración

### Puertos
- Servicio: 8084
- MongoDB: 27020
- RabbitMQ: 5672

### Variables de entorno
Ver archivo `.env` en la raíz del proyecto.

## Swagger UI

Accede a la documentación interactiva en:
http://localhost:8084/swagger-ui.html

## Flujo de trabajo

1. El servicio `empleados-service` publica eventos (`empleado.creado` o `empleado.eliminado`)
2. RabbitMQ distribuye los eventos a la cola `cola.notificaciones`
3. Este servicio consume los eventos y genera notificaciones
4. Las notificaciones se persisten en MongoDB para consulta futura
5. Los usuarios pueden consultar el historial mediante los endpoints REST

## Base de datos

MongoDB: `db-notificaciones`

## Características especiales

- **Solo lectura**: No expone endpoints POST/PUT/DELETE
- **Persistencia**: Guarda historial completo de notificaciones
- **Simulación**: Loguea las notificaciones como si fueran emails enviados
- **Eventos múltiples**: Procesa ambos tipos de eventos (creado/eliminado)
