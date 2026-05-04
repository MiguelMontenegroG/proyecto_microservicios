package stepdefinitions;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import support.TestContext;
import support.PollingUtils;

/**
 * Step definitions comunes para todas las pruebas
 */
public class CommonSteps {
    private final TestContext testContext = TestContext.getInstance();

    @Given("que el sistema esta desplegado y operativo")
    public void queElSistemaEstaDesplegadoYOperativo() {
        String baseUrl = testContext.getBaseUrl();
        System.out.println("Verificando sistema desplegado en: " + baseUrl);
        Response response = RestAssured.given()
            .header("Accept", "application/json")
            .when()
            .get(baseUrl + "/actuator/health");
        System.out.println("Health check response: " + response.getStatusCode());
        testContext.setLastResponse(response);
    }

    @Given("que el sistema esta desplegado")
    public void queElSistemaEstaDesplegado() {
        String baseUrl = testContext.getBaseUrl();
        System.out.println("Verificando sistema desplegado en: " + baseUrl);
    }

    @When("consulto el endpoint base")
    public void consultoElEndpointBase() {
        System.out.println("Consultando endpoint base: " + testContext.getBaseUrl());
        Response response = RestAssured.get(testContext.getBaseUrl());
        testContext.setLastResponse(response);
        System.out.println("Respuesta endpoint base: " + response.getStatusCode());
    }

    @When("consulto la lista de empleados sin token de autenticacion")
    public void consultoLaListaDeEmpleadosSinToken() {
        String empUrl = testContext.getEmpleadosUrl();
        System.out.println("Consultando empleados sin token en: " + empUrl + "/empleados");
        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .when()
            .get(empUrl + "/empleados");
        System.out.println("Respuesta sin token: " + response.getStatusCode());
        testContext.setLastResponse(response);
    }

    @When("uso un token invalido para acceder a empleados")
    public void usoUnTokenInvalidoParaAccederAEmpleados() {
        String empUrl = testContext.getEmpleadosUrl();
        System.out.println("Usando token invalido en: " + empUrl + "/empleados");
        Response response = RestAssured.given()
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .header("Authorization", "Bearer invalid_token_12345")
            .when()
            .get(empUrl + "/empleados");
        System.out.println("Respuesta con token invalido: " + response.getStatusCode());
        testContext.setLastResponse(response);
    }

    @Then("la respuesta debe tener codigo {int}")
    public void laRespuestaDebeTenerCodigo(int statusCode) {
        Response response = testContext.getLastResponse();
        Assert.assertNotNull("No hay respuesta almacenada", response);
        int actualCode = response.getStatusCode();
        System.out.println("Verificando codigo de estado: esperado=" + statusCode + ", actual=" + actualCode);
        response.then().statusCode(statusCode);
    }

    @And("eventualmente la respuesta debe tener codigo {int}")
    public void eventualmenteLaRespuestaDebeTenerCodigo(int statusCode) throws InterruptedException {
        boolean condition = PollingUtils.waitUntil(() -> {
            try {
                Response response = RestAssured.given()
                    .header("Accept", "application/json")
                    .when()
                    .get(testContext.getBaseUrl());
                testContext.setLastResponse(response);
                return response.getStatusCode() == statusCode;
            } catch (Exception e) {
                return false;
}
        });
        Assert.assertTrue("No se alcanzo el codigo de estado esperado " + statusCode, condition);
    }
}
