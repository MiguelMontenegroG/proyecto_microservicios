# Sistema de Microservicios

Sistema de microservicios para la gestión de empleados y departamentos con comunicación asíncrona basada en eventos usando RabbitMQ.

## Descripción del Sistema

Este sistema implementa una arquitectura de microservicios que combina:
- **Comunicación síncrona (REST)**: Para operaciones directas entre servicios
- **Comunicación asíncrona (eventos)**: Para propagar cambios automáticamente mediante RabbitMQ (patrón fan-out)

### Características principales

* **5 microservicios independientes** (empleados, departamentos, perfiles, notificaciones, autenticación)  
* **4 bases de datos MongoDB** (una por servicio, sin compartir datos)  
* **RabbitMQ** como message broker para eventos  
* **Patrón Fan-out**: Un evento → Múltiples consumidores  
* **OpenAPI/Swagger** en todos los servicios  
* **Dockerizado** y listo para desplegar con docker-compose  
* **Resiliente** con Resilience4j (circuit breaker)  
* **Automatización**: Creación de perfil y notificación al crear empleado
* **Seguridad JWT**: Autenticación y autorización con tokens JSON Web Token

## Arquitectura

```
┌─────────────────────────┐
│      Cliente HTTP       │
│   (Postman, curl)       │
└───────────┬─────────────┘
            │
            ▼
┌─────────────────────────┐         ┌──────────────────────┐
│   Empleados             │  HTTP   │  Departamentos       │
│   Service :8080         │ ──────> │  Service :8081       │
│                         │         │                      │
│  - CRUD Empleados       │         │  - CRUD Departamentos│
│  - Publica Eventos      │         │                      │
└───────────┬─────────────┘         └──────────┬───────────┘
            │                                  │
            │ publica eventos                  │
            ▼                                  │
┌─────────────────────────┐                    │
│     RabbitMQ            │                    │
│  empleados.events       │                    │
│    (fanout exchange)    │                    │
└───────────┬─────────────┘                    │
            │                                  │
     ┌──────┴──────┐                           │
     │             │                           │
     ▼             ▼                           │
┌─────────┐  ┌────────────┐                    │
│ Perfiles│  │Notificaciones                   │
│Service  │  │Service     │                    │
│:8083    │  │:8084       │                    │
└────┬────┘  └─────┬──────┘                    │
     │             │                           │
     ▼             ▼                           │
┌─────────┐  ┌────────────┐                    │
│ MongoDB │  │  MongoDB   │                    │
│Perfiles │  │Notificaciones                   │
└─────────┘  └────────────┘                    │
                                               │
            ┌──────────────────────────────────┘
            │
            ▼
      ┌───────────┐
      │  MongoDB  │
      │Departamentos
      └───────────┘

┌─────────────────────────┐
│   Auth Service :8085    │
│                         │
│  - Login/Logout         │
│  - JWT Token Mgmt       │
│  - User Validation      │
│  - Password Recovery    │
└───────────┬─────────────┘
            │
            ▼
      ┌───────────┐
      │  MongoDB  │
      │   Auth    │
      └───────────┘
```

### Flujo de evento: Creación de empleado

```mermaid
sequenceDiagram
    participant C as Cliente
    participant E as Empleados Service (:8080)
    participant D as Departamentos Service (:8081)
    participant R as RabbitMQ
    participant P as Perfiles Service (:8083)
    participant N as Notificaciones Service (:8084)
    
    C->>E: POST /empleados
    E->>D: GET /departamentos/{id} (validar)
    D-->>E: Departamento válido
    E->>E: Guardar en MongoDB
    E->>R: Publicar evento empleado.creado
    R->>P: Enviar a cola.perfiles
    R->>N: Enviar a cola.notificaciones
    P->>P: Crear perfil automáticamente
    N->>N: Registrar notificación BIENVENIDA
    E-->>C: HTTP 201 Created
```

## Tecnologías

- **Backend**: Spring Boot 3.2.5 con Groovy
- **Bases de datos**: MongoDB 7.0 (una por servicio)
- **Message Broker**: RabbitMQ 3.x
- **Comunicación**: REST/HTTP + AMQP (RabbitMQ)
- **Resiliencia**: Resilience4j (Circuit Breaker, Time Limiter)
- **Documentación**: OpenAPI/Swagger
- **Containerización**: Docker y Docker Compose
- **Build Tool**: Gradle
- **Java**: 21

## Selección del Message Broker

### Comparativa de Brokers de Mensajería

Para la comunicación asíncrona entre microservicios, se evaluaron las siguientes opciones:

#### 1. RabbitMQ

**Ventajas:**
-  Protocolo AMQP estándar y maduro
-  Fácil configuración y uso
-  Soporta múltiples patrones: colas, publish/subscribe, routing, topics
-  Gestión mediante UI web incluida (RabbitMQ Management)
-  Persistencia de mensajes configurable
-  Acknowledgment de mensajes
-  Dead letter queues para manejo de errores
-  Excelente documentación y comunidad
-  Ligero y fácil de containerizar

**Desventajas:**
-  Menor throughput que Kafka en escenarios de muy alta carga
-  No es ideal para event sourcing a largo plazo

