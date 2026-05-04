# Script de prueba para auth-service
Write-Host "=== Probando Auth-Service ===" -ForegroundColor Cyan

# Test 1: Health Check
Write-Host "`n1. Verificando health del servicio..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8085/actuator/health" -Method GET -UseBasicParsing
    Write-Host "Health Status: $($response.Content)" -ForegroundColor Green
} catch {
    Write-Host "Error en health check: $_" -ForegroundColor Red
}

# Test 2: Swagger UI disponible
Write-Host "`n2. Verificando Swagger UI..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "http://localhost:8085/swagger-ui.html" -Method GET -UseBasicParsing
    Write-Host "Swagger UI disponible: http://localhost:8085/swagger-ui.html" -ForegroundColor Green
} catch {
    Write-Host "Error accediendo a Swagger: $_" -ForegroundColor Red
}

Write-Host "`n=== Instrucciones para probar login ===" -ForegroundColor Cyan
Write-Host "1. Abre tu navegador en: http://localhost:8085/swagger-ui.html" -ForegroundColor White
Write-Host "2. Busca el endpoint: POST /auth/login" -ForegroundColor White
Write-Host "3. Usa este body:" -ForegroundColor White
Write-Host @"
{
  "username": "admin",
  "password": "password123"
}
"@ -ForegroundColor Yellow
Write-Host "4. Copia el token JWT que te devuelva" -ForegroundColor White
Write-Host "5. Prueba el endpoint: GET /auth/validate?token=<TU_TOKEN>" -ForegroundColor White
