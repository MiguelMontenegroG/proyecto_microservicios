package stepdefinitions;

import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.datatable.DataTable;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.Assert;
import support.TestContext;
import java.util.Map;
import java.util.List;

/**
 * Step definitions para pruebas de seguridad y control de acceso
 */
public class SecuritySteps {
    private final TestContext testContext = TestContext.getInstance();

    // NOTA: Los pasos base de seguridad (consulta sin token, token invalido)
    // estan definidos en CommonSteps.java
    // Este archivo se mantiene para futuros pasos especificos de seguridad
}