**Casos de uso ideales:**
- Comunicación entre microservicios
- Colas de tareas
- Notificaciones en tiempo real
- Sistemas con patrones complejos de enrutamiento

#### 2. Apache Kafka

**Ventajas:**
-  Alto throughput extremo (millones de mensajes/segundo)
-  Persistencia a largo plazo (logs inmutables)
-  Ideal para event sourcing y CQRS
-  Escalabilidad horizontal masiva
-  Múltiples consumidores independientes

**Desventajas:**
-  Mayor complejidad de configuración y operación
-  Requiere ZooKeeper (aunque está cambiando en versiones recientes)
-  Overkill para sistemas pequeños/medianos
-  Curva de aprendizaje más pronunciada
-  Más recursos necesarios (memoria, CPU)

**Casos de uso ideales:**
- Streaming de datos a gran escala
- Event sourcing
- Procesamiento de logs distribuido
- Sistemas que requieren retención prolongada de mensajes

#### 3. Redis Streams / NATS

**Redis Streams:**

**Ventajas:**
-  Muy baja latencia
-  Simple y ligero
-  Si ya usas Redis, es una característica adicional

**Desventajas:**
-  Funcionalidades limitadas comparado con RabbitMQ/Kafka
-  Persistencia limitada
-  Patrones de mensajería menos maduros
-  No es su propósito principal

**NATS:**

**Ventajas:**
-  Extremadamente rápido y ligero
-  Simple de operar
-  Bueno para IoT y mensajería en tiempo real

**Desventajas:**
-  Menor adopción en enterprise
-  Ecosistema más pequeño
-  Menor madurez en patrones avanzados

### Justificación de la Elección: RabbitMQ

Para este proyecto de microservicios de gestión empresarial, se eligió **RabbitMQ** por las siguientes razones:

1. **Complejidad apropiada**: El sistema no requiere el throughput extremo de Kafka ni la persistencia a largo plazo. RabbitMQ ofrece el balance perfecto entre funcionalidad y simplicidad.

2. **Patrones requeridos**: Necesitamos patrón fan-out (un evento → múltiples consumidores), que RabbitMQ implementa nativamente con exchanges tipo "fanout".

3. **Facilidad de implementación**: RabbitMQ es más fácil de configurar, monitorear y mantener en un entorno de desarrollo y producción de pequeña/mediana escala.

4. **UI de gestión incluida**: RabbitMQ Management proporciona una interfaz web completa para monitorear colas, exchanges y mensajes sin herramientas adicionales.

5. **Madurez y estabilidad**: AMQP es un protocolo estándar con años de desarrollo, ampliamente adoptado en la industria.

6. **Integración con Spring Boot**: Spring AMQP proporciona integración excelente y sencilla con Spring Boot, reduciendo código boilerplate.

7. **Recursos eficientes**: Consume menos recursos que Kafka, importante para despliegues en contenedores Docker.

8. **Aprendizaje**: Para fines educativos, RabbitMQ permite comprender conceptos de mensajería sin la complejidad adicional de Kafka.

**Conclusión**: RabbitMQ es la opción óptima para este sistema porque proporciona todas las funcionalidades necesarias (colas duraderas, publish/subscribe, acknowledgment, persistencia) con una complejidad operativa manejable y excelente soporte en el ecosistema Spring Boot.

## Requisitos previos

- Docker y Docker Compose instalados
- Java 21 (para desarrollo local)
- Gradle 7.x o superior (opcional, se incluye wrapper)
- PowerShell (en Windows)

## Estructura del proyecto

```
proyecto/
├── auth-service/
│   ├── src/
│   ├── build.gradle
│   ├── Dockerfile
│   └── README.md
├── departamentos-service/
│   ├── src/
│   ├── build.gradle
│   ├── Dockerfile
│   └── README.md
├── empleados-service/
│   ├── src/
│   ├── build.gradle
│   ├── Dockerfile
│   └── README.md
├── perfiles-service/
│   ├── src/
│   ├── build.gradle
│   ├── Dockerfile
│   └── README.md
├── notificaciones-service/
│   ├── src/
│   ├── build.gradle
│   ├── Dockerfile
│   └── README.md
├── docker-compose.yml
├── .env
├── .gitignore
└── README.md
```

## Configuración

### Variables de entorno

El sistema utiliza un archivo `.env` en la raíz del proyecto. Ejemplo:

