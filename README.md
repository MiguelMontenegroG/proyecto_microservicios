# Sistema de Microservicios

Sistema de microservicios para la gestión de empleados y departamentos con comunicación asíncrona basada en eventos usando RabbitMQ.

## Descripción del Sistema

Este sistema implementa una arquitectura de microservicios que combina:
- **Comunicación síncrona (REST)**: Para operaciones directas entre servicios
- **Comunicación asíncrona (eventos)**: Para propagar cambios automáticamente mediante RabbitMQ (patrón fan-out)

### Características principales

* **4 microservicios independientes** (empleados, departamentos, perfiles, notificaciones)  
* **4 bases de datos MongoDB** (una por servicio, sin compartir datos)  
* **RabbitMQ** como message broker para eventos  
* **Patrón Fan-out**: Un evento → Múltiples consumidores  
* **OpenAPI/Swagger** en todos los servicios  
* **Dockerizado** y listo para desplegar con docker-compose  
* **Resiliente** con Resilience4j (circuit breaker)  
* **Automatización**: Creación de perfil y notificación al crear empleado

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
┌─────────┐  ┌────────────┐                   │
│ Perfiles│  │Notificaciones                  │
│Service  │  │Service     │                   │
│:8083    │  │:8084       │                   │
└────┬────┘  └─────┬──────┘                   │
     │             │                          │
     ▼             ▼                          │
┌─────────┐  ┌────────────┐                  │
│ MongoDB │  │  MongoDB   │                  │
│Perfiles │  │Notificaciones                 │
└─────────┘  └────────────┘                  │
                                              │
            ┌─────────────────────────────────┘
            │
            ▼
      ┌───────────┐
      │  MongoDB  │
      │Departamentos
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
- ✅ Protocolo AMQP estándar y maduro
- ✅ Fácil configuración y uso
- ✅ Soporta múltiples patrones: colas, publish/subscribe, routing, topics
- ✅ Gestión mediante UI web incluida (RabbitMQ Management)
- ✅ Persistencia de mensajes configurable
- ✅ Acknowledgment de mensajes
- ✅ Dead letter queues para manejo de errores
- ✅ Excelente documentación y comunidad
- ✅ Ligero y fácil de containerizar

**Desventajas:**
- ❌ Menor throughput que Kafka en escenarios de muy alta carga
- ❌ No es ideal para event sourcing a largo plazo

**Casos de uso ideales:**
- Comunicación entre microservicios
- Colas de tareas
- Notificaciones en tiempo real
- Sistemas con patrones complejos de enrutamiento

#### 2. Apache Kafka

**Ventajas:**
- ✅ Alto throughput extremo (millones de mensajes/segundo)
- ✅ Persistencia a largo plazo (logs inmutables)
- ✅ Ideal para event sourcing y CQRS
- ✅ Escalabilidad horizontal masiva
- ✅ Múltiples consumidores independientes

**Desventajas:**
- ❌ Mayor complejidad de configuración y operación
- ❌ Requiere ZooKeeper (aunque está cambiando en versiones recientes)
- ❌ Overkill para sistemas pequeños/medianos
- ❌ Curva de aprendizaje más pronunciada
- ❌ Más recursos necesarios (memoria, CPU)

**Casos de uso ideales:**
- Streaming de datos a gran escala
- Event sourcing
- Procesamiento de logs distribuido
- Sistemas que requieren retención prolongada de mensajes

#### 3. Redis Streams / NATS

**Redis Streams:**

**Ventajas:**
- ✅ Muy baja latencia
- ✅ Simple y ligero
- ✅ Si ya usas Redis, es una característica adicional

**Desventajas:**
- ❌ Funcionalidades limitadas comparado con RabbitMQ/Kafka
- ❌ Persistencia limitada
- ❌ Patrones de mensajería menos maduros
- ❌ No es su propósito principal

**NATS:**

**Ventajas:**
- ✅ Extremadamente rápido y ligero
- ✅ Simple de operar
- ✅ Bueno para IoT y mensajería en tiempo real

**Desventajas:**
- ❌ Menor adopción en enterprise
- ❌ Ecosistema más pequeño
- ❌ Menor madurez en patrones avanzados

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
MONGODB_URI=mongodb://database-empleados:27017/
MONGODB_DATABASE_EMPLEADOS=empleadosdb
MONGODB_DATABASE_DEPARTAMENTOS=departamentosdb
MONGODB_DATABASE_PERFILES=perfilesdb
MONGODB_DATABASE_NOTIFICACIONES=notificacionesdb

# URLs entre servicios
DEPARTAMENTOS_SERVICE_URL=http://departamentos-service:8081

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
```

### Detener el sistema

```bash
# Detener contenedores (conserva datos)
docker-compose down

# Detener y eliminar volúmenes (borra datos)
docker-compose down -v
```

---