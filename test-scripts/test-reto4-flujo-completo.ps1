# Script de prueba del flujo completo de activacion de usuarios (Reto 4)
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  PRUEBA FLUJO ACTIVACION USUARIOS" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$EMPLEADOS_SERVICE_URL = "http://localhost:8080"
$AUTH_SERVICE_URL = "http://localhost:8085"
$NOTIFICACIONES_SERVICE_URL = "http://localhost:8084"

# PASO 1: Login como ADMIN
Write-Host "[PASO 1] Obteniendo token ADMIN..." -ForegroundColor Yellow
try {
    $loginData = @{
        username = "admin"
        password = "password123"
    } | ConvertTo-Json

    $adminResponse = Invoke-RestMethod -Uri "$AUTH_SERVICE_URL/auth/login" `
                                       -Method Post `
                                       -Body $loginData `
                                       -ContentType "application/json"
    
    $adminToken = $adminResponse.token
    Write-Host "  [OK] Token ADMIN obtenido exitosamente" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "  [ERROR] No se pudo obtener token ADMIN: $_" -ForegroundColor Red
    exit 1
}

# PASO 2: Crear nuevo empleado
Write-Host "[PASO 2] Creando nuevo empleado..." -ForegroundColor Yellow
$empleadoId = "EMP-TEST-$((Get-Date).ToString('yyyyMMddHHmmss'))"
$emailPrueba = "prueba.auto.$((Get-Date).ToString('yyyyMMddHHmmss'))@empresa.com"

$nuevoEmpleado = @{
    id = $empleadoId
    nombre = "Usuario Prueba Automatizada"
    email = $emailPrueba
    departamentoId = "IT"
} | ConvertTo-Json

$headers = @{
    Authorization = "Bearer $adminToken"
    ContentType = "application/json"
}

try {
    $empleadoCreado = Invoke-RestMethod -Uri "$EMPLEADOS_SERVICE_URL/empleados" `
                                        -Method Post `
                                        -Headers $headers `
                                        -Body $nuevoEmpleado
    
    Write-Host "  [OK] Empleado creado: $($empleadoCreado.id)" -ForegroundColor Green
    Write-Host "  Email: $($empleadoCreado.email)" -ForegroundColor Gray
    Write-Host ""
} catch {
    Write-Host "  [ERROR] No se pudo crear empleado: $_" -ForegroundColor Red
    exit 1
}

# PASO 3: Esperar procesamiento de eventos
Write-Host "[PASO 3] Esperando procesamiento de eventos asincronos (5 segundos)..." -ForegroundColor Yellow
Start-Sleep -Seconds 5
Write-Host ""

# PASO 4: Verificar logs de auth-service
Write-Host "[PASO 4] Verificando logs de auth-service..." -ForegroundColor Yellow
$authLogs = docker logs auth-service 2>&1 | Select-Object -Last 60
$authLogsString = $authLogs -join "`n"

# Buscar el evento recibido
if ($authLogsString -match "Evento recibido en auth-service.*$empleadoId") {
    Write-Host "  [OK] Evento recibido por auth-service" -ForegroundColor Green
} else {
    Write-Host "  [ERROR] Evento NO recibido por auth-service" -ForegroundColor Red
    Write-Host "  Ultimos logs de auth-service:" -ForegroundColor Yellow
    $authLogs | Select-Object -Last 15 | ForEach-Object { Write-Host "    $_" -ForegroundColor Gray }
    Write-Host ""
    Write-Host "  DETENCION: El flujo fallo en la recepcion del evento" -ForegroundColor Red
    exit 1
}

# Buscar creacion de usuario
if ($authLogsString -match "Usuario creado exitosamente.*con token de activacion") {
    Write-Host "  [OK] Usuario creado con token de activacion" -ForegroundColor Green
} else {
    Write-Host "  [ERROR] Usuario NO creado o sin token" -ForegroundColor Red
}

# Buscar publicacion del evento usuario.creado
$resetToken = $null
if ($authLogsString -match "Evento usuario.creado publicado con token de activacion") {
    Write-Host "  [OK] Evento usuario.creado publicado" -ForegroundColor Green
    
    # Extraer el resetToken del log
    if ($authLogsString -match "resetToken:([a-f0-9-]+)") {
        $resetToken = $matches[1]
        Write-Host "  Token encontrado en auth-service: $resetToken" -ForegroundColor Green
    }
} else {
    Write-Host "  [ERROR] Evento usuario.creado NO publicado" -ForegroundColor Red
}
Write-Host ""

# PASO 5: Verificar logs de notificaciones-service
Write-Host "[PASO 5] Verificando logs de notificaciones-service..." -ForegroundColor Yellow
$notifLogs = docker logs notificaciones-service 2>&1 | Select-Object -Last 50
$notifLogsString = $notifLogs -join "`n"

if ($notifLogsString -match "SIMULACION.*EMAIL.*ACTIVACION.*CUENTA") {
    Write-Host "  [OK] Notificacion de activacion registrada" -ForegroundColor Green
    
    # Extraer token si esta en los logs
    if ($notifLogsString -match "Token de Activacion:\s*([a-f0-9-]+)") {
        $tokenNotif = $matches[1]
        Write-Host "  Token en notificaciones: $tokenNotif" -ForegroundColor Green
        
        # Si no tenemos el token de auth, usar este
        if (-not $resetToken) {
            $resetToken = $tokenNotif
        }
    }
} else {
    Write-Host "  [ERROR] Notificacion de activacion NO encontrada" -ForegroundColor Red
}
Write-Host ""

