package support;

import io.cucumber.java.Before;
import io.cucumber.java.After;
import io.cucumber.java.Scenario;

/**
 * Hooks de Cucumber para configuracion y limpieza
 * Se ejecutan antes y despues de cada escenario
 */
public class Hooks {
    @Before
    public void setUp(Scenario scenario) {
        TestContext context = TestContext.getInstance();
        String baseUrl = System.getProperty("baseUrl",
            System.getenv().getOrDefault("BASE_URL", "http://localhost:8085"));
        context.setBaseUrl(baseUrl);
        context.clear();
        System.out.println("=== Iniciando escenario: " + scenario.getName() + " ===");
        System.out.println("BASE_URL configurada: " + baseUrl);
    }

    @After
    public void tearDown(Scenario scenario) {
        TestContext context = TestContext.getInstance();
        System.out.println("=== Finalizando escenario: " + scenario.getName() +
            " - Estado: " + scenario.getStatus() + " ===");
        context.clear();
    }
}
