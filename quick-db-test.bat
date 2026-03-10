@echo off
title Pruebas de Conexión a Base de Datos
color 0A

echo ========================================
echo    PRUEBAS DE CONEXIÓN A BASES DE DATOS
echo ========================================
echo.

echo Verificando servicios...
docker-compose ps | findstr "running"
echo.

echo ========================================
echo    EMPLEADOS SERVICE - PUERTO 8080
echo ========================================
echo.

echo 1. Ping básico:
curl -s http://localhost:8080/test-db/ping
echo.
echo.

echo 2. Estado de conexión:
curl -s http://localhost:8080/test-db/connection-status
echo.
echo.

echo 3. Información de colecciones:
curl -s http://localhost:8080/test-db/collections-info
echo.
echo.

echo ========================================
echo    DEPARTAMENTOS SERVICE - PUERTO 8081
echo ========================================
echo.

echo 1. Ping básico:
curl -s http://localhost:8081/test-db/ping
echo.
echo.

echo 2. Estado de conexión:
curl -s http://localhost:8081/test-db/connection-status
echo.
echo.

echo 3. Health check:
curl -s http://localhost:8081/test-db/health-check
echo.
echo.

echo ========================================
echo    COMANDOS ÚTILES
echo ========================================
echo.
echo Para ver logs detallados:
echo   docker-compose logs empleados-service
echo   docker-compose logs departamentos-service
echo.
echo Para reiniciar servicios:
echo   docker-compose restart
echo.
echo Para ver estado de servicios:
echo   docker-compose ps
echo.

pause