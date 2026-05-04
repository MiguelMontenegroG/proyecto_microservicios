# Test de Recuperacion de Contrasena con Emails Existentes
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  TEST RECUPERACION DE CONTRASENA" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$AUTH_SERVICE_URL = "http://localhost:8085"

# Lista de emails de prueba (de tu base de datos)
$emailsPrueba = @(
    "patricia@empresa.com",
    "ricardo@empresa.com",
    "juan.perez@empresa.com",
    "a@empresa.com",
    "prueba@empresa.com",
    "prueba1@empresa.com",
    "flujo.test@empresa.com",
    "prueba.token@empresa.com",
    "token.final@empresa.com",
    "email.inexistente@empresa.com"
)

Write-Host "Probando $($emailsPrueba.Count) emails..." -ForegroundColor Yellow
Write-Host ""

$resultados = @()

foreach ($email in $emailsPrueba) {
    Write-Host "----------------------------------------" -ForegroundColor Gray
    Write-Host "Email: $email" -ForegroundColor White
    
    $request = @{
        email = $email
    } | ConvertTo-Json
    
    try {
        $response = Invoke-RestMethod -Uri "$AUTH_SERVICE_URL/auth/forgot-password" `
                                      -Method Post `
                                      -Body $request `
                                      -ContentType "application/json"
        
        if ($response.existe) {
            Write-Host "  OK - Email ENCONTRADO" -ForegroundColor Green
            Write-Host "  Token: $($response.resetToken)" -ForegroundColor Cyan
            Write-Host "  Mensaje: $($response.message)" -ForegroundColor Gray
            
            $resultados += [PSCustomObject]@{
                Email = $email
                Existe = $true
                Token = $response.resetToken
            }
        } else {
            Write-Host "  ERROR - Email NO encontrado" -ForegroundColor Red
            Write-Host "  Motivo: $($response.motivo)" -ForegroundColor Yellow
            Write-Host "  Mensaje: $($response.message)" -ForegroundColor Gray
            
            $resultados += [PSCustomObject]@{
                Email = $email
                Existe = $false
                Token = $null
            }
        }
        
    } catch {
        Write-Host "  ERROR: $_" -ForegroundColor Red
        
        $resultados += [PSCustomObject]@{
            Email = $email
            Existe = "ERROR"
            Token = $null
        }
    }
    
    Write-Host ""
}

# RESUMEN FINAL
Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  RESUMEN DE RESULTADOS" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""

$encontrados = $resultados | Where-Object { $_.Existe -eq $true }
$noEncontrados = $resultados | Where-Object { $_.Existe -eq $false }
$errors = $resultados | Where-Object { $_.Existe -eq "ERROR" }

Write-Host "Total probados: $($emailsPrueba.Count)" -ForegroundColor White
Write-Host "OK - Encontrados: $($encontrados.Count)" -ForegroundColor Green
Write-Host "ERROR - No encontrados: $($noEncontrados.Count)" -ForegroundColor Red
if ($errors.Count -gt 0) {
    Write-Host "WARNING - Errores: $($errors.Count)" -ForegroundColor Yellow
}
Write-Host ""

if ($encontrados.Count -gt 0) {
    Write-Host "Emails validos con tokens:" -ForegroundColor Green
    foreach ($r in $encontrados) {
        Write-Host "  - $($r.Email)" -ForegroundColor White
        Write-Host "    Token: $($r.Token)" -ForegroundColor Cyan
    }
    Write-Host ""
}

if ($noEncontrados.Count -gt 0) {
    Write-Host "Emails no registrados:" -ForegroundColor Red
    foreach ($r in $noEncontrados) {
        Write-Host "  - $($r.Email)" -ForegroundColor Gray
    }
    Write-Host ""
}

Write-Host "============================================" -ForegroundColor Cyan
Write-Host "  PROXIMOS PASOS" -ForegroundColor Cyan
Write-Host "============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "Para probar el flujo completo con un token valido:" -ForegroundColor Yellow
Write-Host ""
Write-Host "1. Copia uno de los tokens de arriba" -ForegroundColor White
Write-Host "2. Usa el endpoint POST /auth/reset-password" -ForegroundColor White
Write-Host "3. Body:" -ForegroundColor White
$jsonExample = @"
{
  `"token`": `"PEGA_AQUI_EL_TOKEN`",
  `"newPassword`": `"NuevaPassword123!`"
}
"@
Write-Host $jsonExample -ForegroundColor Gray
Write-Host ""
Write-Host "4. Luego prueba login con la nueva contrasena" -ForegroundColor White
Write-Host ""
