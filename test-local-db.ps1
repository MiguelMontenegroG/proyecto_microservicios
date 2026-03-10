# Script para pruebas locales en IntelliJ
# Asegúrate de que los servicios estén corriendo en IntelliJ antes de ejecutar

Write-Host "🧪 PRUEBAS LOCALES EN INTELLIJ" -ForegroundColor Cyan
Write-Host "=============================" -ForegroundColor Cyan
Write-Host ""

# Verificar que los puertos estén ocupados (servicios corriendo)
Write-Host "🔍 Verificando puertos..." -ForegroundColor Yellow

$port_8080 = Test-NetConnection -ComputerName localhost -Port 8080 -WarningAction SilentlyContinue
$port_8081 = Test-NetConnection -ComputerName localhost -Port 8081 -WarningAction SilentlyContinue

if ($port_8080.TcpTestSucceeded) {
    Write-Host "✅ Puerto 8080 (empleados-service) está ocupado - servicio probablemente corriendo" -ForegroundColor Green
} else {
    Write-Host "⚠️  Puerto 8080 no está ocupado - ¿Está corriendo empleados-service?" -ForegroundColor Yellow
}

if ($port_8081.TcpTestSucceeded) {
    Write-Host "✅ Puerto 8081 (departamentos-service) está ocupado - servicio probablemente corriendo" -ForegroundColor Green
} else {
    Write-Host "⚠️  Puerto 8081 no está ocupado - ¿Está corriendo departamentos-service?" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🚀 INICIANDO PRUEBAS..." -ForegroundColor Cyan
Write-Host ""

# Función para probar endpoint
function Test-LocalEndpoint {
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
        Write-Host "   Respuesta: $($response | ConvertTo-Json -Depth 2)" -ForegroundColor Gray
        return $true
    } catch {
        Write-Host "❌ Error: $($_.Exception.Message)" -ForegroundColor Red
        if ($_.Exception.Response) {
            Write-Host "   Código de estado: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
        }
        return $false
    }
    Write-Host ""
}

# Pruebas para Empleados Service
Write-Host "💼 PRUEBAS - EMPLEADOS SERVICE (Puerto 8080)" -ForegroundColor Blue
Write-Host "----------------------------------------" -ForegroundColor Blue

