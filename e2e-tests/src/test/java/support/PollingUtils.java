package support;

import java.util.function.Supplier;

/**
 * Utilidad para manejar operaciones de polling
 * con configuración flexible y manejo de errores
 */
public class PollingUtils {
    private static final int DEFAULT_MAX_ATTEMPTS = 15;
    private static final int DEFAULT_INTERVAL_MS = 1500;

    /**
     * Espera hasta que una condición se cumpla o se alcance el límite de intentos
     * 
     * @param condition Condición a verificar
     * @param maxAttempts Máximo número de intentos
     * @param intervalMs Intervalo entre intentos en milisegundos
     * @return true si la condición se cumplió, false en caso contrario
     * @throws InterruptedException si se interrumpe el hilo
     */
    public static boolean waitUntil(Supplier<Boolean> condition, int maxAttempts, int intervalMs) throws InterruptedException {
        int attempt = 0;
        
        while (attempt < maxAttempts) {
            if (condition.get()) {
                return true;
            }
            
            Thread.sleep(intervalMs);
            attempt++;
        }
        
        return false;
    }
    
    /**
     * Espera con valores por defecto (15 intentos, 1.5s intervalo)
     * 
     * @param condition Condición a verificar
     * @return true si la condición se cumplió, false en caso contrario
     * @throws InterruptedException si se interrumpe el hilo
     */
    public static boolean waitUntil(Supplier<Boolean> condition) throws InterruptedException {
        return waitUntil(condition, DEFAULT_MAX_ATTEMPTS, DEFAULT_INTERVAL_MS);
    }
}