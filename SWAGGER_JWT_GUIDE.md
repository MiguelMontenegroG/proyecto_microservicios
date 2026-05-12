# Guía Rápida: Uso de Swagger UI con JWT

## ✅ Estado Actual del Proyecto

Los servicios de **empleados-service** y **auth-service** están protegidos con autenticación JWT.

---

## 🔐 Credenciales de Acceso

```
Usuario: admin
Contraseña: password123
Rol: ADMIN
```

---

## 📋 Pasos para Usar Swagger UI

### 1️⃣ Abrir Swagger UI

Accede a: **[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)**

### 2️⃣ Obtener Token JWT

**Opción A - Usando el script PowerShell:**
```powershell
powershell -ExecutionPolicy Bypass -File "test-scripts/test-empleados-jwt.ps1"
```

**Opción B - Desde Swagger del auth-service:**
1. Abre [http://localhost:8085/swagger-ui.html](http://localhost:8085/swagger-ui.html)
2. Busca el endpoint `POST /auth/login`
3. Usa este JSON en el body:
```json
{
  "username": "admin",
  "password": "password123"
}
```
4. Ejecuta y copia el `token` de la respuesta

### 3️⃣ Autorizar en Swagger

1. En Swagger UI, haz clic en el botón **"Authorize"** (candado arriba a la derecha)
2. En el campo `Value`, escribe: `Bearer <tu-token-completo>`
   - Ejemplo: `Bearer eyJhbGciOiJIUzUxMiJ9.eyJyb2xlIjoiQURNSU4iLCJzdWIiOiJhZG1pbiIs...`
3. Haz clic en **"Authorize"**
4. Haz clic en **"Close"**

### 4️⃣ Probar Endpoints

Ahora puedes usar todos los endpoints de `/empleados`:

#### ✅ Endpoints Disponibles:

- **GET /empleados** - Listar todos los empleados (paginado)
  - Parámetros: `pagina=0`, `tamano=10`
  
- **POST /empleados** - Crear nuevo empleado
  - Body ejemplo:
  ```json
  {
    "id": "EMP001",
    "nombre": "Juan Pérez",
    "email": "juan.perez@empresa.com",
    "departamentoId": "IT",
    "fechaIngreso": "2024-01-15"
  }
  ```

- **GET /empleados/{id}** - Obtener empleado por ID

- **PUT /empleados/{id}** - Actualizar empleado

- **DELETE /empleados/{id}** - Eliminar empleado

- **GET /empleados/departamento/{departamentoId}** - Listar por departamento

---

## 🧪 Endpoints Públicos (Sin Autenticación)

Estos endpoints NO requieren autenticación:

- `/test-db/ping` - Verificar conexión a MongoDB
- `/test-db/connection-status` - Estado de la conexión
- `/test-db/collections-info` - Información de colecciones
- `/swagger-ui/**` - Interfaz Swagger
- `/v3/api-docs/**` - Documentación OpenAPI
- `/actuator/**` - Actuator endpoints

---

## ⚠️ Solución de Problemas

### Error 401 Unauthorized
- El token expiró (dura 1 hora)
- El token está mal formado
- Falta el prefijo "Bearer " antes del token

### Error 403 Forbidden
- El usuario no tiene permisos suficientes
- Verifica que el rol sea ADMIN

### Error 500 Internal Server Error
- Problema con validación de departamentos
- Verifica que el `departamentoId` exista en departamentos-service
- Revisa los logs: `docker logs empleados-service --tail 50`

### Error al Listar Empleados
- Verifica que MongoDB esté corriendo
- Prueba el endpoint público `/test-db/ping`

---

## 🛠️ Comandos Útiles

### Ver logs del servicio:
```powershell
docker logs empleados-service -f
```

### Reiniciar el servicio:
```powershell
docker-compose restart empleados-service
```

### Ver estado de contenedores:
```powershell
docker-compose ps
```

---

## 📝 Notas Importantes

1. **Token Expiración**: El token JWT dura 1 hora (3600000 ms)
2. **Departamentos Válidos**: Antes de crear un empleado, asegúrate de que el departamento exista
3. **ID Único**: El ID del empleado debe ser único, si intentas crear uno repetido obtendrás 409 Conflict
4. **Swagger Configurado**: El Swagger ya incluye el esquema de seguridad Bearer JWT

---

## 🎯 Flujo Recomendado

1. Inicia todos los servicios con `docker-compose up -d`
2. Ejecuta el script `test-empleados-jwt.ps1` para obtener un token fresco
3. Copia el token completo
4. Pega el token en Swagger UI (botón Authorize)
5. Prueba los endpoints en orden:
   - GET /empleados (listar)
   - GET /empleados/{id} (obtener detalle)
   - POST /empleados (crear nuevo)
   - PUT /empleados/{id} (actualizar)
   - DELETE /empleados/{id} (eliminar)

---

## 🔗 URLs de Swagger UI

- **Empleados Service**: http://localhost:8080/swagger-ui.html
- **Auth Service**: http://localhost:8085/swagger-ui.html
- **Departamentos Service**: http://localhost:8081/swagger-ui.html

---

**✅ Todo está configurado correctamente para usar JWT con Swagger!**
