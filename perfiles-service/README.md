# Perfiles Service

Servicio de gestión de perfiles para empleados, parte de la arquitectura de microservicios con comunicación basada en eventos.

## Descripción

Este servicio es responsable de:
- Crear automáticamente perfiles cuando se crea un nuevo empleado
- Permitir la consulta y actualización de perfiles
- Consumir eventos desde RabbitMQ (cola: `cola.perfiles`)

## Endpoints REST

### GET /perfiles
Lista todos los perfiles registrados.

### GET /perfiles/{empleadoId}
Consulta el perfil asociado a un empleado específico.

### PUT /perfiles/{empleadoId}
Actualiza la información del perfil de un empleado.

## Eventos que consume

### empleado.creado
Cuando recibe este evento, crea automáticamente un perfil para el nuevo empleado.

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

## Modelo de Datos

El modelo `Perfil` contiene:
- `id`: Identificador único del perfil
- `empleadoId`: ID del empleado asociado
- `nombre`: Nombre completo
- `email`: Correo electrónico
- `telefono`: Teléfono (opcional)
- `direccion`: Dirección (opcional)
- `ciudad`: Ciudad (opcional)
- `biografia`: Biografía (opcional)
- `fechaCreacion`: Fecha de creación del perfil

## Configuración

### Puertos
- Servicio: 8083
- MongoDB: 27019
- RabbitMQ: 5672

### Variables de entorno
Ver archivo `.env` en la raíz del proyecto.

## Swagger UI

Accede a la documentación interactiva en:
http://localhost:8083/swagger-ui.html

## Flujo de trabajo

1. El servicio `empleados-service` publica un evento `empleado.creado`
2. RabbitMQ distribuye el evento a la cola `cola.perfiles`
3. Este servicio consume el evento y crea un perfil automáticamente
4. El usuario puede consultar y actualizar el perfil mediante los endpoints REST

## Base de datos

MongoDB: `db-perfiles`
