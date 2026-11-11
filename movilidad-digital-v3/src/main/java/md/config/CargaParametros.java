package md.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

public class CargaParametros {

    private static final Properties CONFIG = new Properties();

    static {
        // Intento 1: Buscar en el classpath (src/main/resources)
        try (InputStream in = CargaParametros.class.getResourceAsStream("/application.properties")) {
            if (in != null) {
                CONFIG.load(in);
            } else {
                // Intento 2: Buscar en la raíz del proyecto como "application.properties" o "config.properties"
                try (InputStream fileIn = new FileInputStream("application.properties")) {
                    CONFIG.load(fileIn);
                } catch (IOException e2) {
                    try (InputStream fileIn2 = new FileInputStream("config.properties")) {
                        CONFIG.load(fileIn2);
                    } catch (IOException e3) {
                        // Si todo falla, cargamos valores por defecto para que la app no rompa al iniciar
                        System.err.println("Advertencia: No se encontró archivo de propiedades. Usando valores por defecto.");
                        CONFIG.setProperty("app.ui", "javafx");
                        CONFIG.setProperty("app.persistencia", "archivo");
                    }
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo leer la configuración", e);
        }
    }

    public static String ui() {
        return CONFIG.getProperty("app.ui", "javafx")
                     .trim()
                     .toLowerCase(Locale.ROOT);
    }

    public static String getProperty(String key, String defaultValue) {
        return CONFIG.getProperty(key, defaultValue);
    }
}