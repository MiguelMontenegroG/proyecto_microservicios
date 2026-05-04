# Auth Service - Servicio de Autenticación y Autorización

## Descripción

Servicio centralizado de autenticación y autorización con JWT para el sistema de microservicios.

## Características Principales

- **Autenticación con JWT**: Generación y validación de tokens JSON Web Token
- **Autorización basada en roles (RBAC)**: Roles ADMIN y USER
- **Recuperación de contraseña**: Flujo seguro con tokens temporales
- **Integración con eventos**: Escucha eventos de empleados para crear usuarios automáticamente

## Endpoints

### Autenticación
- `POST /auth/login` - Iniciar sesión y obtener token JWT
- `GET /auth/validate?token=<token>` - Validar token JWT

### Recuperación de Contraseña
- `POST /auth/forgot-password` - Solicitar token de recuperación
- `POST /auth/reset-password` - Restablecer contraseña con token

## Estructura del JWT

```json
{
  "sub": "usuario",
  "role": "ADMIN | USER",
  "iat": timestamp,
  "exp": timestamp
}
```

## Integración con RabbitMQ

El servicio consume eventos:
- `empleado.creado` - Crea usuario automáticamente cuando se crea un empleado
- `empleado.eliminado` - Inhabilita usuario cuando se elimina un empleado

Publica eventos:
- `usuario.creado` - Notifica creación de usuario
- `usuario.recuperacion` - Notifica solicitud de recuperación de contraseña

## Configuración

### Variables de Entorno

```bash
# Puerto del servicio
AUTH_SERVICE_PORT=8085

# MongoDB
MONGODB_URI=mongodb://database-auth:27017/
MONGODB_DATABASE_AUTH=authdb

# JWT
JWT_SECRET=tu_clave_secreta_muy_larga_de_mas_de_32_caracteres
JWT_EXPIRATION_MS=3600000

# RabbitMQ
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USER=admin
RABBITMQ_PASS=admin
```

## Flujo de Autenticación

1. Cliente envía credenciales a `/auth/login`
2. Servicio valida credenciales contra MongoDB
3. Si son válidas, genera JWT token
4. Cliente usa el token en header `Authorization: Bearer <token>`
5. Los demás servicios validan el token con cada request

## Seguridad

- Contraseñas encriptadas con BCrypt
- Tokens JWT firmados con HS512
- Tokens de recuperación expiran en 1 hora
- No se revelan detalles de existencia de usuarios por seguridad

## Swagger/OpenAPI

Documentación disponible en: `http://localhost:8085/swagger-ui.html`

Para usar endpoints protegidos:
1. Obtener token desde `/auth/login`
2. Click en botón "Authorize" en Swagger
3. Pegar token con formato: `Bearer <token>`
