# Sistema de Microservicios - Empleados y Departamentos

Sistema de microservicios para la gestión de empleados y departamentos, implementado con Spring Boot 3.2.5, Groovy, MongoDB y Docker.

## Descripción del Sistema

Este sistema consiste en dos microservicios independientes pero relacionados:

1. **Departamentos Service**: Gestiona la información de los departamentos
2. **Empleados Service**: Gestiona empleados y usuarios, validando departamentos

## Arquitectura

```
┌─────────────────────┐         ┌──────────────────────┐
│   Empleados         │  HTTP   │  Departamentos       │
│   Service :8080     │ ──────> │  Service :8081       │
│                     │         │                      │
│  - Empleados        │         │  - Departamentos     │
│  - Usuarios         │         │                      │
└─────────┬───────────┘         └──────────┬───────────┘
          │                                │
          ▼                                ▼
┌─────────────────────┐         ┌──────────────────────┐
│   MongoDB           │         │   MongoDB            │
│   Empleados         │         │   Departamentos      │
└─────────────────────┘         └──────────────────────┘
```

## Tecnologías

- **Backend**: Spring Boot 3.2.5 con Groovy
- **Bases de datos**: MongoDB 7.0 (una por servicio)
- **Comunicación**: REST/HTTP
- **Resiliencia**: Resilience4j (Circuit Breaker, Time Limiter)
- **Documentación**: OpenAPI/Swagger
- **Containerización**: Docker y Docker Compose
- **Build Tool**: Gradle
- **Java**: 21

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

# Puertos
EMPLEADOS_SERVICE_PORT=8080
DEPARTAMENTOS_SERVICE_PORT=8081
MONGODB_EMPL_PORT=27017
MONGODB_DEP_PORT=27018

# MongoDB URIs
MONGODB_URI=mongodb://database-empleados:27017/
MONGODB_DATABASE_EMPLEADOS=empleadosdb
MONGODB_DATABASE_DEPARTAMENTOS=departamentosdb

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

1. **Inicializar el sistema**: `docker-compose up --build`
2. **Verificar salud**: Usar Swagger UI o actuator endpoints
3. **Crear departamentos primero**: Los empleados requieren departamentos válidos
4. **Crear empleados**: Con referencias a departamentos existentes
5. **Crear usuarios**: Asociados a empleados
6. **Monitorear**: Usar actuator endpoints para health checks y métricas




