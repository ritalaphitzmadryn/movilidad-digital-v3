package md.config;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import md.modelo.Usuario;
import md.modelo.Vehiculo;

public class ConfiguracionUsuarios {
    public static Map<String, Usuario> cargarUsuarios(String archivo,Map<String, Vehiculo> vehiculos) {
        Map<String, Usuario> usuarios = new HashMap<>();
        try (BufferedReader lector =
                new BufferedReader(new FileReader(archivo))) {
            String linea;
            while ((linea = lector.readLine()) != null) {
                linea = linea.trim();
 // Ignorar líneas vacías y comentarios
             if (linea.isEmpty() || linea.startsWith("#")) {
                  continue;
                }
             String[] datos = linea.split(";");
                // Datos básicos del usuario
             String nombre = datos[0].trim();
             String telefono = datos[1].trim();
             String email = datos[2].trim();
// ----------------------------------
// USUARIO / CLIENTE
// ----------------------------------

              // Por ahora usamos el nombre como ID
            Usuario usuario = new Usuario(nombre,nombre,telefono,email);

// ----------------------------------
// CONDUCTOR
// ----------------------------------

              // Si tiene licencia, también es conductor
              if (datos.length > 3 && !datos[3].trim().isEmpty()) {
                    String licencia = datos[3].trim();
              // Primera patente
              Vehiculo primerVehiculo = null;
              if (datos.length > 4 &&
                    !datos[4].trim().isEmpty()) {
                    String patente = datos[4].trim();
                    primerVehiculo = vehiculos.get(patente);
                    }
              // Registrar perfil de conductor
              usuario.altaConductor(licencia,primerVehiculo);
// ----------------------------------
// VEHÍCULOS ADICIONALES
// ----------------------------------
              for (int i = 5; i < datos.length; i++) {
                   String patente = datos[i].trim();
                   if (!patente.isEmpty()) {
                          Vehiculo vehiculo = vehiculos.get(patente);
                          if (vehiculo != null) {
                              usuario.getConductor().agregarVehiculo(vehiculo);
                            }
                        }
                    }
                }

// ----------------------------------
// GUARDAR USUARIO
// ----------------------------------
                usuarios.put(usuario.getId(), usuario);
            }
        } catch (IOException e) {

            throw new RuntimeException(
                    "Error al leer usuarios: " + archivo,e);
        }
        return usuarios;
    }
}