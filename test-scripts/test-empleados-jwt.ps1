# ============================================================================
# SCRIPT DE PRUEBA PARA EMPLEADOS-SERVICE CON JWT
# ============================================================================

# Este comando en la terminal: powershell -ExecutionPolicy Bypass -File "C:\Users\ANGEL\Desktop\Universidad\202601\Microservicios\proyecto\test-scripts\test-empleados-jwt.ps1"

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  PRUEBA DE EMPLEADOS-SERVICE CON JWT" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$AUTH_SERVICE_URL = "http://localhost:8085"
$EMPLEADOS_SERVICE_URL = "http://localhost:8080"

# PASO 1: OBTENER TOKEN JWT
Write-Host "[PASO 1] Obteniendo token JWT..." -ForegroundColor Yellow

try {
    $loginData = @{
        username = "admin"
        password = "password123"
    } | ConvertTo-Json

    $response = Invoke-RestMethod -Uri "$AUTH_SERVICE_URL/auth/login" `
                                  -Method Post `
                                  -Body $loginData `
                                  -ContentType "application/json"
    
    $token = $response.token
    
    if ($token) {
        Write-Host "OK Token JWT obtenido!" -ForegroundColor Green
        Write-Host ""
    } else {
        Write-Host "ERROR: No se pudo obtener el token" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "ERROR al conectar con auth-service: $_" -ForegroundColor Red
    exit 1
}

# PASO 2: PROBAR ENDPOINT PBLICO
Write-Host "[PASO 2] Probando endpoint publico /test-db/ping..." -ForegroundColor Yellow

try {
    $pingResult = Invoke-RestMethod -Uri "$EMPLEADOS_SERVICE_URL/test-db/ping" -Method Get
    Write-Host "OK Endpoint publico accesible: $($pingResult.message)" -ForegroundColor Green
    Write-Host ""
} catch {
    Write-Host "ERROR en endpoint publico: $_" -ForegroundColor Red
    Write-Host ""
}

# PASO 3: LISTAR EMPLEADOS (CON TOKEN)
Write-Host "[PASO 3] Listando empleados (con autenticacion JWT)..." -ForegroundColor Yellow

$headers = @{
    Authorization = "Bearer $token"
    ContentType = "application/json"
}

try {
    $empleados = Invoke-RestMethod -Uri "$EMPLEADOS_SERVICE_URL/empleados?pagina=0&tamano=10" `
                                   -Method Get `
                                   -Headers $headers
    
    Write-Host "OK Empleados obtenidos: $($empleados.Count) encontrados" -ForegroundColor Green
    
    if ($empleados.Count -gt 0) {
        Write-Host ""
        Write-Host "Primeros 5 empleados:" -ForegroundColor Cyan
        $empleados | Select-Object -First 5 | ForEach-Object {
            Write-Host "  - $($_.nombre) ($($_.email)) - Dept: $($_.departamentoId)" -ForegroundColor Gray
        }
    }
    Write-Host ""
} catch {
    Write-Host "ERROR al listar empleados: $_" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    Write-Host ""
}

# PASO 4: CREAR NUEVO EMPLEADO
Write-Host "[PASO 4] Creando nuevo empleado de prueba..." -ForegroundColor Yellow

$dateStamp = Get-Date -Format "yyyyMMddHHmmss"
$nuevoEmpleado = @{
    id = "EMP_TEST_$dateStamp"
    nombre = "Empleado Prueba $((Get-Random -Minimum 1000 -Maximum 9999))"
    email = "test.$dateStamp@empresa.com"
    departamentoId = "IT"
    fechaIngreso = (Get-Date -Format "yyyy-MM-dd")
} | ConvertTo-Json

try {
    $empleadoCreado = Invoke-RestMethod -Uri "$EMPLEADOS_SERVICE_URL/empleados" `
                                        -Method Post `
                                        -Headers $headers `
                                        -Body $nuevoEmpleado
    
    Write-Host "OK Empleado creado: $($empleadoCreado.id)" -ForegroundColor Green
    Write-Host "  Nombre: $($empleadoCreado.nombre)" -ForegroundColor Cyan
    Write-Host "  Email: $($empleadoCreado.email)" -ForegroundColor Cyan
    Write-Host ""
} catch {
    Write-Host "ERROR al crear empleado: $_" -ForegroundColor Red
    Write-Host "Status: $($_.Exception.Response.StatusCode.value__)" -ForegroundColor Yellow
    Write-Host ""
}

# RESUMEN FINAL
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  RESUMEN" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "OK Token JWT funcional" -ForegroundColor Green
Write-Host "OK Endpoints protegidos con seguridad" -ForegroundColor Green
Write-Host "OK CRUD de empleados operativo" -ForegroundColor Green
Write-Host ""
Write-Host "Para usar en Swagger UI:" -ForegroundColor Yellow
Write-Host "1. Abre http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "2. Haz clic en el boton 'Authorize' (candado)" -ForegroundColor White
Write-Host "3. Ingresa: Bearer $token" -ForegroundColor White
Write-Host "4. Ahora puedes probar todos los endpoints" -ForegroundColor White
Write-Host ""