```properties
# Versiones
MONGO_VERSION=7.0

# Puertos Servicios Existentes
EMPLEADOS_SERVICE_PORT=8080
DEPARTAMENTOS_SERVICE_PORT=8081
MONGODB_EMPL_PORT=27017
MONGODB_DEP_PORT=27018

# Puerto Auth Service (Reto 4)
AUTH_SERVICE_PORT=8085
MONGODB_AUTH_PORT=27021

# Puertos Nuevos Servicios Reto 3
PERFILES_SERVICE_PORT=8083
NOTIFICACIONES_SERVICE_PORT=8084
MONGODB_PERF_PORT=27019
MONGODB_NOTIF_PORT=27020

# RabbitMQ
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USER=admin
RABBITMQ_PASS=admin

# MongoDB URIs
MONGODB_URI=mongodb://database-auth:27017/
MONGODB_DATABASE_AUTH=authdb
MONGODB_URI_EMPLEADOS=mongodb://database-empleados:27017/
MONGODB_DATABASE_EMPLEADOS=empleadosdb
MONGODB_DATABASE_DEPARTAMENTOS=departamentosdb
MONGODB_DATABASE_PERFILES=perfilesdb
MONGODB_DATABASE_NOTIFICACIONES=notificacionesdb

# URLs entre servicios
DEPARTAMENTOS_SERVICE_URL=http://departamentos-service:8081
AUTH_SERVICE_URL=http://auth-service:8085

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_EMPLEADOS=INFO
LOGGING_LEVEL_DEPARTAMENTOS=INFO
LOGGING_LEVEL_MONGODB=INFO

# Resilience4j
CIRCUIT_BREAKER_FAILURE_RATE_THRESHOLD=50
CIRCUIT_BREAKER_WAIT_DURATION=30000
CIRCUIT_BREAKER_PERMITTED_CALLS=10
CIRCUIT_BREAKER_SLIDING_WINDOW_SIZE=100
CIRCUIT_BREAKER_MINIMUM_CALLS=5

# Spring Profile
SPRING_PROFILES_ACTIVE=local
```

## Despliegue con Docker Compose

### Despliegue completo del sistema

```bash
# Desde la raíz del proyecto
docker-compose up --build
```

Este comando:
1. Construye las imágenes Docker de ambos servicios
2. Inicia las dos bases de datos MongoDB
3. Inicia ambos microservicios
4. Configura la red y dependencias automáticamente

### Despliegue de servicios individuales

#### Solo Departamento Service

```bash
docker-compose up --build departamentos-service database-departamentos
```

#### Solo Empleados Service

```bash
docker-compose up --build empleados-service database-empleados departamentos-service
```

### Ver logs en tiempo real

```bash
docker-compose logs -f
```

### Ver logs de un servicio específico

```bash
docker-compose logs -f empleados-service
docker-compose logs -f departamentos-service
```

### Detener el sistema

```bash
# Detener contenedores
docker-compose down

# Detener y eliminar volúmenes (cuidado: borra los datos)
docker-compose down -v
```

### Ver estado de los contenedores

```bash
docker-compose ps
```

## Acceso a los servicios

### Empleados Service

- **Servicio**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/v3/api-docs
- **Actuator Health**: http://localhost:8080/actuator/health

### Departamentos Service

- **Servicio**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **API Docs**: http://localhost:8081/v3/api-docs
- **Actuator Health**: http://localhost:8081/actuator/health

### Perfiles Service

- **Servicio**: http://localhost:8083
- **Swagger UI**: http://localhost:8083/swagger-ui.html
- **API Docs**: http://localhost:8083/v3/api-docs
- **Actuator Health**: http://localhost:8083/actuator/health

### Notificaciones Service

- **Servicio**: http://localhost:8084
- **Swagger UI**: http://localhost:8084/swagger-ui.html
- **API Docs**: http://localhost:8084/v3/api-docs
- **Actuator Health**: http://localhost:8084/actuator/health

### Auth Service

- **Servicio**: http://localhost:8085
- **Swagger UI**: http://localhost:8085/swagger-ui.html
- **API Docs**: http://localhost:8085/v3/api-docs
- **Actuator Health**: http://localhost:8085/actuator/health

### RabbitMQ Management

- **Interfaz Web**: http://localhost:15672
- **Usuario**: admin
- **Contraseña**: admin



## Desarrollo local

### Sin Docker

1. **Iniciar MongoDB manualmente** o usar Docker solo para las bases de datos:

```bash
docker-compose up database-empleados database-departamentos
```

2. **Configurar variables de entorno** en tu sistema para apuntar a localhost

3. **Ejecutar cada servicio**:

```bash
# Departamentos Service
cd departamentos-service
./gradlew bootRun

# En otra terminal
cd empleados-service
./gradlew bootRun
```

### Build manual

```bash
# Departamentos Service
cd departamentos-service
./gradlew clean build

# Empleados Service
cd empleados-service
./gradlew clean build
```





## Flujo de trabajo recomendado

### Sistema Completo (Reto 3)

#### 1. Inicializar el sistema

```bash
docker-compose up --build
```

Esperar a que todos los servicios estén saludables (aproximadamente 2-3 minutos).

#### 2. Verificar salud de los servicios

```bash
# Empleados Service
curl http://localhost:8080/actuator/health

# Departamentos Service
curl http://localhost:8081/actuator/health

# Perfiles Service
curl http://localhost:8083/actuator/health

# Notificaciones Service
curl http://localhost:8084/actuator/health

# Auth Service
curl http://localhost:8085/actuator/health
```

#### 3. Crear departamento (requisito previo)

```bash
curl -X POST http://localhost:8081/departamentos \
  -H "Content-Type: application/json" \
  -d '{
    "id": "IT",
    "nombre": "Tecnología",
    "descripcion": "Departamento de Tecnologías de la Información"
  }'
```

#### 4. Crear empleado (dispara eventos automáticos)

```bash
curl -X POST http://localhost:8080/empleados \
  -H "Content-Type: application/json" \
  -d '{
    "id": "E001",
    "nombre": "Juan Perez",
    "email": "juan@empresa.com",
    "departamentoId": "IT",
    "fechaIngreso": "2026-03-10"
  }'
```

