# Test de Login con Auth-Service
Write-Host "=== Probando Login JWT ===" -ForegroundColor Cyan

# Credenciales del usuario admin
$body = @{
    username = "admin"
    password = "password123"
} | ConvertTo-Json

Write-Host "`nEnviando login request..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8085/auth/login" `
        -Method POST `
        -Body $body `
        -ContentType "application/json; charset=utf-8" `
        -UseBasicParsing
    
    Write-Host "`n✅ Login exitoso!" -ForegroundColor Green
    Write-Host "Status: $($response.StatusCode)" -ForegroundColor Green
    
    # Parsear respuesta JSON
    $data = $response.Content | ConvertFrom-Json
    
    Write-Host "`n=== Token JWT Generado ===" -ForegroundColor Cyan
    Write-Host "Token: $($data.token)" -ForegroundColor White
    Write-Host "`nUsername: $($data.username)" -ForegroundColor White
    Write-Host "Rol: $($data.rol)" -ForegroundColor White
    Write-Host "Expires In: $($data.expiresIn) ms" -ForegroundColor White
    
    # Guardar token en variable de entorno para uso posterior
    [Environment]::SetEnvironmentVariable("JWT_TOKEN", $data.token, "Process")
    Write-Host "`n✅ Token guardado en variable de entorno JWT_TOKEN" -ForegroundColor Green
    
    # Probar validación del token
    Write-Host "`n=== Validando Token ===" -ForegroundColor Yellow
    $validateResponse = Invoke-WebRequest -Uri "http://localhost:8085/auth/validate?token=$($data.token)" `
        -Method GET `
        -UseBasicParsing
    
    Write-Host "Validación: $($validateResponse.Content)" -ForegroundColor Green
    
} catch {
    Write-Host "`n❌ Error en login: $_" -ForegroundColor Red
    Write-Host "Error Details: $($_.Exception.Message)" -ForegroundColor Red
}
