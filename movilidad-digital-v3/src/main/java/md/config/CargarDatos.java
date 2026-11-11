package md.config;

import java.util.List;
import java.util.Map;
import md.modelo.Servicio;
import md.modelo.Usuario;
import md.modelo.Vehiculo;

public class CargarDatos {

    private Map<String, Usuario> usuarios;
    private Map<String, Vehiculo> vehiculos;
    private List<Servicio> servicios;

    public CargarDatos() {
        // Como CargaParametros ahora lee estáticamente application.properties,
        // no necesitamos pasarle nombres de archivos por parámetro en el constructor.
        
        try {
            // -------------------------------
            // VEHICULOS
            // -------------------------------
            String archivoVehiculos = CargaParametros.getProperty("app.vehiculos", "vehiculos.json"); // Ajustá la key según tu properties
            vehiculos = ConfiguracionVehiculos.cargarVehiculos(archivoVehiculos);

            // -------------------------------
            // SERVICIOS
            // -------------------------------
            String archivoServicios = CargaParametros.getProperty("app.servicios", "servicios.json");
            servicios = ConfiguracionServicios.cargarServicios(archivoServicios);

            // -------------------------------
            // USUARIOS
            // -------------------------------
            String archivoUsuarios = CargaParametros.getProperty("app.usuarios", "usuarios.json");
            usuarios = ConfiguracionUsuarios.cargarUsuarios(archivoUsuarios, vehiculos);

        } catch (Exception e) {
            System.err.println("Error al cargar los datos de configuración: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<String, Usuario> getUsuarios() {
        return usuarios;
    }

    public Map<String, Vehiculo> getVehiculos() {
        return vehiculos;
    }

    public List<Servicio> getServicios() {
        return servicios;
    }
}