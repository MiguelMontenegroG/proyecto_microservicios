import json
import pytest
from app import app


@pytest.fixture
def client():
    app.config['TESTING'] = True
    with app.test_client() as client:
        yield client


def test_health_endpoint(client):
    response = client.get('/health')
    data = json.loads(response.data)

    assert response.status_code == 200
    assert data['status'] == 'UP'
    assert data['service'] == 'python-healthcheck-service'
    assert 'uptime_seconds' in data
    assert data['version'] == '1.0.0'


def test_status_endpoint(client):
    response = client.get('/api/status')
    data = json.loads(response.data)

    assert response.status_code == 200
    assert data['status'] == 'running'
    assert data['service'] == 'python-healthcheck-service'
    assert data['ci_integration'] is True


def test_home_endpoint(client):
    response = client.get('/')
    data = json.loads(response.data)

    assert response.status_code == 200
    assert data['name'] == 'Python Healthcheck Service'
    assert 'endpoints' in data
    assert data['language'] == 'Python'
    assert data['framework'] == 'Flask'


def test_health_uptime_increases(client):
    response1 = client.get('/health')
    data1 = json.loads(response1.data)
    uptime1 = data1['uptime_seconds']

    import time
    time.sleep(0.1)

    response2 = client.get('/health')
    data2 = json.loads(response2.data)
    uptime2 = data2['uptime_seconds']

    assert uptime2 > uptime1


def test_health_endpoint_has_all_required_fields(client):
    response = client.get('/health')
    data = json.loads(response.data)

    required_fields = ['status', 'service', 'uptime_seconds', 'version']
    for field in required_fields:
        assert field in data, f"Campo '{field}' faltante en respuesta de health"


def test_home_endpoint_lists_endpoints(client):
    response = client.get('/')
    data = json.loads(response.data)

    assert '/health' in data['endpoints']
    assert '/api/status' in data['endpoints']
    assert len(data['endpoints']) == 2


def test_status_endpoint_returns_description(client):
    response = client.get('/api/status')
    data = json.loads(response.data)

    assert 'description' in data
    assert 'monitoreo' in data['description'].lower()