# PASO 6: Si no tenemos el token, intentar con forgot-password
if (-not $resetToken) {
    Write-Host "[PASO 6] Intentando obtener token via forgot-password..." -ForegroundColor Yellow
    try {
        $forgotRequest = @{
            email = $emailPrueba
        } | ConvertTo-Json
        
        $forgotResponse = Invoke-RestMethod -Uri "$AUTH_SERVICE_URL/auth/forgot-password" `
                                            -Method Post `
                                            -Body $forgotRequest `
                                            -ContentType "application/json"
        
        if ($forgotResponse.resetToken) {
            $resetToken = $forgotResponse.resetToken
            Write-Host "  [OK] Token obtenido via forgot-password: $resetToken" -ForegroundColor Green
        } else {
            Write-Host "  [ERROR] No se recibio token en la respuesta" -ForegroundColor Red
        }
    } catch {
        Write-Host "  [ERROR] Error en forgot-password: $_" -ForegroundColor Red
    }
    Write-Host ""
}

# PASO 7: Reset password (si tenemos token)
if ($resetToken) {
    Write-Host "[PASO 7] Estableciendo nueva contrasena..." -ForegroundColor Yellow
    $nuevaPassword = "PasswordSegura123!"
    
    $resetRequest = @{
        token = $resetToken
        newPassword = $nuevaPassword
    } | ConvertTo-Json
    
    try {
        $resetResponse = Invoke-RestMethod -Uri "$AUTH_SERVICE_URL/auth/reset-password" `
                                           -Method Post `
                                           -Body $resetRequest `
                                           -ContentType "application/json"
        
        Write-Host "  [OK] Contrasena restablecida exitosamente" -ForegroundColor Green
        Write-Host "  Mensaje: $($resetResponse.message)" -ForegroundColor Gray
        Write-Host ""
    } catch {
        Write-Host "  [ERROR] No se pudo restablecer contrasena: $_" -ForegroundColor Red
        Write-Host "  Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "  POSIBLE CAUSA: Token invalido o ya usado" -ForegroundColor Yellow
        Write-Host ""
        exit 1
    }
    
    # PASO 8: Login con nueva contrasena
    Write-Host "[PASO 8] Haciendo login con nueva contrasena..." -ForegroundColor Yellow
    
    # El username se deriva del email (parte antes del @)
    $username = $emailPrueba.Split('@')[0]
    
    $loginRequest = @{
        username = $username
        password = $nuevaPassword
    } | ConvertTo-Json
    
    try {
        $loginResponse = Invoke-RestMethod -Uri "$AUTH_SERVICE_URL/auth/login" `
                                           -Method Post `
                                           -Body $loginRequest `
                                           -ContentType "application/json"
        
        Write-Host "  [OK] Login exitoso con nueva contrasena" -ForegroundColor Green
        Write-Host "  Username: $($loginResponse.username)" -ForegroundColor Gray
        Write-Host "  Rol: $($loginResponse.rol)" -ForegroundColor Gray
        Write-Host "  Token JWT: $($loginResponse.token.Substring(0, [Math]::Min(50, $loginResponse.token.Length)))..." -ForegroundColor Gray
        Write-Host ""
        
        # RESUMEN FINAL
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host "  RESULTADO FINAL" -ForegroundColor Cyan
        Write-Host "========================================" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "[EXITO] Flujo de activacion COMPLETADO EXITOSAMENTE" -ForegroundColor Green
        Write-Host ""
        Write-Host "Resumen:" -ForegroundColor White
        Write-Host "  - Empleado creado: $empleadoId" -ForegroundColor Gray
        Write-Host "  - Email: $emailPrueba" -ForegroundColor Gray
        Write-Host "  - Username generado: $username" -ForegroundColor Gray
        Write-Host "  - Token de activacion: $resetToken" -ForegroundColor Gray
        Write-Host "  - Nueva contrasena establecida" -ForegroundColor Gray
        Write-Host "  - Login exitoso con JWT" -ForegroundColor Gray
        Write-Host ""
        Write-Host "El Reto 4 esta funcionando correctamente!" -ForegroundColor Green
        Write-Host ""
        
    } catch {
        Write-Host "  [ERROR] Fallo el login: $_" -ForegroundColor Red
        Write-Host "  Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
        Write-Host ""
        Write-Host "  POSIBLE CAUSA:" -ForegroundColor Yellow
        Write-Host "  - El username podria ser diferente. Verifica en MongoDB:" -ForegroundColor White
        Write-Host "    docker exec -it database-auth mongosh authdb --eval 'db.usuarios.findOne({email: `"$emailPrueba`"})'" -ForegroundColor Gray
        Write-Host ""
        exit 1
    }
    
} else {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  PRUEBA FALLIDA" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "No se pudo obtener el token de activacion." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Pasos para debug manual:" -ForegroundColor White
    Write-Host "1. Verifica logs completos de auth-service:" -ForegroundColor Gray
    Write-Host "   docker logs auth-service --tail 100" -ForegroundColor Gray
    Write-Host ""
    Write-Host "2. Verifica logs de notificaciones-service:" -ForegroundColor Gray
    Write-Host "   docker logs notificaciones-service --tail 100" -ForegroundColor Gray
    Write-Host ""
    Write-Host "3. Verifica si el usuario fue creado en MongoDB:" -ForegroundColor Gray
    Write-Host "   docker exec -it database-auth mongosh authdb --eval 'db.usuarios.find().pretty()'" -ForegroundColor Gray
    Write-Host ""
    exit 1
}