**Flujo automático interno:**
1. empleados-service valida departamento con departamentos-service (REST)
2. Guarda empleado en MongoDB
3. Publica evento `empleado.creado` en RabbitMQ
4. RabbitMQ distribuye evento a:
   - perfiles-service: crea perfil automáticamente
   - notificaciones-service: registra notificación de bienvenida

#### 5. Verificar resultados automáticos

**Consultar perfil creado automáticamente:**
```bash
curl http://localhost:8083/perfiles/E001
```

**Consultar notificación registrada:**
```bash
curl http://localhost:8084/notificaciones/E001
```

**Ver logs de notificaciones:**
```bash
docker-compose logs notificaciones-service | grep "NOTIFICACIÓN"
```

Debe mostrar:
```
[NOTIFICACIÓN]
Tipo: BIENVENIDA
Para: juan@empresa.com
Mensaje: Bienvenido Juan Perez
```

#### 6. Actualizar perfil (información adicional)

```bash
curl -X PUT http://localhost:8083/perfiles/E001 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Carlos Perez",
    "telefono": "+57 300 123 4567",
    "direccion": "Calle 123 #45-67",
    "ciudad": "Bogotá",
    "biografia": "Ingeniero de software con 5 años de experiencia"
  }'
```

#### 7. Listar todos los empleados

```bash
curl http://localhost:8080/empleados?pagina=0&tamano=10
```

#### 8. Filtrar empleados por departamento

```bash
curl "http://localhost:8080/empleados/departamento/IT?pagina=0&tamano=10"
```

#### 9. Eliminar empleado (dispara notificación de desvinculación)

```bash
curl -X DELETE http://localhost:8080/empleados/E001
```

**Flujo automático interno:**
1. Elimina empleado de MongoDB
2. Publica evento `empleado.eliminado` en RabbitMQ
3. notificaciones-service registra notificación de desvinculación

**Verificar notificación de desvinculación:**
```bash
curl http://localhost:8084/notificaciones/E001
```

Debe mostrar una notificación tipo `DESVINCULACION`.

#### 10. Monitoreo

**RabbitMQ Management UI:**
- URL: http://localhost:15672
- Usuario: `admin`
- Contraseña: `admin`

Verificar:
- Exchange `empleados.events` existe
- Colas `cola.perfiles` y `cola.notificaciones` existen
- Mensajes publicados y consumidos

**Swagger UI de cada servicio:**
- Empleados: http://localhost:8080/swagger-ui.html
- Departamentos: http://localhost:8081/swagger-ui.html
- Perfiles: http://localhost:8083/swagger-ui.html
- Notificaciones: http://localhost:8084/swagger-ui.html
- Auth: http://localhost:8085/swagger-ui.html

## Eventos del Sistema (Reto 3)

### empleado.creado

**Publicado por:** empleados-service  
**Cuando:** POST /empleados exitoso  
**Consumidores:** perfiles-service, notificaciones-service  
**Exchange:** `empleados.events` (fanout)  
**Colas:** `cola.perfiles`, `cola.notificaciones`

**Payload:**
```json
{
  "id": "E001",
  "nombre": "Juan Perez",
  "email": "juan@empresa.com",
  "departamentoId": "IT",
  "fechaIngreso": "2026-03-10",
  "tipo": "empleado.creado"
}
```

**Acciones resultantes:**
1. **perfiles-service**: Crea perfil con datos iniciales
   - empleadoId, nombre, email
   - telefono, direccion, ciudad, biografia vacíos
2. **notificaciones-service**: Registra notificación de bienvenida
   - Tipo: BIENVENIDA
   - Mensaje: "Bienvenido [nombre]"

### empleado.eliminado

**Publicado por:** empleados-service  
**Cuando:** DELETE /empleados/{id} exitoso  
**Consumidores:** notificaciones-service  
**Exchange:** `empleados.events` (fanout)  
**Cola:** `cola.notificaciones`

**Payload:**
```json
{
  "id": "E001",
  "nombre": "Juan Perez",
  "email": "juan@empresa.com",
  "tipo": "empleado.eliminado"
}
```

**Acciones resultantes:**
1. **notificaciones-service**: Registra notificación de desvinculación
   - Tipo: DESVINCULACION
   - Mensaje: "Notificación de desvinculación: [nombre]"

### Características de RabbitMQ

**Patrón:** Fan-out (broadcast)
- Un evento publicado se distribuye a MÚLTIPLES colas
- Cada cola recibe una copia del mensaje

**Durabilidad:**
- Colas duraderas (sobreviven reinicios de RabbitMQ)
- Mensajes persistentes (no se pierden si un consumidor está caído)

**Configuración de consumidores:**
- Concurrent consumers: 3-10
- Prefetch count: 10
- SimpleMessageListenerContainer para manejo eficiente

## Endpoints por Servicio

### empleados-service (:8080)

- `POST /empleados` - Crear empleado (publica evento)
- `GET /empleados` - Listar empleados
- `GET /empleados/{id}` - Consultar empleado
- `PUT /empleados/{id}` - Actualizar empleado
- `DELETE /empleados/{id}` - Eliminar empleado (publica evento)
- `GET /empleados/departamento/{departamentoId}` - Filtrar por departamento

### departamentos-service (:8081)

