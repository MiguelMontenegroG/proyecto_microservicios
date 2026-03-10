# Empleados Service

Servicio de gestión de empleados para el sistema de microservicios.

## Descripción

Este servicio gestiona la información de los empleados y usuarios, validando la existencia de departamentos mediante comunicación con el `departamentos-service`. Está implementado en Groovy con Spring Boot 3.2.5.

## Tecnologías

- **Lenguaje**: Groovy
- **Framework**: Spring Boot 3.2.5
- **Base de datos**: MongoDB 7.0
- **Resiliencia**: Resilience4j (Circuit Breaker, Time Limiter)
- **Documentación API**: OpenAPI/Swagger
- **Build Tool**: Gradle
- **Java**: 21

## Puertos

- **Puerto del servicio**: 8080 (configurable via `EMPLEADOS_SERVICE_PORT`)

## Endpoints principales

### Swagger UI
```
http://localhost:8080/swagger-ui.html
```

### API Docs
```
http://localhost:8080/v3/api-docs
```

### Actuator
```
http://localhost:8080/actuator/health
http://localhost:8080/actuator/info
http://localhost:8080/actuator/metrics
http://localhost:8080/actuator/circuitbreakers
```

## Configuración

### Variables de entorno requeridas

```properties
# Puerto del servicio
EMPLEADOS_SERVICE_PORT=8080

# Conexión a MongoDB
MONGODB_URI=mongodb://localhost:27017/
MONGODB_DATABASE_EMPLEADOS=empleadosdb

# URL del servicio de departamentos
DEPARTAMENTOS_SERVICE_URL=http://departamentos-service:8081

# Logging
LOGGING_LEVEL_ROOT=INFO
LOGGING_LEVEL_EMPLEADOS=INFO
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
# Construir y levantar todo el sistema
docker-compose up --build
```

O solo el servicio de empleados:

```bash
docker-compose up --build empleados-service database-empleados departamentos-service
```

### Opción 2: Build manual con Gradle

```bash
# Navegar al directorio del servicio
cd empleados-service

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
java -jar build/libs/empleados-service-0.0.1-SNAPSHOT.jar
```





## Características especiales

### Validación de departamentos

El servicio valida que los departamentos existan antes de crear o actualizar empleados. Esta validación se realiza mediante una llamada HTTP al `departamentos-service`.

### Circuit Breaker

Implementa un patrón Circuit Breaker para manejar fallos en la comunicación con el servicio de departamentos. Cuando el circuit breaker está abierto, el servicio puede continuar operando sin validar departamentos temporalmente.





## Estructura del proyecto

```
empleados-service/
├── src/
│   ├── main/
│   │   ├── groovy/com/microservicios/empleados_service/
│   │   │   ├── config/          # Configuraciones (OpenAPI, Resilience4j, RestTemplate)
│   │   │   ├── controller/      # Controladores REST (Empleados, Usuarios)
│   │   │   ├── model/           # Modelos de datos (Empleado, Usuario)
│   │   │   ├── repository/      # Repositorios MongoDB
│   │   │   ├── service/         # Lógica de negocio y validación
│   │   │   └── EmpleadosServiceApplication.groovy
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── groovy/com/microservicios/empleados_service/
├── build.gradle
├── Dockerfile
└── settings.gradle
```