if ($port_8080.TcpTestSucceeded) {
    $tests_empleados = @(
        @{ Url = "http://localhost:8080/test-db/ping"; Description = "Ping básico"; Method = "GET" },
        @{ Url = "http://localhost:8080/test-db/connection-status"; Description = "Estado de conexión"; Method = "GET" },
        @{ Url = "http://localhost:8080/test-db/collections-info"; Description = "Información de colecciones"; Method = "GET" }
    )

    $empleados_success = 0
    foreach ($test in $tests_empleados) {
        if (Test-LocalEndpoint -Url $test.Url -Description $test.Description -Method $test.Method) {
            $empleados_success++
        }
    }

    # Prueba de inserción para empleados
    Write-Host "🧪 Probando: Inserción de documento de prueba (empleados)" -ForegroundColor Yellow
    $insert_body = @{
        testName = "Prueba Local IntelliJ"
        timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
        environment = "IntelliJ-local"
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
} else {
    Write-Host "⏭️  Saltando pruebas de empleados - servicio no disponible" -ForegroundColor Yellow
    $empleados_success = 0
    $total_tests_empleados = 0
}

Write-Host ""
Write-Host "🏢 PRUEBAS - DEPARTAMENTOS SERVICE (Puerto 8081)" -ForegroundColor Blue
Write-Host "---------------------------------------------" -ForegroundColor Blue

if ($port_8081.TcpTestSucceeded) {
    $tests_departamentos = @(
        @{ Url = "http://localhost:8081/test-db/ping"; Description = "Ping básico"; Method = "GET" },
        @{ Url = "http://localhost:8081/test-db/connection-status"; Description = "Estado de conexión"; Method = "GET" },
        @{ Url = "http://localhost:8081/test-db/collections-info"; Description = "Información de colecciones"; Method = "GET" },
        @{ Url = "http://localhost:8081/test-db/health-check"; Description = "Health check completo"; Method = "GET" }
    )

    $departamentos_success = 0
    foreach ($test in $tests_departamentos) {
        if (Test-LocalEndpoint -Url $test.Url -Description $test.Description -Method $test.Method) {
            $departamentos_success++
        }
    }

    # Prueba de inserción para departamentos
    Write-Host "🧪 Probando: Inserción de documento de prueba (departamentos)" -ForegroundColor Yellow
    $insert_body_dept = @{
        testName = "Prueba Local IntelliJ Departamentos"
        timestamp = (Get-Date).ToString("yyyy-MM-dd HH:mm:ss")
        environment = "IntelliJ-local"
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
} else {
    Write-Host "⏭️  Saltando pruebas de departamentos - servicio no disponible" -ForegroundColor Yellow
    $departamentos_success = 0
    $total_tests_departamentos = 0
}

# Resultados finales
Write-Host ""
Write-Host "📊 RESULTADOS FINALES" -ForegroundColor Cyan
Write-Host "===================" -ForegroundColor Cyan
Write-Host ""

$total_tests_empleados = 5
$total_tests_departamentos = 6

if ($port_8080.TcpTestSucceeded) {
    Write-Host "💼 Empleados Service:" -ForegroundColor Blue
    Write-Host "   Tests exitosos: $empleados_success/$total_tests_empleados" -ForegroundColor White
    if ($empleados_success -eq $total_tests_empleados) {
        Write-Host "   ✅ Todos los tests PASARON" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Algunos tests FALLARON" -ForegroundColor Red
    }
} else {
    Write-Host "💼 Empleados Service: NO DISPONIBLE" -ForegroundColor Red
}

Write-Host ""

if ($port_8081.TcpTestSucceeded) {
    Write-Host "🏢 Departamentos Service:" -ForegroundColor Blue
    Write-Host "   Tests exitosos: $departamentos_success/$total_tests_departamentos" -ForegroundColor White
    if ($departamentos_success -eq $total_tests_departamentos) {
        Write-Host "   ✅ Todos los tests PASARON" -ForegroundColor Green
    } else {
        Write-Host "   ❌ Algunos tests FALLARON" -ForegroundColor Red
    }
} else {
    Write-Host "🏢 Departamentos Service: NO DISPONIBLE" -ForegroundColor Red
}

Write-Host ""
$total_success = $empleados_success + $departamentos_success
$total_possible = ($port_8080.TcpTestSucceeded ? $total_tests_empleados : 0) + ($port_8081.TcpTestSucceeded ? $total_tests_departamentos : 0)

if ($total_possible -gt 0) {
    Write-Host "📈 RESUMEN GENERAL:" -ForegroundColor Cyan
    Write-Host "   Total tests exitosos: $total_success/$total_possible" -ForegroundColor White

    if ($total_success -eq $total_possible) {
        Write-Host "   🎉 ¡TODAS LAS PRUEBAS DISPONIBLES PASARON!" -ForegroundColor Green
        Write-Host "   ✅ Las bases de datos están conectadas correctamente en IntelliJ." -ForegroundColor Green
    } else {
        Write-Host "   ⚠️  Algunas pruebas fallaron. Revisa los errores mostrados arriba." -ForegroundColor Yellow
        Write-Host "   💡 Consulta LOCAL_TEST_COMMANDS.md para más detalles." -ForegroundColor Gray
    }
} else {
    Write-Host "⚠️  Ningún servicio está disponible para pruebas." -ForegroundColor Yellow
    Write-Host "💡 Asegúrate de ejecutar los servicios en IntelliJ primero." -ForegroundColor Gray
}

Write-Host ""
Write-Host "🔧 Comandos útiles en IntelliJ:" -ForegroundColor Cyan
Write-Host "   - Verifica que los servicios estén corriendo en puertos 8080/8081" -ForegroundColor Gray
Write-Host "   - Revisa la consola de salida para mensajes de conexión MongoDB" -ForegroundColor Gray
Write-Host "   - Configura las variables de entorno en Run/Debug Configurations" -ForegroundColor Gray
Write-Host "   - Usa el modo Debug para inspeccionar variables" -ForegroundColor Gray