- `POST /departamentos` - Crear departamento
- `GET /departamentos` - Listar departamentos
- `GET /departamentos/{id}` - Consultar departamento

### perfiles-service (:8083)

- `GET /perfiles` - Listar todos los perfiles
- `GET /perfiles/{empleadoId}` - Consultar perfil
- `PUT /perfiles/{empleadoId}` - Actualizar perfil

### notificaciones-service (:8084)

- `GET /notificaciones` - Listar todas las notificaciones
- `GET /notificaciones/{empleadoId}` - Consultar notificaciones por empleado

### auth-service (:8085)

- `POST /auth/login` - Autenticar usuario y obtener token JWT
- `GET /auth/validate` - Validar token JWT
- `POST /auth/forgot-password` - Solicitar recuperación de contraseña
- `POST /auth/reset-password` - Restablecer contraseña con token

## Justificación de RabbitMQ

Se seleccionó RabbitMQ como message broker porque:

### 1. **Patrón Fan-out**
- Permite que un evento sea consumido por múltiples servicios simultáneamente
- Ideal para el requisito de notificar a perfiles-service y notificaciones-service con un solo evento

### 2. **Baja latencia**
- Comunicación asíncrona en tiempo real (< 10ms)
- No bloquea el servicio emisor

### 3. **Persistencia de colas**
- Los mensajes no se pierden si un consumidor está caído
- Colas duraderas configuradas para sobrevivir reinicios

### 4. **Facilidad de uso**
- Simple configuración con Spring AMQP
- Interfaz web de administración incluida (puerto 15672)
- Monitoreo visual de exchanges, colas y mensajes

### 5. **Compatibilidad con Spring**
- Integración nativa con Spring Boot
- Anotaciones `@RabbitListener` para consumo simple
- `RabbitTemplate` para publicación sencilla

### Alternativas consideradas:

| Tecnología | Ventajas | Desventajas | Por qué no se eligió |
|------------|----------|-------------|---------------------|
| **Kafka** | Alto throughput, event sourcing | Más complejo, requiere Zookeeper | Overkill para este caso de uso |
| **Redis Streams** | Ligero, rápido | Menos características de mensajería empresarial | No tiene fan-out nativo |
| **NATS** | Simple, muy rápido | Menos maduro en ecosistema Spring | Menor soporte comunitario |

### Configuración implementada:

```yaml
Exchange: empleados.events (fanout)
Colas:
  - cola.perfiles (durable)
  - cola.notificaciones (durable)
Binding:
  - cola.perfiles -> empleados.events
  - cola.notificaciones -> empleados.events
```

---

## Flujo de pruebas

### Escenario completo: Alta de empleado

#### Paso 1: Crear departamento
```bash
curl -X POST http://localhost:8081/departamentos \
  -H "Content-Type: application/json" \
  -d '{"id":"IT","nombre":"Tecnología","descripcion":"Sistemas"}'
```

**Resultado esperado:** HTTP 201 Created

#### Paso 2: Crear empleado
```bash
curl -X POST http://localhost:8080/empleados \
  -H "Content-Type: application/json" \
  -d '{"id":"E001","nombre":"Juan Perez","email":"juan@empresa.com","departamentoId":"IT","fechaIngreso":"2026-03-10"}'
```

**Resultado esperado:** HTTP 201 Created

**Verificación interna:**
- Empleado guardado en MongoDB empleadosdb
- Evento `empleado.creado` publicado en RabbitMQ

#### Paso 3: Verificar perfil creado automáticamente
```bash
curl http://localhost:8083/perfiles/E001
```

**Resultado esperado:** HTTP 200 OK
```json
{
  "id": "...",
  "empleadoId": "E001",
  "nombre": "Juan Perez",
  "email": "juan@empresa.com",
  "telefono": "",
  "direccion": "",
  "ciudad": "",
  "biografia": "",
  "fechaCreacion": "2026-03-23T..."
}
```

#### Paso 4: Verificar notificación registrada
```bash
curl http://localhost:8084/notificaciones/E001
```

**Resultado esperado:** HTTP 200 OK
```json
[
  {
    "id": "...",
    "tipo": "BIENVENIDA",
    "destinatario": "juan@empresa.com",
    "mensaje": "Bienvenido Juan Perez",
    "fechaEnvio": "2026-03-23T...",
    "empleadoId": "E001"
  }
]
```

#### Paso 5: Actualizar perfil (datos adicionales)
```bash
curl -X PUT http://localhost:8083/perfiles/E001 \
  -H "Content-Type: application/json" \
  -d '{"telefono":"+57 300 123 4567","ciudad":"Bogotá"}'
```

**Resultado esperado:** HTTP 200 OK

#### Paso 6: Listar todos los empleados
```bash
curl "http://localhost:8080/empleados?pagina=0&tamano=10"
```

**Resultado esperado:** HTTP 200 OK con lista de empleados

#### Paso 7: Eliminar empleado
```bash
curl -X DELETE http://localhost:8080/empleados/E001
```

**Resultado esperado:** HTTP 204 No Content

**Verificación interna:**
- Empleado eliminado de MongoDB
- Evento `empleado.eliminado` publicado

#### Paso 8: Verificar notificación de desvinculación
```bash
curl http://localhost:8084/notificaciones/E001
```

