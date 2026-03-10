# Departamentos Service

Servicio de gestión de departamentos para el sistema de microservicios.

## Descripción

Este servicio gestiona la información de los departamentos utilizando MongoDB como base de datos y está implementado en Groovy con Spring Boot 3.2.5.

## Tecnologías

- **Lenguaje**: Groovy
- **Framework**: Spring Boot 3.2.5
- **Base de datos**: MongoDB 7.0
- **Resiliencia**: Resilience4j (Circuit Breaker, Time Limiter)
- **Documentación API**: OpenAPI/Swagger
- **Build Tool**: Gradle
- **Java**: 21

## Puertos

- **Puerto del servicio**: 8081 (configurable via `DEPARTAMENTOS_SERVICE_PORT`)

## Endpoints principales

### Swagger UI
```
http://localhost:8081/swagger-ui.html
```

### API Docs
```
http://localhost:8081/v3/api-docs
```

### Actuator
```
http://localhost:8081/actuator/health
http://localhost:8081/actuator/info
http://localhost:8081/actuator/metrics
http://localhost:8081/actuator/circuitbreakers
```

## Configuración

### Variables de entorno requeridas

```properties
# Puerto del servicio
DEPARTAMENTOS_SERVICE_PORT=8081

# Conexión a MongoDB
MONGODB_URI=mongodb://localhost:27017/
MONGODB_DATABASE_DEPARTAMENTOS=departamentosdb

# Logging
LOGGING_LEVEL_ROOT=INFO
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

## Despliegue

### Opción 1: Usando Docker Compose (Recomendado)

Desde la raíz del proyecto:

```bash
# Construir y levantar solo el servicio de departamentos
docker-compose up --build departamentos-service database-departamentos
```

### Opción 2: Build manual con Gradle

```bash
# Navegar al directorio del servicio
cd departamentos-service

# Construir el proyecto
./gradlew build

# Ejecutar la aplicación
./gradlew bootRun
```

### Opción 3: Usando el JAR generado

```bash
# Construir el JAR
./gradlew clean build

# Ejecutar el JAR
java -jar build/libs/departamentos-service-0.0.1-SNAPSHOT.jar
```









## Estructura del proyecto

```
departamentos-service/
├── src/
│   ├── main/
│   │   ├── groovy/com/microservicios/departamentos_service/
│   │   │   ├── config/          # Configuraciones (OpenAPI, Resilience4j)
│   │   │   ├── controller/      # Controladores REST
│   │   │   ├── model/           # Modelos de datos
│   │   │   ├── repository/      # Repositorios MongoDB
│   │   │   ├── service/         # Lógica de negocio
│   │   │   └── DepartamentosServiceApplication.groovy
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── groovy/com/microservicios/departamentos_service/
├── build.gradle
├── Dockerfile
└── settings.gradle
```
