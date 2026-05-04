package support;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Contexto compartido entre pasos de prueba
 * Utiliza Singleton para compartir estado entre steps
 * Cada escenario obtiene su propia instancia via getInstance()
 */
public class TestContext {
    private static TestContext instance;

    private String token;
    private Response lastResponse;
    private String baseUrl;
    private final Map<String, Object> data = new HashMap<>();

    // URLs especificas por servicio
    private final String empleadosUrl;
    private final String departamentosUrl;
    private final String authUrl;
    private final String notificacionesUrl;

    public TestContext() {
        String baseUrl = System.getProperty("baseUrl",
            System.getenv().getOrDefault("BASE_URL", "http://localhost:8085"));
        this.baseUrl = baseUrl;

        // Configurar URLs especificas por servicio, con fallback a BASE_URL
        this.authUrl = System.getProperty("authUrl",
            System.getenv().getOrDefault("AUTH_URL", baseUrl));
        this.empleadosUrl = System.getProperty("empleadosUrl",
            System.getenv().getOrDefault("EMPLEADOS_URL", baseUrl));
        this.departamentosUrl = System.getProperty("departamentosUrl",
            System.getenv().getOrDefault("DEPARTAMENTOS_URL", baseUrl));
        this.notificacionesUrl = System.getProperty("notificacionesUrl",
            System.getenv().getOrDefault("NOTIFICACIONES_URL", baseUrl));
    }

    public static synchronized TestContext getInstance() {
        if (instance == null) {
            instance = new TestContext();
        }
        return instance;
    }

    public static synchronized void resetInstance() {
        instance = null;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Response getLastResponse() {
        return lastResponse;
    }

    public void setLastResponse(Response lastResponse) {
        this.lastResponse = lastResponse;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public String getEmpleadosUrl() {
        return empleadosUrl;
    }

    public String getDepartamentosUrl() {
        return departamentosUrl;
    }

    public String getNotificacionesUrl() {
        return notificacionesUrl;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void clearData() {
        data.clear();
    }

    public void clear() {
        this.token = null;
        this.lastResponse = null;
        this.data.clear();
    }
}
