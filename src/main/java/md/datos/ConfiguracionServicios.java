package md.datos;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import md.modelo.CategoriaVehiculo;
import md.modelo.Servicio;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;

public class ConfiguracionServicios {

    public static List<Servicio> cargarServicios(String archivo) {
        List<Servicio> servicios = new ArrayList<>();
        
        // Usamos el ClassLoader para leer el archivo desde src/main/resources
        try (InputStream inputStream = ConfiguracionServicios.class.getClassLoader().getResourceAsStream(archivo)) {
            
            if (inputStream == null) {
                throw new java.io.FileNotFoundException("No se encontró el archivo en los recursos: " + archivo);
            }
            
            try (BufferedReader lector = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                String linea;
                while ((linea = lector.readLine()) != null) {
                    linea = linea.trim();
                    if (linea.isEmpty() || linea.startsWith("#")) {
                        continue;
                    }
                    String[] datos = linea.split(";");
                    String nombre = datos[0].trim();

                    double tarifaBase = Double.parseDouble(datos[1].trim());
                    double precioKm = Double.parseDouble(datos[2].trim());
                    double precioMinuto = Double.parseDouble(datos[3].trim());

                    TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(datos[4].trim().toUpperCase());
                    CategoriaVehiculo categoria = CategoriaVehiculo.valueOf(datos[5].trim().toUpperCase());
                    TipoServicio tipoServicio = TipoServicio.valueOf(datos[6].trim().toUpperCase());
                    
                    Servicio servicio = new Servicio(nombre, tipoServicio, tipoVehiculo, categoria, tarifaBase, precioKm, precioMinuto);
                    servicios.add(servicio);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(
                "Error al leer servicios: " + archivo,
                e
            );
        }

        return servicios;
    }
}