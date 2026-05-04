# Suite de Pruebas BDD - Sistema de Onboarding y Offboarding de Empleados

## ¿Que es BDD?

Behavior-Driven Development (BDD) es una metodologia de desarrollo que extiende TDD al enfocar las pruebas en el comportamiento del sistema desde la perspectiva del usuario, no en la implementacion tecnica. Los escenarios se escriben en lenguaje natural estructurado (Gherkin) y se automatizan para validar el sistema completo.

Se eligio BDD para este proyecto porque:
- Permite documentar el comportamiento del sistema de forma ejecutable
- Facilita la comunicacion entre desarrolladores, QA y stakeholders
- Los escenarios en lenguaje natural son faciles de entender y mantener
- Se integra naturalmente con pruebas de extremo a extremo contra el sistema desplegado

## Prerrequisitos

- Docker y Docker Compose instalados
- Java 17+ (solo si se ejecutan las pruebas localmente)
- Maven 3.9+ (solo si se ejecutan las pruebas localmente)

## Estructura del Proyecto

```
e2e-tests/
  features/              -- Archivos .feature (Gherkin en espanol)
    smoke.feature        -- Verificacion de que el sistema esta operativo
    security.feature     -- Pruebas de seguridad y control de acceso RBAC
    onboarding.feature   -- Pruebas de registro de empleados
    offboarding.feature  -- Pruebas de desvinculacion de empleados
  step_definitions/      -- Implementacion de los pasos Gherkin
    AuthSteps.java       -- Pasos relacionados con autenticacion
    CommonSteps.java     -- Pasos comunes y de seguridad
    EmployeeSteps.java   -- Pasos de operaciones con empleados
    SecuritySteps.java   -- Pasos adicionales de seguridad
  support/               -- Contexto compartido, hooks y utilidades
    TestContext.java     -- Contexto compartido entre pasos (Singleton)
    Hooks.java           -- Configuracion y limpieza antes/despues de cada escenario
    PollingUtils.java    -- Utilidad de polling para verificaciones asincronas
  runner/
    TestRunner.java      -- Runner de Cucumber que ejecuta todas las pruebas
```

## Herramientas y Frameworks

| Herramienta | Version | Proposito |
|---|---|---|
| Cucumber-JVM | 7.14.0 | Framework BDD para ejecutar escenarios Gherkin |
| Rest Assured | 5.3.2 | Cliente HTTP para peticiones REST contra el sistema |
| JUnit 4 | 4.13.2 | Ejecutor de pruebas |
| Jackson | 2.15.3 | Procesamiento de JSON |
| Maven | 3.9+ | Gestion de dependencias y ejecucion |

### Justificacion de la eleccion

- **Cucumber-JVM**: Es el framework BDD mas maduro para Java, con amplia documentacion y soporte para Gherkin en espanol.
- **Rest Assured**: Proporciona una API fluida y expresiva para realizar peticiones HTTP y validar respuestas, ideal para pruebas de API REST.
- **JUnit 4**: Compatibilidad probada con Cucumber-JVM 7.x y configuracion sencilla.
- **Maven**: Gestion de dependencias automatica y ejecucion con un solo comando.

## Variables de Entorno

| Variable | Descripcion | Valor por Defecto |
|---|---|---|
| `BASE_URL` | URL base del sistema | `http://localhost:8085` |
| `AUTH_URL` | URL del servicio de autenticacion | (usa BASE_URL) |
| `EMPLEADOS_URL` | URL del servicio de empleados | (usa BASE_URL) |
| `DEPARTAMENTOS_URL` | URL del servicio de departamentos | (usa BASE_URL) |
| `NOTIFICACIONES_URL` | URL del servicio de notificaciones | (usa BASE_URL) |
| `ADMIN_USER` | Usuario administrador | `admin` |
| `ADMIN_PASSWORD` | Contrasena del administrador | `admin123` |
| `REGULAR_USER` | Usuario regular | `user` |
| `REGULAR_PASSWORD` | Contrasena del usuario regular | `user123` |

## Instrucciones de Ejecucion

### 1. Levantar el sistema

Desde la raiz del proyecto:

```bash
docker-compose up --build -d
```

Esto construye e inicia todos los microservicios, bases de datos, RabbitMQ y las pruebas BDD.

### 2. Ejecutar las pruebas

Las pruebas se ejecutan automaticamente como parte del docker-compose. Para ejecutarlas manualmente dentro del contenedor:

```bash
docker-compose run --rm e2e-tests
```

O si se tiene Maven instalado localmente:

```bash
cd e2e-tests
mvn test -DbaseUrl="http://localhost:8085"
```

### 3. Interpretar los resultados

Los reportes se generan en:
- `e2e-tests/target/cucumber-reports/report.html` - Reporte HTML legible
- `e2e-tests/target/surefire-reports/` - Reportes de JUnit

En la consola se muestra un resumen con el numero de escenarios pasados, fallidos y pendientes.

## Escenarios Implementados

### Punto 1: Verificacion del Sistema (Smoke Test)
- **El sistema responde correctamente**: Verifica que el endpoint base del sistema responde con codigo 200.

### Punto 2: Seguridad y Control de Acceso (4 escenarios)
- **Acceso denegado sin token de autenticacion**: Consulta empleados sin token -> 401
- **Acceso con token invalido**: Usa token malformado -> 401
- **Usuario ADMIN puede crear empleados**: ADMIN crea empleado -> 201
- **Usuario USER no puede crear empleados**: USER intenta crear empleado -> 403

### Punto 3: Onboarding de Empleados (6 escenarios)
- **Registro exitoso de empleado**: Creacion con verificacion asincronica de existencia
- **Verificar creacion de credenciales via evento**: Verifica que el servicio de auth crea el usuario asincronicamente
- **Verificar notificacion de registro**: Verifica que se genera notificacion de tipo CREACION
- **Nuevo empleado puede establecer contrasena y hacer login**: Flujo completo de registro, cambio de contrasena y login
- **Registro con departamento inexistente**: Error 400 por departamento invalido
- **Registro con datos faltantes**: Error 400 por campos obligatorios faltantes

### Punto 4: Offboarding de Empleados (3 escenarios)
- **Desvinculacion completa de empleado**: Eliminacion con verificacion asincronica y notificacion de tipo DESVINCULACION
- **Empleado desvinculado no puede hacer login**: Verifica que el login falla tras la desvinculacion
- **Recuperacion de contrasena falla para empleado desvinculado**: Verifica que la recuperacion falla con 404

## Estrategia de Polling

Para manejar la eventual consistencia del sistema basado en eventos, se utiliza polling con los siguientes parametros:

| Parametro | Valor | Justificacion |
|---|---|---|
| Maximo de intentos | 15 | Suficiente para el procesamiento de eventos |
| Intervalo entre intentos | 1500 ms (1.5 segundos) | Balance entre velocidad y carga en el sistema |
| Timeout total | ~22.5 segundos | Limite razonable para procesamiento asincronico |

No se utiliza `Thread.sleep()` fijo. El polling es superior porque:
- La prueba termina mas rapido si el evento se procesa rapidamente
- Tolela variaciones de tiempo sin fallar de forma intermitente

## Aislamiento de Escenarios

Cada escenario es independiente y no depende del resultado de otros:
- El contexto (`TestContext`) se limpia entre escenarios via hooks `@Before` y `@After`
- Los IDs de empleados son unicos por escenario
- El `Background` recrea el estado necesario para cada escenario
- Los departamentos se crean solo si no existen (operacion idempotente)
