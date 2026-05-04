# Scripts de Prueba - Auth Service & JWT

## 📋 Descripción
Esta carpeta contiene scripts y archivos de utilidad para probar el sistema de autenticación JWT.

## 🔧 Scripts PowerShell

### 1. `test-auth.ps1` - Verificación básica del servicio
Verifica que el auth-service esté corriendo y accesible.

```powershell
powershell -ExecutionPolicy Bypass -File test-scripts/test-auth.ps1
```

**Qué hace:**
- ✅ Verifica health del servicio
- ✅ Confirma Swagger UI disponible
- ✅ Muestra instrucciones básicas

### 2. `test-login.ps1` - Prueba de login JWT ⭐ RECOMENDADO
Realiza login y obtiene un token JWT.

```powershell
powershell -ExecutionPolicy Bypass -File test-scripts/test-login.ps1
```

**Qué hace:**
- ✅ Inicia sesión con admin/password123
- ✅ Obtiene token JWT
- ✅ Guarda token en variable de entorno
- ✅ Valida el token obtenido

### 3. `test-jwt-complete.ps1` - Test completo de seguridad
Prueba todo el flujo de autenticación y autorización.

```powershell
powershell -ExecutionPolicy Bypass -File test-scripts/test-jwt-complete.ps1
```

**Qué hace:**
- ✅ Login y obtención de token
- ✅ Prueba acceso a empleados-service con JWT
- ✅ Verifica que sin token se rechaza (401)
- ✅ Confirma integración entre servicios

## 🗄️ Scripts MongoDB

### Archivos `.js` para crear usuarios manualmente:

- `create-user.js` - Crea usuario admin inicial
- `create-test-users.js` - Crea admin y user con hash BCrypt
- `create-users-correct.js` - Versión corregida de usuarios
- `setup-auth-users.js` - Setup completo con 2 usuarios

**Uso:**
```bash
Get-Content test-scripts/create-test-users.js | docker exec -i database-auth mongosh authdb
```

## 🎯 Credenciales de Prueba

### Usuario ADMIN:
- **Username:** `admin`
- **Password:** `password123`
- **Rol:** ADMIN (acceso total)

### Usuario USER:
- **Username:** `user`
- **Password:** `password123`
- **Rol:** USER (solo lectura)

## 🌐 Probar en Swagger UI

### Paso 1: Acceder al Swagger del Auth-Service
```
http://localhost:8085/swagger-ui.html
```

### Paso 2: Hacer Login
1. Busca el endpoint: **POST /auth/login**
2. Click en "Try it out"
3. Ingresa el body:
```json
{
  "username": "admin",
  "password": "password123"
}
```
4. Click en "Execute"
5. Copia el **token** de la respuesta

### Paso 3: Autorizar en Swagger
1. Click en el botón **"Authorize"** (candado arriba)
2. En "Value" pega: `Bearer <tu-token>`
   - Ejemplo: `Bearer eyJhbGciOiJIUzUxMiJ9...`
3. Click "Authorize"
4. Click "Close"

### Paso 4: Probar endpoints protegidos
Ahora puedes usar cualquier endpoint que requiera autenticación.

## 🔍 Endpoints Disponibles

### Auth-Service (Puerto 8085):
- **POST /auth/login** - Obtener token (público)
- **GET /auth/validate?token=xxx** - Validar token
- **POST /auth/forgot-password** - Recuperar contraseña
- **POST /auth/reset-password** - Restablecer contraseña

### Empleados-Service (Puerto 8080):
- **GET /empleados** - Listar empleados (requiere AUTH)
- **POST /empleados** - Crear empleado (requiere AUTH)
- **PUT /empleados/{id}** - Actualizar empleado (requiere AUTH)
- **DELETE /empleados/{id}** - Eliminar empleado (requiere AUTH)

## 🚀 Flujo Recomendado de Pruebas

### Para pruebas rápidas:
```powershell
# 1. Verificar servicio
.\test-scripts\test-auth.ps1

# 2. Obtener token
.\test-scripts\test-login.ps1

# 3. Probar integración completa
.\test-scripts\test-jwt-complete.ps1
```

### Para pruebas manuales en Swagger:
1. Abrir http://localhost:8085/swagger-ui.html
2. Hacer login y copiar token
3. Ir a http://localhost:8080/swagger-ui.html
4. Autorizar con el token
5. Probar endpoints

## ⚠️ Solución de Problemas

### Error 403 Forbidden en login:
- Verificar que los usuarios estén creados en MongoDB
- Revisar logs: `docker logs auth-service --tail 50`

### Error 401 Unauthorized:
- El token expiró (dura 1 hora)
- Formato incorrecto del Authorization header
- Token no fue copiado completamente

### Servicio no responde:
- Verificar containers: `docker ps`
- Reiniciar auth-service: `docker-compose restart auth-service`
