package md.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import md.modelo.CategoriaVehiculo;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import md.modelo.Vehiculo;

public class ConfiguracionVehiculos {

    public static Map<String, Vehiculo> cargarVehiculos(String archivo) {
        Map<String, Vehiculo> vehiculos = new HashMap<>();
        try (BufferedReader lector = new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#")) {
                    continue;
                }
                String[] datos = linea.split(";");
                String patente = datos[0].trim();
                String modelo = datos[1].trim();

                int capacidad = Integer.parseInt(datos[2].trim());

                TipoVehiculo tipoVehiculo = TipoVehiculo.valueOf(datos[3].trim().toUpperCase());

                CategoriaVehiculo categoria = CategoriaVehiculo.valueOf(datos[4].trim().toUpperCase());

                Vehiculo vehiculo = new Vehiculo(patente,modelo,capacidad, tipoVehiculo, categoria);

                // Desde la posición 5 están los tipos de servicio
                for (int i = 5; i < datos.length; i++) {

                    if (!datos[i].trim().isEmpty()) {

                        TipoServicio tipoServicio =
                            TipoServicio.valueOf(
                                datos[i].trim().toUpperCase()
                            );

                        vehiculo.agregarTipoServicio(
                            tipoServicio
                        );
                    }
                }

                vehiculos.put(patente, vehiculo);
            }

        } catch (IOException e) {

            throw new RuntimeException(
                "Error al leer vehículos: " + archivo,
                e
            );
        }

        return vehiculos;
    }
}