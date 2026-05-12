FROM jenkins/jenkins:lts-jdk17

# Pre-instalar plugins necesarios de forma automatizada
RUN jenkins-plugin-cli --plugins \
  workflow-aggregator \
  pipeline-stage-view \
  git \
  configuration-as-code \
  job-dsl \
  sonar \
  pipeline-utility-steps \
  jacoco \
  blueocean \
  docker-workflow \
  docker-commons \
  credentials-binding \
  plain-credentials

# Instalar cliente Docker dentro del contenedor Jenkins
USER root
RUN apt-get update && \
  apt-get install -y docker.io && \
  rm -rf /var/lib/apt/lists/*

# Crear script docker-compose que invoque el plugin compose de docker
RUN printf '#!/bin/bash\ndocker compose "$@"\n' > /usr/local/bin/docker-compose && \
    chmod +x /usr/local/bin/docker-compose

# Verificar que docker-compose este disponible
RUN docker-compose --version

# Agregar usuario jenkins al grupo docker para acceder al socket
RUN groupadd -f docker && usermod -aG docker jenkins

# Copiar entrypoint personalizado que hace setup como root y luego ejecuta Jenkins como jenkins
COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

# Asegurar que el entrypoint se ejecute como root
USER root
# Usar entrypoint personalizado
ENTRYPOINT ["/entrypoint.sh"]
