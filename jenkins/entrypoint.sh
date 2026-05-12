#!/bin/bash

# Script de entrada personalizado para Jenkins
# Se ejecuta como root para configurar permisos, luego cambia a jenkins

echo "Entrypoint iniciado - configurando permisos Docker..."

# Eliminar cualquier configuracion JCasC residual que pueda bloquear el arranque
rm -rf /var/jenkins_home/casc_configs 2>/dev/null || true

# El socket Docker montado puede tener grupo root (GID 0) en Docker Desktop.
# Solucion: hacer que el socket sea accesible globalmente (lectura/escritura)
if [ -e /var/run/docker.sock ]; then
  chmod 666 /var/run/docker.sock
  echo "Permisos del socket Docker ajustados a 666"
  ls -la /var/run/docker.sock
fi

# Verificar acceso a Docker
docker version > /dev/null 2>&1
if [ $? -eq 0 ]; then
  echo "Docker accesible correctamente"
else
  echo "ADVERTENCIA: Docker no accesible - intentando solucion alternativa..."
  # Intentar cambiar grupo del socket
  chown root:docker /var/run/docker.sock 2>/dev/null || true
  chmod 666 /var/run/docker.sock 2>/dev/null || true
  ls -la /var/run/docker.sock
fi
echo "Cambiando al usuario jenkins para iniciar Jenkins..."
# Cambiar al usuario jenkins y ejecutar Jenkins
exec su -s /bin/bash jenkins -c "/usr/bin/tini -- /usr/local/bin/jenkins.sh"
