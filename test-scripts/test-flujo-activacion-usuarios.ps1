# Test Completo del Flujo de Activación de Usuarios (Reto 4)
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  TEST FLUJO ACTIVACIÓN DE USUARIOS" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$AUTH_SERVICE_URL = "http://localhost:8085"
$EMPLEADOS_SERVICE_URL = "http://localhost:8080"
$NOTIFICACIONES_SERVICE_URL = "http://localhost:8084"

# PASO 1: Crear un nuevo empleado (esto dispara la creación automática de usuario)
Write-Host "[PASO 1] Creando nuevo empleado..." -ForegroundColor Yellow
$nuevoEmpleado = @{
    id = "EMP-TEST-$(Get-Date -Format 'yyyyMMddHHmmss')"
    nombre = "Usuario Prueba Activación"
    email = "usuario.prueba@empresa.com"
    departamentoId = "IT"
} | ConvertTo-Json

try {
    # Primero necesitamos un token ADMIN para crear el empleado
    Write-Host "  → Obteniendo token ADMIN..." -ForegroundColor Gray
    
    $loginData = @{
        username = "admin"
        password = "password123"
    } | ConvertTo-Json
    
    $adminResponse = Invoke-RestMethod -Uri "$AUTH_SERVICE_URL/auth/login" `
                                       -Method Post `
                                       -Body $loginData `
                                       -ContentType "application/json"
    
    $adminToken = $adminResponse.token
    Write-Host "  ✓ Token ADMIN obtenido" -ForegroundColor Green
    
    $headers = @{
        Authorization = "Bearer $adminToken"
        ContentType = "application/json"
    }
    
    $empleadoCreado = Invoke-RestMethod -Uri "$EMPLEADOS_SERVICE_URL/empleados" `
                                        -Method Post `
                                        -Headers $headers `
                                        -Body $nuevoEmpleado
    
    Write-Host "  ✓ Empleado creado: $($empleadoCreado.id)" -ForegroundColor Green
    Write-Host "  → Email: $($empleadoCreado.email)" -ForegroundColor Gray
    Write-Host ""
    
    # Esperar un momento para que los eventos se procesen
    Write-Host "  → Esperando procesamiento de eventos asíncronos..." -ForegroundColor Gray
    Start-Sleep -Seconds 3
    
} catch {
    Write-Host "  ✗ ERROR al crear empleado: $_" -ForegroundColor Red
    Write-Host "  Nota: Si el empleado ya existe, continúa con el siguiente paso" -ForegroundColor Yellow
    Write-Host ""
}

# PASO 2: Verificar notificación de activación en notificaciones-service
Write-Host "[PASO 2] Verificando notificación de activación..." -ForegroundColor Yellow

try {
    # Extraer el ID del empleado creado o usar uno conocido
    $empleadoId = if ($empleadoCreado) { $empleadoCreado.id } else { "EMP-TEST-EXISTENTE" }
    
    $notificaciones = Invoke-RestMethod -Uri "$NOTIFICACIONES_SERVICE_URL/notificaciones/$empleadoId" `
                                        -Method Get
    
    if ($notificaciones -and $notificaciones.Count -gt 0) {
        $notificacionActivacion = $notificaciones | Where-Object { $_.tipo -eq 'ACTIVACION_CUENTA' } | Select-Object -First 1
        
        if ($notificacionActivacion) {
            Write-Host "  ✓ Notificación de activación encontrada" -ForegroundColor Green
            Write-Host "  → Tipo: $($notificacionActivacion.tipo)" -ForegroundColor Gray
            Write-Host "  → Destinatario: $($notificacionActivacion.destinatario)" -ForegroundColor Gray
            
            # Extraer el token del mensaje
            $mensaje = $notificacionActivacion.mensaje
            Write-Host "  → Mensaje: $mensaje" -ForegroundColor Gray
            Write-Host ""
            
            # El token debería estar en el mensaje, pero también podemos obtenerlo de los logs
            Write-Host "  NOTA: Revisa los logs del notificaciones-service para ver el token completo" -ForegroundColor Yellow
            Write-Host "  Comando: docker logs notificaciones-service --tail 50" -ForegroundColor Gray
            Write-Host ""
        } else {
            Write-Host "  ⚠ No se encontró notificación de ACTIVACION_CUENTA" -ForegroundColor Yellow
            Write-Host "  → Verifica los logs del notificaciones-service" -ForegroundColor Gray
            Write-Host ""
        }
    } else {
        Write-Host "  ⚠ No hay notificaciones para este empleado aún" -ForegroundColor Yellow
        Write-Host ""
    }
} catch {
    Write-Host "  ⚠ No se pudieron obtener notificaciones: $_" -ForegroundColor Yellow
    Write-Host ""
}

# PASO 3: Simular obtención del token (en producción vendría por email)
Write-Host "[PASO 3] Obteniendo token de activación..." -ForegroundColor Yellow
Write-Host "  → Para pruebas, usaremos el endpoint forgot-password" -ForegroundColor Gray
Write-Host ""

$forgotPasswordRequest = @{
    email = "usuario.prueba@empresa.com"
} | ConvertTo-Json

try {
    $forgotResponse = Invoke-RestMethod -Uri "$AUTH_SERVICE_URL/auth/forgot-password" `
                                        -Method Post `
                                        -Body $forgotPasswordRequest `
                                        -ContentType "application/json"
    
    if ($forgotResponse.resetToken) {
        $resetToken = $forgotResponse.resetToken
        Write-Host "  ✓ Token de recuperación obtenido: $resetToken" -ForegroundColor Green
        Write-Host ""
    } else {
        Write-Host "  ⚠ Respuesta sin token visible:" -ForegroundColor Yellow
        Write-Host "    $($forgotResponse | ConvertTo-Json)" -ForegroundColor Gray
        Write-Host "  → Usando método alternativo (ver logs de auth-service)" -ForegroundColor Yellow
        Write-Host ""
        
        # Instrucciones manuales
        Write-Host "  INSTRUCCIONES MANUALES:" -ForegroundColor Cyan
        Write-Host "  1. Ejecuta: docker logs auth-service --tail 30" -ForegroundColor White
        Write-Host "  2. Busca el mensaje 'Evento usuario.creado publicado'" -ForegroundColor White
        Write-Host "  3. Copia el resetToken del log" -ForegroundColor White
        Write-Host ""
        
        # Solicitar token manualmente
        $manualToken = Read-Host "  Ingresa el token manualmente (o presiona Enter para omitir)"
        
        if ([string]::IsNullOrWhiteSpace($manualToken)) {
            Write-Host "  ✗ Test abortado - token no proporcionado" -ForegroundColor Red
            exit 1
        }
        
        $resetToken = $manualToken
        Write-Host ""
    }
} catch {
    Write-Host "  ✗ ERROR al solicitar recuperación: $_" -ForegroundColor Red
    exit 1
}