**Resultado esperado:** HTTP 200 OK con 2 notificaciones:
1. BIENVENIDA (del paso 4)
2. DESVINCULACION (nueva)

### Validación en RabbitMQ

Acceder a http://localhost:15672

**Verificar:**
1. Exchange `empleados.events` existe y es tipo `fanout`
2. Colas creadas:
   - `cola.perfiles` con mensajes consumidos
   - `cola.notificaciones` con mensajes consumidos
3. Contadores:
   - Total messages published: igual al número de eventos publicados
   - Total messages consumed: igual al número de eventos consumidos

### Pruebas con Swagger UI

Cada servicio tiene documentación interactiva:
- Empleados: http://localhost:8080/swagger-ui.html
- Departamentos: http://localhost:8081/swagger-ui.html
- Perfiles: http://localhost:8083/swagger-ui.html
- Notificaciones: http://localhost:8084/swagger-ui.html

---

## Cómo levantar el sistema

### Opción 1: Docker Compose (Recomendada)

```bash
# Desde la raíz del proyecto
docker-compose up --build
```

Este comando:
1. Construye las imágenes Docker de los 4 servicios
2. Inicia las 4 bases de datos MongoDB
3. Inicia RabbitMQ
4. Configura redes y dependencias automáticamente
5. Expone todos los puertos necesarios

**Tiempo estimado:** 2-3 minutos

### Opción 2: Servicios individuales

```bash
# Solo RabbitMQ y bases de datos
docker-compose up rabbitmq database-empleados database-departamentos database-perfiles database-notificaciones

# Luego ejecutar cada servicio localmente con Gradle
cd empleados-service && ./gradlew bootRun
cd departamentos-service && ./gradlew bootRun
cd perfiles-service && ./gradlew bootRun
cd notificaciones-service && ./gradlew bootRun
```

### Verificación posterior al levantamiento

1. **Verificar contenedores activos:**
```bash
docker-compose ps
```

Todos deben mostrar estado "Up".

2. **Verificar logs:**
```bash
docker-compose logs -f
```

3. **Verificar salud:**
```bash
# Todos los servicios
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
```

### Detener el sistema

```bash
# Detener contenedores (conserva datos)
docker-compose down

# Detener y eliminar volúmenes (borra datos)
docker-compose down -v
```

---

## Seguridad y Autenticación JWT (Reto 4)

El sistema incluye un servicio de autenticación (`auth-service`) que proporciona seguridad basada en tokens JWT (JSON Web Tokens).

### Características de Seguridad

- **Autenticación stateless**: Los tokens JWT permiten autenticación sin estado en el servidor
- **Autorización basada en roles**: Soporte para roles ADMIN y USER
- **Tokens con expiración**: Configuración de tiempo de vida del token
- **Contraseñas encriptadas**: BCrypt para hash de contraseñas
- **Recuperación de contraseña**: Flujo seguro de recuperación con tokens temporales

### Endpoints de Autenticación

#### 1. Login - POST /auth/login

Autentica un usuario y retorna un token JWT.

**Request:**
```json
{
  "username": "juan.perez",
  "password": "password123"
}
```

