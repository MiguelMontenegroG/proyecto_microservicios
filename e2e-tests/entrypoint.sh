#!/bin/sh

# Establecer valores por defecto si no están definidos
BASE_URL=${BASE_URL:-http://auth-service:8085}
ADMIN_USER=${ADMIN_USER:-admin}
ADMIN_PASSWORD=${ADMIN_PASSWORD:-password123}
REGULAR_USER=${REGULAR_USER:-user}
REGULAR_PASSWORD=${REGULAR_PASSWORD:-password123}

echo "Ejecutando pruebas E2E con configuracion:"
echo "BASE_URL: $BASE_URL"
echo "ADMIN_USER: $ADMIN_USER"
echo "REGULAR_USER: $REGULAR_USER"
echo "EMPLEADOS_URL: ${EMPLEADOS_URL:-$BASE_URL}"
echo "DEPARTAMENTOS_URL: ${DEPARTAMENTOS_URL:-$BASE_URL}"
echo "AUTH_URL: ${AUTH_URL:-$BASE_URL}"
echo "NOTIFICACIONES_URL: ${NOTIFICACIONES_URL:-$BASE_URL}"

# Ejecutar pruebas Maven con los parametros
mvn test \
    -DbaseUrl="$BASE_URL" \
    -DadminUser="$ADMIN_USER" \
    -DadminPassword="$ADMIN_PASSWORD" \
    -DregularUser="$REGULAR_USER" \
    -DregularPassword="$REGULAR_PASSWORD"