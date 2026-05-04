package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.cucumber.datatable.DataTable;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import support.TestContext;
import support.PollingUtils;
import java.util.Map;
import java.util.List;

/**
 * Step definitions para operaciones de empleados
 */
public class EmployeeSteps {
    private final TestContext testContext = TestContext.getInstance();

    @Given("que existe un departamento {string} con nombre {string}")
    public void queExisteUnDepartamento(String id, String nombre) {
        String depUrl = testContext.getDepartamentosUrl();
        Response checkResponse = RestAssured.given()
            .header("Accept", "application/json")
            .when()
            .get(depUrl + "/departamentos/" + id);

        if (checkResponse.getStatusCode() == 200) {
            System.out.println("Departamento " + id + " ya existe");
            return;
        }

        RequestSpecification request = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");
        if (testContext.getToken() != null) {
            request.header("Authorization", "Bearer " + testContext.getToken());
        }

        String body = String.format("{\"id\": \"%s\", \"nombre\": \"%s\"}", id, nombre);
        Response response = request.body(body).post(depUrl + "/departamentos");
        System.out.println("Creacion departamento " + id + ": " + response.getStatusCode());
    }

    @Given("que existe un empleado registrado con:")
    @Given("que registro un nuevo empleado con datos:")
    @Given("que registro un empleado con datos:")
    @Given("he registrado un empleado con datos:")
    public void dadoQueRegistroUnEmpleadoConDatos(DataTable dataTable) {
        registroUnNuevoEmpleadoConDatos(dataTable);
    }

    @Given("que he eliminado al empleado con ID {string}")
    public void queHeEliminadoAlEmpleadoConID(String employeeId) {
        eliminoAlEmpleadoConID(employeeId);
    }

    @Given("que el empleado con ID {string} ha sido desvinculado")
    public void queElEmpleadoHaSidoDesvinculado(String employeeId) {
        eliminoAlEmpleadoConID(employeeId);
        Assert.assertNotNull("No hay respuesta de eliminacion", testContext.getLastResponse());
        System.out.println("Empleado " + employeeId + " desvinculado con codigo: " +
            testContext.getLastResponse().getStatusCode());
    }

    @When("registro un nuevo empleado con datos:")
    public void registroUnNuevoEmpleadoConDatos(DataTable dataTable) {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        Map<String, String> employeeData = data.get(0);
        String empUrl = testContext.getEmpleadosUrl();

        RequestSpecification request = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");
        if (testContext.getToken() != null) {
            request.header("Authorization", "Bearer " + testContext.getToken());
        }

        StringBuilder body = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : employeeData.entrySet()) {
            if (!first) {
                body.append(",");
            }
            body.append(String.format("\"%s\": \"%s\"", entry.getKey(), entry.getValue()));
            first = false;
        }
        body.append("}");

        System.out.println("Registrando empleado en: " + empUrl + "/empleados");
        System.out.println("Body: " + body);

        Response response = request.body(body.toString()).post(empUrl + "/empleados");
        System.out.println("Respuesta creacion empleado: " + response.getStatusCode());
        if (response.getStatusCode() != 201) {
            System.out.println("Cuerpo respuesta: " + response.getBody().asString());
        }

