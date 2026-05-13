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
import java.util.HashMap;

/**
 * Step definitions para operaciones de empleados
 */
public class EmployeeSteps {
    private final TestContext testContext = TestContext.getInstance();

    // Mapa de IDs referenciales (del feature) a IDs reales unicos generados
    private static final Map<String, String> idMapping = new HashMap<>();
    private static int idCounter = 0;

    private static synchronized String getOrCreateUniqueId(String referenceId) {
        if (!idMapping.containsKey(referenceId)) {
            idCounter++;
            idMapping.put(referenceId, referenceId + "_" + idCounter);
        }
        return idMapping.get(referenceId);
    }

    private static String resolveId(String referenceId) {
        return idMapping.getOrDefault(referenceId, referenceId);
    }

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
        if (response.getStatusCode() != 201 && response.getStatusCode() != 200) {
            System.out.println("Respuesta: " + response.getBody().asString());
        }
    }

    @Given("que existe un empleado registrado con:")
    public void dadoQueExisteUnEmpleadoRegistradoCon(DataTable dataTable) {
        crearEmpleadoConIdUnico(dataTable, true);
    }

    @Given("que registro un nuevo empleado con datos:")
    @Given("que registro un empleado con datos:")
    @Given("he registrado un empleado con datos:")
    public void dadoQueRegistroUnEmpleadoConDatos(DataTable dataTable) {
        crearEmpleadoConIdUnico(dataTable, true);
    }

    @Given("que he eliminado al empleado con ID {string}")
    public void queHeEliminadoAlEmpleadoConID(String employeeId) {
        eliminoAlEmpleadoConID(employeeId);
    }

    @Given("que el empleado con ID {string} ha sido desvinculado")
    public void queElEmpleadoHaSidoDesvinculado(String employeeId) {
        String actualId = resolveId(employeeId);
        eliminoAlEmpleadoConID(actualId);
        if (testContext.getLastResponse() != null) {
            System.out.println("Empleado " + actualId + " desvinculado con codigo: " +
                testContext.getLastResponse().getStatusCode());
        }
    }

    @When("registro un nuevo empleado con datos:")
    public void registroUnNuevoEmpleadoConDatos(DataTable dataTable) {
        crearEmpleadoConIdUnico(dataTable, true);
    }

    private void crearEmpleadoConIdUnico(DataTable dataTable, boolean almacenarEnContexto) {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        Map<String, String> employeeData = new HashMap<>(data.get(0));
        String empUrl = testContext.getEmpleadosUrl();

        // Generar ID unico para evitar conflictos 409
        String originalId = employeeData.getOrDefault("id", "EMP");
        String uniqueId = getOrCreateUniqueId(originalId);
        String originalEmail = employeeData.getOrDefault("email", uniqueId + "@empresa.com");

        // Mantener el email original del feature (e.g. "carlos.mendoza@empresa.com")
        // pero si contiene el ID original, reemplazarlo
        String uniqueEmail = originalEmail.contains(originalId) ?
            originalEmail.replace(originalId, uniqueId) : originalEmail;

        employeeData.put("id", uniqueId);
        employeeData.put("email", uniqueEmail);

        RequestSpecification request = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");
        if (testContext.getToken() != null) {
            request.header("Authorization", "Bearer " + testContext.getToken());
        }

        StringBuilder body = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : employeeData.entrySet()) {
            if (!first) body.append(",");
            body.append(String.format("\"%s\": \"%s\"", entry.getKey(), entry.getValue()));
            first = false;
        }
        body.append("}");

        System.out.println("Registrando empleado: ID original=" + originalId + " -> ID unico=" + uniqueId);
        System.out.println("Body: " + body);

        Response response = request.body(body.toString()).post(empUrl + "/empleados");
        System.out.println("Respuesta creacion empleado: " + response.getStatusCode());
        if (response.getStatusCode() != 201) {
            System.out.println("Cuerpo respuesta: " + response.getBody().asString());
        }

        testContext.setLastResponse(response);
        if (almacenarEnContexto) {
            testContext.getData().put("lastEmployee", new HashMap<>(employeeData));
            testContext.getData().put("lastEmployeeId", uniqueId);
            testContext.getData().put("lastEmployeeEmail", uniqueEmail);
        }
    }

    @When("elimino al empleado con ID {string}")
    public void eliminoAlEmpleadoConID(String employeeId) {
        String actualId = resolveId(employeeId);
        String empUrl = testContext.getEmpleadosUrl();

        RequestSpecification request = RestAssured.given()
            .header("Accept", "application/json");
        if (testContext.getToken() != null) {
            request.header("Authorization", "Bearer " + testContext.getToken());
        }

        System.out.println("Eliminando empleado con ID: " + actualId + " desde: " + empUrl + "/empleados/" + actualId);
        Response response = request.delete(empUrl + "/empleados/" + actualId);
        System.out.println("Respuesta eliminacion: " + response.getStatusCode());
        testContext.setLastResponse(response);
    }

    @When("el empleado {string} establece su contrasena como {string}")
    public void elEmpleadoEstableceSuContrasena(String employeeId, String password) {
        String actualId = resolveId(employeeId);
        String authUrl = testContext.getAuthUrl();
        System.out.println("Estableciendo contrasena para empleado " + actualId + " en " + authUrl + "/auth/change-password");

        RequestSpecification request = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");
        if (testContext.getToken() != null) {
            request.header("Authorization", "Bearer " + testContext.getToken());
        }

        String body = String.format("{\"id\": \"%s\", \"password\": \"%s\"}", actualId, password);
        Response response = request.body(body).put(authUrl + "/auth/change-password");
        System.out.println("Respuesta cambio contrasena: " + response.getStatusCode());
        testContext.setLastResponse(response);
    }

    @Then("eventualmente el empleado con ID {string} debe existir")
    public void eventualmenteElEmpleadoConIDDebeExistir(String employeeId) throws InterruptedException {
        String actualId = resolveId(employeeId);
        String empUrl = testContext.getEmpleadosUrl();

        boolean condition = PollingUtils.waitUntil(() -> {
            try {
                RequestSpecification request = RestAssured.given().header("Accept", "application/json");
                if (testContext.getToken() != null) {
                    request.header("Authorization", "Bearer " + testContext.getToken());
                }
                Response response = request.get(empUrl + "/empleados/" + actualId);
                testContext.setLastResponse(response);
                return response.getStatusCode() == 200;
            } catch (Exception e) {
                return false;
            }
        });
        Assert.assertTrue("No se encontro al empleado con ID " + actualId, condition);
        System.out.println("Empleado " + actualId + " existe confirmado");
    }

    @And("eventualmente el empleado con ID {string} no debe existir")
    public void eventualmenteElEmpleadoConIDNoDebeExistir(String employeeId) throws InterruptedException {
        String actualId = resolveId(employeeId);
        String empUrl = testContext.getEmpleadosUrl();

        boolean condition = PollingUtils.waitUntil(() -> {
            try {
                RequestSpecification request = RestAssured.given().header("Accept", "application/json");
                if (testContext.getToken() != null) {
                    request.header("Authorization", "Bearer " + testContext.getToken());
                }
                Response response = request.get(empUrl + "/empleados/" + actualId);
                testContext.setLastResponse(response);
                return response.getStatusCode() == 404;
            } catch (Exception e) {
                return false;
            }
        });
        Assert.assertTrue("El empleado con ID " + actualId + " aun existe", condition);
        System.out.println("Empleado " + actualId + " ya no existe confirmado");
    }

    @And("eventualmente el empleado {string} debe poder hacer login con contrasena {string}")
    public void eventualmenteElEmpleadoDebePoderHacerLogin(String employeeId, String password) throws InterruptedException {
        String email = (String) testContext.getData().get("lastEmployeeEmail");
        if (email == null) {
            String actualId = resolveId(employeeId);
            email = actualId + "@empresa.com";
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
            String actualId = resolveId(employeeId);
            email = actualId + "@empresa.com";
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
                System.out.println("Verificando existencia usuario " + userEmail + " - status: " + status);
                return status == 403 || status == 401 || status == 200;
            } catch (Exception e) {
                return false;
            }
        });
        Assert.assertTrue("El servicio de autenticacion no creo un usuario para " + employeeId, condition);
        System.out.println("Usuario de autenticacion creado para empleado " + employeeId);
    }

    @And("eventualmente se debe haber generado una notificacion de tipo {string} para {string}")
    public void eventualmenteSeDebeHaberGeneradoNotificacion(String tipo, String employeeId) throws InterruptedException {
        String actualId = resolveId(employeeId);
        String notifUrl = testContext.getNotificacionesUrl();

        boolean condition = PollingUtils.waitUntil(() -> {
            try {
                RequestSpecification request = RestAssured.given().header("Accept", "application/json");
                if (testContext.getToken() != null) {
                    request.header("Authorization", "Bearer " + testContext.getToken());
                }
                Response response = request.get(notifUrl + "/notificaciones/" + actualId);
                testContext.setLastResponse(response);
                if (response.getStatusCode() == 200) {
                    String body = response.getBody().asString();
                    System.out.println("Notificacion encontrada: " + body);
                    return body.contains(tipo);
                }
                return false;
            } catch (Exception e) {
                return false;
            }
        });
        Assert.assertTrue("No se genero notificacion de tipo " + tipo + " para " + actualId, condition);
        System.out.println("Notificacion " + tipo + " generada para empleado " + actualId);
    }

    public TestContext getTestContext() {
        return testContext;
    }
}
