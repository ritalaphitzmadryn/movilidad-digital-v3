package md.datos;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuracion {

    private Properties propiedades;
    
    public Configuracion(String archivo) {
        propiedades = new Properties();
        
        // Usamos el ClassLoader para leer el archivo desde src/main/resources
        try (InputStream entrada = getClass().getClassLoader().getResourceAsStream(archivo)) {
            
            if (entrada == null) {
                throw new java.io.FileNotFoundException("No se encontró el archivo en los recursos: " + archivo);
            }
            
            propiedades.load(entrada);
        } 
        catch (IOException e) {
            throw new RuntimeException(
                "No se pudo leer el archivo de configuración: " + archivo, e);
        }
    }

    public String getPersistencia() {
        return propiedades.getProperty("persistencia");
    }

    public String getArchivoUsuarios() {
        return propiedades.getProperty("usuario");
    }

    public String getArchivoServicios() {
        return propiedades.getProperty("servicio");
    }

    public String getArchivoVehiculos() {
        return propiedades.getProperty("vehiculo");
    }
    
    public String getUrl() {
        return propiedades.getProperty("db.url");
    }

    public String getUsuarioBd() {
        return propiedades.getProperty("db.user");
    }

    public String getPassword() {
        return propiedades.getProperty("db.password");
    }
    
    public double getLatitud1() {
        return Double.parseDouble(propiedades.getProperty("latitud1"));
    }

    public double getLongitud1() {
        return Double.parseDouble(propiedades.getProperty("longitud1"));
    }

    public double getLatitud2() {
        return Double.parseDouble(propiedades.getProperty("latitud2"));
    }

    public double getLongitud2() {
        return Double.parseDouble(propiedades.getProperty("longitud2"));
    }
}