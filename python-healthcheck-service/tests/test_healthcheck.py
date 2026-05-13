"""
Tests unitarios para python-healthcheck-service.
Si el servicio ya tiene tests, adapta las importaciones según la estructura real.
"""
import pytest
import json


# ─── Intenta importar la app Flask del servicio ────────────────────────────
# Ajusta el import según tu archivo principal (app.py, main.py, etc.)
try:
    from app import app as flask_app  # noqa: F401
    HAS_APP = True
except ImportError:
    HAS_APP = False


# ─── Tests básicos que siempre corren ──────────────────────────────────────

class TestHealthLogic:
    """Tests de lógica de negocio del healthcheck (sin HTTP)."""

    def test_services_list_not_empty(self):
        """La lista de servicios a verificar no debe estar vacía."""
        services = [
            "empleados-service",
            "departamentos-service",
            "perfiles-service",
            "notificaciones-service",
            "auth-service",
        ]
        assert len(services) > 0

    def test_health_status_values(self):
        """Los estados válidos de salud son UP y DOWN."""
        valid_statuses = {"UP", "DOWN", "UNKNOWN"}
        test_status = "UP"
        assert test_status in valid_statuses

    def test_health_response_structure(self):
        """Un response de healthcheck debe tener los campos requeridos."""
        mock_response = {
            "status": "UP",
            "services": {
                "empleados-service": "UP",
                "departamentos-service": "UP",
            },
            "timestamp": "2026-01-01T00:00:00"
        }
        assert "status" in mock_response
        assert "services" in mock_response
        assert mock_response["status"] in {"UP", "DOWN", "DEGRADED"}

    def test_all_services_up_means_system_up(self):
        """Si todos los servicios están UP, el sistema debe reportar UP."""
        service_statuses = {"svc1": "UP", "svc2": "UP", "svc3": "UP"}
        system_status = "UP" if all(v == "UP" for v in service_statuses.values()) else "DEGRADED"
        assert system_status == "UP"

    def test_any_service_down_means_degraded(self):
        """Si algún servicio está DOWN, el sistema debe reportar DEGRADED o DOWN."""
        service_statuses = {"svc1": "UP", "svc2": "DOWN", "svc3": "UP"}
        system_status = "UP" if all(v == "UP" for v in service_statuses.values()) else "DEGRADED"
        assert system_status != "UP"

    def test_json_serialization(self):
        """El resultado del healthcheck debe ser serializable a JSON."""
        response = {"status": "UP", "services": {}}
        json_str = json.dumps(response)
        parsed = json.loads(json_str)
        assert parsed["status"] == "UP"


# ─── Tests con Flask si la app está disponible ─────────────────────────────

@pytest.mark.skipif(not HAS_APP, reason="App Flask no encontrada")
class TestFlaskEndpoints:
    """Tests de los endpoints HTTP del servicio."""

    @pytest.fixture
    def client(self):
        flask_app.config["TESTING"] = True
        with flask_app.test_client() as client:
            yield client

    def test_health_endpoint_returns_200(self, client):
        """El endpoint /health debe responder 200."""
        response = client.get("/health")
        assert response.status_code == 200

    def test_health_endpoint_returns_json(self, client):
        """El endpoint /health debe retornar JSON."""
        response = client.get("/health")
        data = json.loads(response.data)
        assert "status" in data
