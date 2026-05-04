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
import java.util.Map;
import java.util.List;

/**
 * Step definitions para autenticacion y autorizacion
 */
public class AuthSteps {
    private final TestContext testContext = TestContext.getInstance();

    @Given("que estoy autenticado como {string}")
    public void queEstoyAutenticadoComo(String role) {
        RequestSpecification request = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json");

        String user, password;
        
        if ("ADMIN".equalsIgnoreCase(role)) {
            user = System.getProperty("adminUser", System.getenv().getOrDefault("ADMIN_USER", "admin"));
            password = System.getProperty("adminPassword", System.getenv().getOrDefault("ADMIN_PASSWORD", "password123"));
        } else if ("USER".equalsIgnoreCase(role)) {
            user = System.getProperty("regularUser", System.getenv().getOrDefault("REGULAR_USER", "user"));
            password = System.getProperty("regularPassword", System.getenv().getOrDefault("REGULAR_PASSWORD", "password123"));
        } else {
            throw new IllegalArgumentException("Rol no soportado: " + role);
        }

        String authUrl = testContext.getAuthUrl();
        System.out.println("Autenticando como " + role + " en " + authUrl + "/auth/login");

        Response response = request
            .body(String.format("{\"username\": \"%s\", \"password\": \"%s\"}", user, password))
            .post(authUrl + "/auth/login");

        System.out.println("Respuesta login (" + role + "): " + response.getStatusCode());
        Assert.assertEquals("Fallo la autenticacion como " + role, 200, response.getStatusCode());

        String token = response.jsonPath().getString("token");
        Assert.assertNotNull("No se recibio token en la respuesta", token);
        testContext.setToken(token);
        System.out.println("Token obtenido exitosamente para " + role);
    }

    @When("intento hacer login con credenciales:")
    public void intentoHacerLoginConCredenciales(DataTable dataTable) {
        List<Map<String, String>> data = dataTable.asMaps(String.class, String.class);
        Map<String, String> credentials = data.get(0);

        String authUrl = testContext.getAuthUrl();
        String username = credentials.get("username");
        String password = credentials.get("password");

        System.out.println("Intentando login con usuario: " + username + " en " + authUrl + "/auth/login");

        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .body(String.format("{\"username\": \"%s\", \"password\": \"%s\"}", username, password))
            .post(authUrl + "/auth/login");

        System.out.println("Respuesta login: " + response.getStatusCode());
        testContext.setLastResponse(response);
    }

    @When("intento recuperar la contrasena del empleado con email {string}")
    public void intentoRecuperarLaContrasenaDelEmpleadoConEmail(String email) {
        String authUrl = testContext.getAuthUrl();
        System.out.println("Recuperando contrasena para email: " + email + " en " + authUrl + "/auth/forgot-password");

        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .body(String.format("{\"email\": \"%s\"}", email))
            .post(authUrl + "/auth/forgot-password");

        System.out.println("Respuesta recuperacion: " + response.getStatusCode());
        testContext.setLastResponse(response);
    }

    @And("la respuesta debe indicar que el email no existe")
    public void laRespuestaDebeIndicarQueEmailNoExiste() {
        Response response = testContext.getLastResponse();
        Assert.assertNotNull("No hay respuesta almacenada", response);
        String body = response.getBody().asString();
        System.out.println("Cuerpo respuesta recuperacion: " + body);
        boolean noExiste = body.contains("NO esta registrado") || body.contains("email_no_encontrado") || body.contains("\"existe\": false");
        Assert.assertTrue("La respuesta no indica que el email no existe: " + body, noExiste);
    }

    @And("la respuesta debe indicar que la recuperacion fue exitosa")
    public void laRespuestaDebeIndicarQueRecuperacionFueExitosa() {
        Response response = testContext.getLastResponse();
        Assert.assertNotNull("No hay respuesta almacenada", response);
        String body = response.getBody().asString();
        System.out.println("Cuerpo respuesta recuperacion exitosa: " + body);
        boolean exitosa = body.contains("Token de recuperacion") || body.contains("resetToken") || body.contains("\"existe\": true") || body.contains("token");
        Assert.assertTrue("La respuesta no indica que la recuperacion fue exitosa: " + body, exitosa);
    }
}
