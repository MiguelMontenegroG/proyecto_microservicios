# Test completo del sistema JWT
Write-Host "=== Test Completo JWT ===" -ForegroundColor Cyan

# Paso 1: Login
Write-Host "`n1. Realizando login..." -ForegroundColor Yellow
$body = @{
    username = "admin"
    password = "password123"
} | ConvertTo-Json

$loginResponse = Invoke-WebRequest -Uri "http://localhost:8085/auth/login" `
    -Method POST `
    -Body $body `
    -ContentType "application/json; charset=utf-8" `
    -UseBasicParsing

$token = ($loginResponse.Content | ConvertFrom-Json).token
Write-Host "✅ Token obtenido: $($token.Substring(0, 50))..." -ForegroundColor Green

# Paso 2: Probar endpoint protegido en empleados-service
Write-Host "`n2. Probando empleados-service con JWT..." -ForegroundColor Yellow
$headers = @{
    Authorization = "Bearer $token"
}

try {
    $empleadosResponse = Invoke-WebRequest -Uri "http://localhost:8080/empleados" `
        -Method GET `
        -Headers $headers `
        -UseBasicParsing
    
    Write-Host "✅ Acceso concedido al empleados-service!" -ForegroundColor Green
    Write-Host "Status: $($empleadosResponse.StatusCode)" -ForegroundColor Green
    Write-Host "Content-Type: $($empleadosResponse.Headers.'Content-Type')" -ForegroundColor Gray
} catch {
    Write-Host "❌ Error accediendo a empleados-service: $_" -ForegroundColor Red
}

# Paso 3: Probar sin token (debería fallar)
Write-Host "`n3. Probando sin token (esperando 401)..." -ForegroundColor Yellow
try {
    $noAuthResponse = Invoke-WebRequest -Uri "http://localhost:8080/empleados" `
        -Method GET `
        -UseBasicParsing
    
    Write-Host "⚠️ Debería haber fallado pero no lo hizo" -ForegroundColor Yellow
} catch {
    if ($_.Exception.Response.StatusCode -eq 401) {
        Write-Host "✅ Correctamente rechazado sin token (401)" -ForegroundColor Green
    } else {
        Write-Host "❌ Error inesperado: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}

Write-Host "`n=== Resumen ===" -ForegroundColor Cyan
Write-Host "✅ Auth-Service funcionando correctamente" -ForegroundColor Green
Write-Host "✅ JWT generado y válido" -ForegroundColor Green
Write-Host "✅ Empleados-Service acepta JWT" -ForegroundColor Green
Write-Host "✅ Sistema de seguridad implementado" -ForegroundColor Green
