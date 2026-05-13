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
        // Resetear contexto para cada escenario (independencia total)
        TestContext.resetInstance();
        TestContext context = TestContext.getInstance();
        System.out.println("=== Iniciando escenario: " + scenario.getName() + " ===");
        System.out.println("AUTH_URL: " + context.getAuthUrl());
        System.out.println("EMPLEADOS_URL: " + context.getEmpleadosUrl());
        System.out.println("DEPARTAMENTOS_URL: " + context.getDepartamentosUrl());
        System.out.println("NOTIFICACIONES_URL: " + context.getNotificacionesUrl());
    }

    @After
    public void tearDown(Scenario scenario) {
        TestContext context = TestContext.getInstance();
        System.out.println("=== Finalizando escenario: " + scenario.getName() +
            " - Estado: " + scenario.getStatus() + " ===");
        context.clear();
    }
}