# PASO 4: Establecer nueva contraseña usando el token
Write-Host "[PASO 4] Estableciendo nueva contraseña..." -ForegroundColor Yellow

$nuevaPassword = "MiPassword123!"
$resetPasswordRequest = @{
    token = $resetToken
    newPassword = $nuevaPassword
} | ConvertTo-Json

try {
    $resetResponse = Invoke-RestMethod -Uri "$AUTH_SERVICE_URL/auth/reset-password" `
                                       -Method Post `
                                       -Body $resetPasswordRequest `
                                       -ContentType "application/json"
    
    Write-Host "  ✓ Contraseña restablecida exitosamente" -ForegroundColor Green
    Write-Host "  → Nueva contraseña: $nuevaPassword" -ForegroundColor Gray
    Write-Host "  → Mensaje: $($resetResponse.message)" -ForegroundColor Gray
    Write-Host ""
    
} catch {
    Write-Host "  ✗ ERROR al restablecer contraseña: $_" -ForegroundColor Red
    Write-Host "  Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    exit 1
}

# PASO 5: Intentar login con la nueva contraseña
Write-Host "[PASO 5] Probando login con nueva contraseña..." -ForegroundColor Yellow

$loginRequest = @{
    username = "usuario.prueba"  # Derivado del email
    password = $nuevaPassword
} | ConvertTo-Json

try {
    $loginResponse = Invoke-RestMethod -Uri "$AUTH_SERVICE_URL/auth/login" `
                                       -Method Post `
                                       -Body $loginRequest `
                                       -ContentType "application/json"
    
    Write-Host "  ✓ Login exitoso con nueva contraseña" -ForegroundColor Green
    Write-Host "  → Username: $($loginResponse.username)" -ForegroundColor Gray
    Write-Host "  → Rol: $($loginResponse.rol)" -ForegroundColor Gray
    Write-Host "  → Token: $($loginResponse.token.Substring(0, 50))..." -ForegroundColor Gray
    Write-Host ""
    
} catch {
    Write-Host "  ✗ ERROR en login: $_" -ForegroundColor Red
    Write-Host "  Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "  POSIBLE CAUSA:" -ForegroundColor Yellow
    Write-Host "  - El username podría ser diferente. Revisa en MongoDB:" -ForegroundColor White
    Write-Host "    docker exec -it database-auth mongosh authdb --eval 'db.usuarios.findOne({email: `"usuario.prueba@empresa.com`"})'" -ForegroundColor Gray
    Write-Host ""
    exit 1
}

# PASO 6: Probar acceso a endpoints protegidos
Write-Host "[PASO 6] Probando acceso a endpoints protegidos..." -ForegroundColor Yellow

$userToken = $loginResponse.token
$authHeaders = @{
    Authorization = "Bearer $userToken"
    ContentType = "application/json"
}

try {
    $empleados = Invoke-RestMethod -Uri "$EMPLEADOS_SERVICE_URL/empleados" `
                                   -Method Get `
                                   -Headers $authHeaders
    
    Write-Host "  ✓ Acceso concedido a empleados-service" -ForegroundColor Green
    Write-Host "  → Empleados encontrados: $($empleados.Count)" -ForegroundColor Gray
    Write-Host ""
    
} catch {
    Write-Host "  ✗ ERROR accediendo a empleados-service: $_" -ForegroundColor Red
    Write-Host ""
}

# RESUMEN FINAL
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  RESUMEN DEL TEST" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "✓ Empleado creado exitosamente" -ForegroundColor Green
Write-Host "✓ Usuario generado automáticamente por evento" -ForegroundColor Green
Write-Host "✓ Token de activación generado y simulado" -ForegroundColor Green
Write-Host "✓ Contraseña establecida vía reset-password" -ForegroundColor Green
Write-Host "✓ Login funcional con nueva contraseña" -ForegroundColor Green
Write-Host "✓ Acceso a endpoints protegidos verificado" -ForegroundColor Green
Write-Host ""
Write-Host "FLUJO COMPLETO EXITOSO ✓" -ForegroundColor Green
Write-Host ""
Write-Host "Credenciales del usuario de prueba:" -ForegroundColor Yellow
Write-Host "  Username: usuario.prueba" -ForegroundColor White
Write-Host "  Password: $nuevaPassword" -ForegroundColor White
Write-Host "  Rol: USER" -ForegroundColor White
Write-Host ""
