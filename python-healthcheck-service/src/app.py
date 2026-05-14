from flask import Flask, jsonify, request
import time
import os
import logging
from pythonjsonlogger import jsonlogger

# --- Logs estructurados JSON (Reto 7 - Loki) ---
log_handler = logging.StreamHandler()
formatter = jsonlogger.JsonFormatter(
    fmt='%(timestamp)s %(level)s %(name)s %(message)s',
    datefmt='%Y-%m-%dT%H:%M:%SZ'
)
log_handler.setFormatter(formatter)
logger = logging.getLogger(__name__)
logger.addHandler(log_handler)
logger.setLevel(logging.INFO)

# Reemplazar el handler root tambien
root_logger = logging.getLogger()
root_logger.handlers = [log_handler]
root_logger.setLevel(logging.INFO)

app = Flask(__name__)

# --- Prometheus (Reto 7 - Metricas) ---
from prometheus_flask_exporter import PrometheusMetrics

metrics = PrometheusMetrics(app)
metrics.info('app_info', 'Application info', version='1.0.0')

START_TIME = time.time()

# Endpoint de metricas en /metrics (por defecto con prometheus-flask-exporter)

@app.route('/health')
def health():
    uptime_seconds = int(time.time() - START_TIME)
    logger.info('Health check solicitado', extra={'service': 'python-healthcheck-service'})
    return jsonify({
        'status': 'UP',
        'service': 'python-healthcheck-service',
        'uptime_seconds': uptime_seconds,
        'version': '1.0.0',
        'checks': {
            'application': 'UP'
        }
    })


@app.route('/api/status')
def status():
    logger.info('Status endpoint consultado', extra={'service': 'python-healthcheck-service'})
    return jsonify({
        'status': 'running',
        'service': 'python-healthcheck-service',
        'description': 'Microservicio de monitoreo de estado del sistema',
        'ci_integration': True
    })


@app.route('/')
def home():
    return jsonify({
        'name': 'Python Healthcheck Service',
        'purpose': 'Monitoreo de estado del sistema de microservicios',
        'endpoints': {
            '/health': 'Health check del servicio',
            '/api/status': 'Estado general del servicio',
            '/metrics': 'Metricas en formato Prometheus'
        },
        'language': 'Python',
        'framework': 'Flask'
    })


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8086)
