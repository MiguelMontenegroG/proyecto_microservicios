FROM jenkins/jenkins:lts-jdk17

# Instalar plugins necesarios de forma automatizada
RUN jenkins-plugin-cli --plugins \
    docker-workflow \
    pipeline-stage-view \
    workflow-aggregator \
    git \
    configuration-as-code \
    job-dsl \
    sonar \
    htmlpublisher \
    junit \
    matrix-auth \
    credentials-binding \
    pipeline-utility-steps \
    timestamper \
    build-timeout \
    ws-cleanup

# Instalar cliente Docker dentro del contenedor Jenkins
USER root
RUN apt-get update && \
    apt-get install -y docker.io curl && \
    rm -rf /var/lib/apt/lists/*

# Agregar usuario jenkins al grupo docker para acceder al socket
RUN groupadd -f docker && usermod -aG docker jenkins

# Instalar docker-compose
RUN curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-Linux-x86_64" -o /usr/local/bin/docker-compose && \
    chmod +x /usr/local/bin/docker-compose

# Copiar entrypoint personalizado que configura permisos del socket Docker
COPY entrypoint.sh /usr/local/bin/entrypoint.sh
RUN chmod +x /usr/local/bin/entrypoint.sh

ENTRYPOINT ["/usr/local/bin/entrypoint.sh"]
