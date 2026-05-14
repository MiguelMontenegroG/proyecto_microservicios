#!/bin/sh

# Establecer valores por defecto si no estan definidos
# En Docker Compose, cada servicio tiene su propio hostname
AUTH_URL=${AUTH_URL:-http://auth-service:8085}
EMPLEADOS_URL=${EMPLEADOS_URL:-http://empleados-service:8080}
DEPARTAMENTOS_URL=${DEPARTAMENTOS_URL:-http://departamentos-service:8081}
NOTIFICACIONES_URL=${NOTIFICACIONES_URL:-http://notificaciones-service:8084}

# BASE_URL como fallback (usamos auth por compatibilidad)
BASE_URL=${BASE_URL:-$AUTH_URL}

ADMIN_USER=${ADMIN_USER:-admin}
ADMIN_PASSWORD=${ADMIN_PASSWORD:-password123}
REGULAR_USER=${REGULAR_USER:-user}
REGULAR_PASSWORD=${REGULAR_PASSWORD:-password123}

echo "=========================================="
echo "Ejecutando pruebas E2E con configuracion:"
echo "=========================================="
echo "BASE_URL: $BASE_URL"
echo "AUTH_URL: $AUTH_URL"
echo "EMPLEADOS_URL: $EMPLEADOS_URL"
echo "DEPARTAMENTOS_URL: $DEPARTAMENTOS_URL"
echo "NOTIFICACIONES_URL: $NOTIFICACIONES_URL"
echo "ADMIN_USER: $ADMIN_USER"
echo "REGULAR_USER: $REGULAR_USER"
echo "=========================================="

# Esperar a que los servicios esten listos
echo "Esperando a que los servicios esten disponibles..."
for i in $(seq 1 30); do
    AUTH_OK=$(curl -s -o /dev/null -w "%{http_code}" $AUTH_URL/auth/login 2>/dev/null || echo "000")
    EMP_OK=$(curl -s -o /dev/null -w "%{http_code}" $EMPLEADOS_URL/empleados 2>/dev/null || echo "000")

    if [ "$AUTH_OK" != "000" ] && [ "$EMP_OK" != "000" ]; then
        echo "Servicios disponibles! Auth=$AUTH_OK Empleados=$EMP_OK"
        break
    fi
    echo "Esperando servicios... (intento $i/30)"
    sleep 2
done

# Ejecutar pruebas Maven con los parametros
echo "Iniciando pruebas..."
mvn test \
    -DbaseUrl="$BASE_URL" \
    -DauthUrl="$AUTH_URL" \
    -DempleadosUrl="$EMPLEADOS_URL" \
    -DdepartamentosUrl="$DEPARTAMENTOS_URL" \
    -DnotificacionesUrl="$NOTIFICACIONES_URL" \
    -DadminUser="$ADMIN_USER" \
    -DadminPassword="$ADMIN_PASSWORD" \
    -DregularUser="$REGULAR_USER" \
    -DregularPassword="$REGULAR_PASSWORD" 2>&1

EXIT_CODE=$?
echo "Pruebas finalizadas con codigo: $EXIT_CODE"
exit $EXIT_CODE
