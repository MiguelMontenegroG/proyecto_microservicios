package support;

import io.restassured.response.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Contexto compartido entre pasos de prueba
 * Se crea una nueva instancia por cada escenario via Hooks
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
        String defaultUrl = "http://localhost:8080";

        // Cada servicio en Docker Compose tiene su propio host y puerto
        this.authUrl = getUrl("AUTH_URL", "http://auth-service:8085", defaultUrl);
        this.empleadosUrl = getUrl("EMPLEADOS_URL", "http://empleados-service:8080", defaultUrl);
        this.departamentosUrl = getUrl("DEPARTAMENTOS_URL", "http://departamentos-service:8081", defaultUrl);
        this.notificacionesUrl = getUrl("NOTIFICACIONES_URL", "http://notificaciones-service:8084", defaultUrl);

        // baseUrl se usa como fallback general
        this.baseUrl = System.getProperty("baseUrl",
            System.getenv().getOrDefault("BASE_URL", this.authUrl));
    }

    private String getUrl(String envVar, String dockerDefault, String fallback) {
        String fromSys = System.getProperty(toCamelCase(envVar));
        if (fromSys != null && !fromSys.isEmpty()) return fromSys;
        String fromEnv = System.getenv(envVar);
        if (fromEnv != null && !fromEnv.isEmpty()) return fromEnv;
        String fromBase = System.getProperty("baseUrl", System.getenv("BASE_URL"));
        if (fromBase != null && !fromBase.isEmpty()) return fromBase;
        // Intentar variable dockerizada
        String fromDockerEnv = System.getenv(toDockerEnv(envVar));
        if (fromDockerEnv != null && !fromDockerEnv.isEmpty()) return fromDockerEnv;
        return dockerDefault;
    }

    private String toCamelCase(String envVar) {
        // Convierte AUTH_URL a authUrl
        String[] parts = envVar.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            sb.append(Character.toUpperCase(parts[i].charAt(0)));
            sb.append(parts[i].substring(1));
        }
        return sb.toString();
    }

    private String toDockerEnv(String envVar) {
        // Para compatibilidad con nombres de variables dockerizadas
        return envVar;
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

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Response getLastResponse() { return lastResponse; }
    public void setLastResponse(Response lastResponse) { this.lastResponse = lastResponse; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getAuthUrl() { return authUrl; }
    public String getEmpleadosUrl() { return empleadosUrl; }
    public String getDepartamentosUrl() { return departamentosUrl; }
    public String getNotificacionesUrl() { return notificacionesUrl; }

    public Map<String, Object> getData() { return data; }
    public void clearData() { data.clear(); }
    public void clear() {
        this.token = null;
        this.lastResponse = null;
        this.data.clear();
    }
}