**Response 200 OK:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "juan.perez",
  "role": "USER",
  "expiresIn": 86400000
}
```

**Códigos de respuesta:**
- `200` - Login exitoso
- `400` - Datos inválidos o formato incorrecto
- `401` - Credenciales inválidas (usuario o contraseña incorrectos)
- `403` - Usuario inactivo o bloqueado
- `500` - Error interno del servidor

#### 2. Validar Token - GET /auth/validate?token={token}

Valida si un token JWT es válido y retorna información del usuario.

**Response 200 OK:**
```json
{
  "valid": true,
  "username": "juan.perez",
  "role": "USER"
}
```

**Códigos de respuesta:**
- `200` - Token válido
- `400` - Token no proporcionado o formato inválido
- `401` - Token inválido o expirado
- `403` - Acceso denegado - usuario no autorizado
- `500` - Error interno al validar token

#### 3. Recuperar Contraseña - POST /auth/forgot-password

Solicita un token de recuperación de contraseña.

**Request:**
```json
{
  "email": "juan@empresa.com"
}
```

**Response 200 OK (cuando el usuario existe):**
```json
{
  "message": "Token de recuperación generado",
  "resetToken": "abc123xyz456...",
  "instruction": "Usa este token en POST /auth/reset-password con tu nueva contraseña"
}
```

**Response 200 OK (cuando el usuario NO existe - por seguridad):**
```json
{
  "message": "Si el email existe, se ha enviado el token de recuperación"
}
```

**Códigos de respuesta:**
- `200` - Token generado exitosamente (o email no existe por seguridad)
- `400` - Formato de email inválido
- `403` - Usuario inactivo o bloqueado - no se permite recuperación
- `500` - Error interno

**Nota de seguridad:** Por razones de seguridad, la API siempre retorna éxito aunque el email no exista, para evitar enumeración de usuarios. **El token solo se muestra en la respuesta cuando el usuario existe realmente.**

#### 4. Restablecer Contraseña - POST /auth/reset-password

Establece una nueva contraseña usando el token de recuperación.

**Request:**
```json
{
  "token": "abc123xyz...",
  "newPassword": "nuevaPassword123"
}
```

**Response 200 OK:**
```json
{
  "message": "Contraseña restablecida exitosamente"
}
```

**Códigos de respuesta:**
- `200` - Contraseña restablecida
- `400` - Token inválido, expirado o contraseña débil
- `500` - Error interno

### Uso de Tokens JWT

Para acceder a endpoints protegidos, incluir el token en el header:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Resumen de Códigos de Respuesta HTTP

El auth-service utiliza los siguientes códigos de respuesta HTTP según el estándar REST:

#### Códigos de Éxito (2xx)
- **`200 OK`**: Solicitud exitosa
  - Login correcto
  - Token válido
  - Contraseña restablecida
  - Recuperación solicitada

#### Códigos de Error del Cliente (4xx)
- **`400 Bad Request`**: Datos inválidos o formato incorrecto
  - Campos requeridos faltantes
  - Formato de email inválido
  - Contraseña muy débil
  - Token con formato incorrecto
  
- **`401 Unauthorized`**: Credenciales o token inválidos
  - Usuario o contraseña incorrectos
  - Token JWT inválido
  - Token JWT expirado
  
- **`403 Forbidden`**: Acceso denegado
  - Usuario inactivo
  - Usuario bloqueado
  - No tiene permisos para la acción
  - Usuario no autorizado para recuperación
  
- **`404 Not Found`**: Recurso no encontrado
  - Usuario no existe (aunque por seguridad no se revela en forgot-password)
  - Token de recuperación no existe
  
- **`409 Conflict`**: Conflicto con el estado actual
  - Email ya registrado (al crear usuario)
  - Username ya existe

#### Códigos de Error del Servidor (5xx)
- **`500 Internal Server Error`**: Error interno del servidor
  - Error al procesar login
  - Error al validar token
  - Error al generar token de recuperación
  - Error al restablecer contraseña
  - Error de conexión con MongoDB
  - Error de conexión con RabbitMQ

### Configuración de Seguridad

**Variables de entorno requeridas:**

```properties
# JWT Configuration
JWT_SECRET=tu_secreto_muy_seguro_y_largo_minimo_32_caracteres
JWT_EXPIRATION=86400000
```

**Recomendaciones de producción:**
- Usar un JWT_SECRET único y seguro (mínimo 32 caracteres)
- Rotar el secreto periódicamente
- Usar HTTPS en producción
- Configurar tiempos de expiración apropiados
- Implementar refresh tokens para sesiones largas

### Base de Datos de Usuarios

El auth-service utiliza MongoDB para almacenar:
- Credenciales de usuarios (contraseñas encriptadas con BCrypt)
- Información de usuarios (username, email, rol)
- Tokens de recuperación de contraseña
- Estados de cuenta (activo/inactivo)

### Flujo de Autenticación

```
Cliente → POST /auth/login → Auth Service
                              ↓
                         Validar credenciales
                              ↓
                         Generar JWT Token
                              ↓
                         Retornar Token
                              ↓
Cliente ← Token JWT ←───────┘
                              ↓
Cliente → Request con Token → Servicio Protegido
                              ↓
                         Validar Token (JwtAuthenticationFilter)
                              ↓
                         Procesar Request
```

---
## Reto 6 - Integracion Continua con Jenkins

### Que es CI?

La **Integracion Continua (CI)** es una practica de desarrollo de software en la que los desarrolladores integran su codigo en un repositorio compartido de forma frecuente, y cada integracion se verifica automaticamente mediante compilacion y pruebas. Esto permite detectar problemas en minutos, no en dias.

| Aspecto | Sin CI | Con CI |
|---------|--------|--------|
| Compilacion | Manual, cuando alguien se acuerda | Automatica, en cada push |
| Pruebas | Se ejecutan localmente (o no se ejecutan) | Se ejecutan automaticamente en cada cambio |
| Deteccion de errores | Al intentar desplegar (tarde) | En minutos tras el commit (temprano) |
| Confianza en el codigo | "En mi maquina funciona" | El pipeline lo verifica en un entorno limpio |
| Empaquetado | Manual: docker build en la terminal | Automatico: imagen Docker generada por el pipeline |

### Arquitectura CI del Sistema

```
Desarrollador -> git push -> Jenkins (CI) -> Build -> Test -> SonarQube -> Quality Gate -> Docker Build -> Registry
                                                                       |
                                                                       v
                                                                E2E Tests (BDD)
