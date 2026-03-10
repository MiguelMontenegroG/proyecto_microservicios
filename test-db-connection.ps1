# Script para probar conexiones a bases de datos
# Uso: .\test-db-connection.ps1

Write-Host "🧪 PRUEBAS DE CONEXIÓN A BASES DE DATOS" -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

# Verificar que los servicios estén corriendo
Write-Host "🔍 Verificando estado de servicios..." -ForegroundColor Yellow
try {
    $services = docker-compose ps --format json | ConvertFrom-Json
    if ($services.Count -eq 0) {
        Write-Host "❌ No se encontraron servicios corriendo" -ForegroundColor Red
        Write-Host "💡 Inicia los servicios con: docker-compose up -d" -ForegroundColor Green
        exit 1
    }
    
    $empleados_running = $services | Where-Object { $_.Service -eq "empleados-service" -and $_.State -eq "running" }
    $departamentos_running = $services | Where-Object { $_.Service -eq "departamentos-service" -and $_.State -eq "running" }
    
    if ($empleados_running) {
        Write-Host "✅ empleados-service está corriendo" -ForegroundColor Green
    } else {
        Write-Host "❌ empleados-service no está corriendo" -ForegroundColor Red
    }
    
    if ($departamentos_running) {
        Write-Host "✅ departamentos-service está corriendo" -ForegroundColor Green
    } else {
        Write-Host "❌ departamentos-service no está corriendo" -ForegroundColor Red
    }
    
} catch {
    Write-Host "❌ Error al verificar servicios: $_" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "🚀 INICIANDO PRUEBAS..." -ForegroundColor Cyan
Write-Host ""

# Función para probar endpoint
function Test-Endpoint {
    param(
        [string]$Url,
        [string]$Description,
        [string]$Method = "GET"
    )
    
    Write-Host "🧪 Probando: $Description" -ForegroundColor Yellow
    Write-Host "   URL: $Url" -ForegroundColor Gray
    
    try {
        $response = Invoke-RestMethod -Uri $Url -Method $Method -ErrorAction Stop
        Write-Host "✅ Éxito" -ForegroundColor Green
        Write-Host "   Respuesta: $($response | ConvertTo-Json -Depth 3)" -ForegroundColor Gray
        return $true
    } catch {
        Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
    Write-Host ""
}

# Pruebas para Empleados Service
Write-Host "💼 PRUEBAS - EMPLEADOS SERVICE (Puerto 8080)" -ForegroundColor Blue
Write-Host "----------------------------------------" -ForegroundColor Blue

$tests_empleados = @(
    @{ Url = "http://localhost:8080/test-db/ping"; Description = "Ping básico"; Method = "GET" },
    @{ Url = "http://localhost:8080/test-db/connection-status"; Description = "Estado de conexión"; Method = "GET" },
    @{ Url = "http://localhost:8080/test-db/collections-info"; Description = "Información de colecciones"; Method = "GET" }
)

$empleados_success = 0
foreach ($test in $tests_empleados) {
    if (Test-Endpoint -Url $test.Url -Description $test.Description -Method $test.Method) {
        $empleados_success++
    }
}

# Prueba de inserción para empleados
Write-Host "🧪 Probando: Inserción de documento de prueba (empleados)" -ForegroundColor Yellow
$insert_body = @{
    testName = "Prueba Automatizada Empleados"
    timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    automated = $true
} | ConvertTo-Json

try {
    $insert_response = Invoke-RestMethod -Uri "http://localhost:8080/test-db/test-insert" -Method POST -Body $insert_body -ContentType "application/json" -ErrorAction Stop
    Write-Host "✅ Inserción exitosa" -ForegroundColor Green
    Write-Host "   ID insertado: $($insert_response.insertedId)" -ForegroundColor Gray
    $empleados_success++
    
    # Limpiar datos de prueba
    Write-Host "🧹 Limpiando datos de prueba..." -ForegroundColor Yellow
    $cleanup_response = Invoke-RestMethod -Uri "http://localhost:8080/test-db/cleanup-test-data" -Method DELETE -ErrorAction Stop
    Write-Host "✅ Limpieza completada. Documentos eliminados: $($cleanup_response.deletedCount)" -ForegroundColor Green
    $empleados_success++
    
} catch {
    Write-Host "❌ Error en inserción/limpieza: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host ""
Write-Host "🏢 PRUEBAS - DEPARTAMENTOS SERVICE (Puerto 8081)" -ForegroundColor Blue
Write-Host "---------------------------------------------" -ForegroundColor Blue

$tests_departamentos = @(
    @{ Url = "http://localhost:8081/test-db/ping"; Description = "Ping básico"; Method = "GET" },
    @{ Url = "http://localhost:8081/test-db/connection-status"; Description = "Estado de conexión"; Method = "GET" },
    @{ Url = "http://localhost:8081/test-db/collections-info"; Description = "Información de colecciones"; Method = "GET" },
    @{ Url = "http://localhost:8081/test-db/health-check"; Description = "Health check completo"; Method = "GET" }
)

$departamentos_success = 0
foreach ($test in $tests_departamentos) {
    if (Test-Endpoint -Url $test.Url -Description $test.Description -Method $test.Method) {
        $departamentos_success++
    }
}

# Prueba de inserción para departamentos
Write-Host "🧪 Probando: Inserción de documento de prueba (departamentos)" -ForegroundColor Yellow
$insert_body_dept = @{
    testName = "Prueba Automatizada Departamentos"
    timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
    automated = $true
} | ConvertTo-Json

try {
    $insert_response_dept = Invoke-RestMethod -Uri "http://localhost:8081/test-db/test-insert" -Method POST -Body $insert_body_dept -ContentType "application/json" -ErrorAction Stop
    Write-Host "✅ Inserción exitosa" -ForegroundColor Green
    Write-Host "   ID insertado: $($insert_response_dept.insertedId)" -ForegroundColor Gray
    $departamentos_success++
    
    # Limpiar datos de prueba
    Write-Host "🧹 Limpiando datos de prueba..." -ForegroundColor Yellow
    $cleanup_response_dept = Invoke-RestMethod -Uri "http://localhost:8081/test-db/cleanup-test-data" -Method DELETE -ErrorAction Stop
    Write-Host "✅ Limpieza completada. Documentos eliminados: $($cleanup_response_dept.deletedCount)" -ForegroundColor Green
    $departamentos_success++
    
} catch {
    Write-Host "❌ Error en inserción/limpieza: $($_.Exception.Message)" -ForegroundColor Red
}

# Resultados finales
Write-Host ""
Write-Host "📊 RESULTADOS FINALES" -ForegroundColor Cyan
Write-Host "===================" -ForegroundColor Cyan
Write-Host ""

$total_tests_empleados = 5
$total_tests_departamentos = 6

Write-Host "💼 Empleados Service:" -ForegroundColor Blue
Write-Host "   Tests exitosos: $empleados_success/$total_tests_empleados" -ForegroundColor White
if ($empleados_success -eq $total_tests_empleados) {
    Write-Host "   ✅ Todos los tests PASARON" -ForegroundColor Green
} else {
    Write-Host "   ❌ Algunos tests FALLARON" -ForegroundColor Red
}

Write-Host ""
Write-Host "🏢 Departamentos Service:" -ForegroundColor Blue
Write-Host "   Tests exitosos: $departamentos_success/$total_tests_departamentos" -ForegroundColor White
if ($departamentos_success -eq $total_tests_departamentos) {
    Write-Host "   ✅ Todos los tests PASARON" -ForegroundColor Green
} else {
    Write-Host "   ❌ Algunos tests FALLARON" -ForegroundColor Red
}

Write-Host ""
$total_success = $empleados_success + $departamentos_success
$total_tests = $total_tests_empleados + $total_tests_departamentos

Write-Host "📈 RESUMEN GENERAL:" -ForegroundColor Cyan
Write-Host "   Total tests exitosos: $total_success/$total_tests" -ForegroundColor White

if ($total_success -eq $total_tests) {
    Write-Host "   🎉 ¡TODAS LAS PRUEBAS PASARON! Las bases de datos están conectadas correctamente." -ForegroundColor Green
} else {
    Write-Host "   ⚠️  Algunas pruebas fallaron. Revisa los errores mostrados arriba." -ForegroundColor Yellow
    Write-Host "   💡 Consulta DB_TEST_COMMANDS.md para más detalles sobre solución de problemas." -ForegroundColor Gray
}

Write-Host ""
Write-Host "🔧 Comandos útiles:" -ForegroundColor Cyan
Write-Host "   Ver logs: docker-compose logs <service-name>" -ForegroundColor Gray
Write-Host "   Reiniciar servicios: docker-compose restart" -ForegroundColor Gray
Write-Host "   Ver estado: docker-compose ps" -ForegroundColor Gray