        testContext.setLastResponse(response);
        testContext.getData().put("lastEmployee", employeeData);
        if (employeeData.containsKey("id")) {
            testContext.getData().put("lastEmployeeId", employeeData.get("id"));
        }
        if (employeeData.containsKey("email")) {
            testContext.getData().put("lastEmployeeEmail", employeeData.get("email"));
        }
    }

    @When("elimino al empleado con ID {string}")
    public void eliminoAlEmpleadoConID(String employeeId) {
        String empUrl = testContext.getEmpleadosUrl();
        RequestSpecification request = RestAssured.given()
            .header("Accept", "application/json");
        if (testContext.getToken() != null) {
            request.header("Authorization", "Bearer " + testContext.getToken());
        }

        System.out.println("Eliminando empleado con ID: " + employeeId + " desde: " + empUrl + "/empleados/" + employeeId);
        Response response = request.delete(empUrl + "/empleados/" + employeeId);
        System.out.println("Respuesta eliminacion: " + response.getStatusCode());
        testContext.setLastResponse(response);
    }

    @When("el empleado {string} establece su contrasena como {string}")
    public void elEmpleadoEstableceSuContrasena(String employeeId, String password) {
        String authUrl = testContext.getAuthUrl();
        System.out.println("Estableciendo contrasena para empleado " + employeeId + " en " + authUrl + "/auth/change-password");

        RequestSpecification request = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");
        if (testContext.getToken() != null) {
            request.header("Authorization", "Bearer " + testContext.getToken());
        }

        String body = String.format("{\"id\": \"%s\", \"password\": \"%s\"}", employeeId, password);
        Response response = request.body(body).put(authUrl + "/auth/change-password");
        System.out.println("Respuesta cambio contrasena: " + response.getStatusCode());
        testContext.setLastResponse(response);
    }

    @Then("eventualmente el empleado con ID {string} debe existir")
    public void eventualmenteElEmpleadoConIDDebeExistir(String employeeId) throws InterruptedException {
        String empUrl = testContext.getEmpleadosUrl();
        boolean condition = PollingUtils.waitUntil(() -> {
            try {
                RequestSpecification request = RestAssured.given().header("Accept", "application/json");
                if (testContext.getToken() != null) {
                    request.header("Authorization", "Bearer " + testContext.getToken());
                }
                Response response = request.get(empUrl + "/empleados/" + employeeId);
                testContext.setLastResponse(response);
                return response.getStatusCode() == 200;
            } catch (Exception e) {
                return false;
            }
        });
        Assert.assertTrue("No se encontro al empleado con ID " + employeeId, condition);
        System.out.println("Empleado " + employeeId + " existe confirmado");
    }

    @And("eventualmente el empleado con ID {string} no debe existir")
    public void eventualmenteElEmpleadoConIDNoDebeExistir(String employeeId) throws InterruptedException {
        String empUrl = testContext.getEmpleadosUrl();
        boolean condition = PollingUtils.waitUntil(() -> {
            try {
                RequestSpecification request = RestAssured.given().header("Accept", "application/json");
                if (testContext.getToken() != null) {
                    request.header("Authorization", "Bearer " + testContext.getToken());
                }
                Response response = request.get(empUrl + "/empleados/" + employeeId);
                testContext.setLastResponse(response);
                return response.getStatusCode() == 404;
            } catch (Exception e) {
                return false;
            }
        });
        Assert.assertTrue("El empleado con ID " + employeeId + " aun existe", condition);
        System.out.println("Empleado " + employeeId + " ya no existe confirmado");
    }

    @And("eventualmente el empleado {string} debe poder hacer login con contrasena {string}")
    public void eventualmenteElEmpleadoDebePoderHacerLogin(String employeeId, String password) throws InterruptedException {
        String email = (String) testContext.getData().get("lastEmployeeEmail");
        if (email == null) {
            email = employeeId + "@empresa.com";
        }
        final String userEmail = email;
        String authUrl = testContext.getAuthUrl();

        boolean condition = PollingUtils.waitUntil(() -> {
            try {
                Response response = RestAssured.given()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(String.format("{\"username\": \"%s\", \"password\": \"%s\"}", userEmail, password))
                    .post(authUrl + "/auth/login");
                if (response.getStatusCode() == 200) {
                    testContext.setLastResponse(response);
                    return true;
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        });
        Assert.assertTrue("El empleado " + employeeId + " no pudo hacer login", condition);
        System.out.println("Login exitoso para empleado " + employeeId);
    }

    @And("eventualmente el servicio de autenticacion debe haber creado un usuario para {string}")
    public void eventualmenteServicioAuthDebeHaberCreadoUsuario(String employeeId) throws InterruptedException {
        String email = (String) testContext.getData().get("lastEmployeeEmail");
        if (email == null) {
            email = employeeId + "@empresa.com";
        }
        final String userEmail = email;
        String authUrl = testContext.getAuthUrl();

        boolean condition = PollingUtils.waitUntil(() -> {
            try {
                Response response = RestAssured.given()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .body(String.format("{\"username\": \"%s\", \"password\": \"%s\"}", userEmail, "contrasena_invalida"))
                    .post(authUrl + "/auth/login");
                int status = response.getStatusCode();
                return status == 403 || status == 401;
            } catch (Exception e) {
                return false;
            }
        });
        Assert.assertTrue("El servicio de autenticacion no creo un usuario para " + employeeId, condition);
        System.out.println("Usuario de autenticacion creado para empleado " + employeeId);
    }

    @And("eventualmente se debe haber generado una notificacion de tipo {string} para {string}")
    public void eventualmenteSeDebeHaberGeneradoNotificacion(String tipo, String employeeId) throws InterruptedException {
        String notifUrl = testContext.getNotificacionesUrl();
        boolean condition = PollingUtils.waitUntil(() -> {
            try {
                RequestSpecification request = RestAssured.given().header("Accept", "application/json");
                if (testContext.getToken() != null) {
                    request.header("Authorization", "Bearer " + testContext.getToken());
                }
                Response response = request.get(notifUrl + "/notificaciones/" + employeeId);
                testContext.setLastResponse(response);
                if (response.getStatusCode() == 200) {
                    return response.getBody().asString().contains(tipo);
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        });
        Assert.assertTrue("No se genero notificacion de tipo " + tipo + " para " + employeeId, condition);
        System.out.println("Notificacion " + tipo + " generada para empleado " + employeeId);
    }

    public TestContext getTestContext() {
        return testContext;
    }
}