```

### Acceso a los Servicios CI

| Servicio | URL | Credenciales |
|----------|-----|--------------|
| **Jenkins** | http://localhost:9090 | admin / admin |
| **SonarQube** | http://localhost:9000 | admin / admin (cambiar en primera vez) |
| **Docker Registry** | http://localhost:5000 | Sin autenticacion |

### Estructura de Archivos CI

```
proyecto/
├── jenkins/
│   ├── Dockerfile                    # Jenkins con plugins pre-instalados
│   └── init.groovy.d/
│       └── create-jobs.groovy        # Creacion automatica de pipelines al iniciar
├── empleados-service/
│   ├── Jenkinsfile                   # Pipeline CI para Java/Gradle
│   └── sonar-project.properties
├── python-healthcheck-service/
│   ├── src/
│   │   ├── app.py                    # Microservicio Flask
│   │   ├── requirements.txt
│   │   └── test/
│   │       └── test_app.py           # Pruebas unitarias (7 tests)
│   ├── Dockerfile
│   ├── Jenkinsfile                   # Pipeline CI para Python
│   └── sonar-project.properties
├── e2e-tests/
│   ├── Jenkinsfile                   # Pipeline E2E con Cucumber
│   └── src/test/...                  # Pruebas BDD del Reto 5
├── docker-compose.yml                # Incluye Jenkins, SonarQube, Registry
└── README.md
```

### Como Levantar el Sistema con CI

```bash
# Desde la raiz del proyecto
docker-compose up --build
```

**Nota:** La primera vez puede tardar varios minutos porque:
1. Se descargan las imagenes de Jenkins, SonarQube, PostgreSQL
2. Jenkins instala los plugins especificados en el Dockerfile
3. SonarQube inicializa su base de datos

### Pipelines Disponibles

| Pipeline | Lenguaje | Etapas |
|----------|----------|--------|
| `empleados-service-ci` | Java/Groovy/Gradle | Checkout -> Build -> Test (JaCoCo) -> SonarQube -> Quality Gate -> Package -> Publish |
| `python-healthcheck-service-ci` | Python/Flask | Checkout -> Install Deps -> Test (pytest-cov) -> SonarQube -> Quality Gate -> Package -> Publish |
| `e2e-tests-ci` | Java/Maven/Cucumber | Checkout -> Deploy System -> Wait for Services -> E2E Tests -> Cleanup |
| `hello-world` | Pipeline prueba | Verificacion de Jenkins y Docker |

### Descripcion de Etapas del Pipeline

| Etapa | Descripcion | Que verifica |
|-------|-------------|--------------|
| **Checkout** | Obtiene el codigo fuente del repositorio | Que el repositorio sea accesible |
| **Build** | Compila el proyecto (sin pruebas) | Que el codigo compile sin errores |
| **Test** | Ejecuta pruebas unitarias y genera reporte de cobertura | Que todas las pruebas pasen y se genere cobertura |
| **SonarQube Analysis** | Envia el codigo y reportes a SonarQube | Analisis estatico: bugs, vulnerabilidades, code smells |
| **Quality Gate** | Espera el resultado del Quality Gate de SonarQube | Que la calidad del codigo cumpla los umbrales (cobertura >= 70%) |
| **Package** | Construye la imagen Docker del servicio | Que el Dockerfile construya correctamente |
| **Publish** | Publica la imagen en el Docker Registry local | Que la imagen se pueda almacenar y distribuir |
| **E2E Tests** | Levanta el sistema completo y ejecuta pruebas BDD | Que el sistema funcione integralmente |

### Interpretacion de Resultados

- **Verde (exito)**: Todas las etapas pasaron correctamente
- **Rojo (fallo)**: El pipeline se detuvo en una etapa. Revisar logs para identificar la causa
- **Amarillo (inestable)**: Algunas pruebas fallaron pero el pipeline continuo

### Simulacion de Fallos

Para probar que el pipeline detecta errores correctamente:

1. **Fallo en Test**: Modificar un test para que falle intencionalmente
2. **Fallo en Quality Gate**: Reducir la cobertura por debajo del 70% eliminando pruebas
3. **Fallo en Package**: Introducir un error en el Dockerfile
4. **Fallo en E2E**: Modificar un escenario BDD para que falle

### Configuracion de SonarQube

1. Acceder a http://localhost:9000
2. Login con admin/admin (cambiar contrasena)
3. Ir a "Administration -> Webhooks" y crear webhook:
   - Name: `jenkins-webhook`
   - URL: `http://jenkins:8080/sonarqube-webhook/`
4. Ir a "Quality Gates" y crear uno personalizado:
   - Name: `Microservicios Quality Gate`
   - Condiciones:
     - Coverage on New Code >= 70%
     - Bugs = 0
     - Vulnerabilities = 0
5. Generar token en "User -> My Account -> Security"

### Notas Tecnicas

- **Jenkins** usa Docker Socket Mount para construir imagenes Docker dentro del contenedor
- **SonarQube** usa PostgreSQL como base de datos separada
- **Docker Registry** es local (sin autenticacion para entorno academico)
- **init.groovy.d** crea los pipelines automaticamente al iniciar Jenkins, leyendo los Jenkinsfiles del codigo fuente montado como volumen (`/code/`)
- Los pipelines usan agentes Docker efimeros definidos con `agent { docker { ... } }` para no instalar herramientas en el contenedor Jenkins
- El cache de dependencias (Gradle, Pip) se mantiene con volumenes montados para evitar descargar todo cada vez
- La etapa `Publish to Registry` envia las imagenes al Docker Registry local en `localhost:5000`

### Puertos del Sistema Completo

| Puerto | Servicio |
|--------|----------|
| 8080 | empleados-service |
| 8081 | departamentos-service |
| 8083 | perfiles-service |
| 8084 | notificaciones-service |
| 8085 | auth-service |
| 8086 | python-healthcheck-service |
| 9000 | SonarQube |
| 9090 | Jenkins |
| 5000 | Docker Registry |
| 5672 | RabbitMQ |
| 15672 | RabbitMQ Management |
| 27017-27021 | Bases de datos MongoDB |
