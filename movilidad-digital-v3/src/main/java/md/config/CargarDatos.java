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

    public CargarDatos(String archivoConfiguracion) {

        Configuracion configuracion =
            new Configuracion(archivoConfiguracion);

        // -------------------------------
        // VEHICULOS
        // -------------------------------

        vehiculos =
            ConfiguracionVehiculos.cargarVehiculos(
                configuracion.getArchivoVehiculos()
            );

        // -------------------------------
        // SERVICIOS
        // -------------------------------

        servicios =
            ConfiguracionServicios.cargarServicios(
                configuracion.getArchivoServicios()
            );

        // -------------------------------
        // USUARIOS
        // -------------------------------

        usuarios =
            ConfiguracionUsuarios.cargarUsuarios(
                configuracion.getArchivoUsuarios(),
                vehiculos
            );
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