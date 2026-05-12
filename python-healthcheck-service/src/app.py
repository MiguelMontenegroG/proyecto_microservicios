from flask import Flask, jsonify
import time

app = Flask(__name__)

START_TIME = time.time()


@app.route('/health')
def health():
    uptime_seconds = int(time.time() - START_TIME)
    return jsonify({
        'status': 'UP',
        'service': 'python-healthcheck-service',
        'uptime_seconds': uptime_seconds,
        'version': '1.0.0'
    })


@app.route('/api/status')
def status():
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
            '/api/status': 'Estado general del servicio'
        },
        'language': 'Python',
        'framework': 'Flask'
    })


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8086)
