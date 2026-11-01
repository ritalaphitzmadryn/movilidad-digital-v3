
package md.aplicacion;

import java.util.List;
import java.util.Map;

import md.config.DAOLoader;
import md.datos.DAO.DAOFactory;
import md.datos.DAO.ServicioDAO;
import md.datos.DAO.UsuariosDAO;
import md.datos.DAO.VehiculosDAO;
import md.impl.cli.MenuPrincipal;
import md.logica.GestorMovilidad;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Servicio;

public class Aplicacion {

    public static void main(String[] args) {

        try {
            // 1. Seleccionar la fábrica de persistencia.
            DAOFactory factory = DAOLoader.getFactory();

            // 2. Obtener los DAO.
            VehiculosDAO vehiculoDAO = factory.getVehiculoDAO();
            UsuariosDAO usuarioDAO = factory.getUsuarioDAO();
            ServicioDAO servicioDAO = factory.getServicioDAO();

            // 3. Cargar los vehículos primero.
            Map<String, Vehiculo> vehiculos =
                    vehiculoDAO.cargarTodos();

            // 4. Cargar los usuarios.
            // El DAO debe reconstruir las asociaciones entre
            // los conductores y los vehículos cargados.
            Map<String, Usuario> usuarios =
                    usuarioDAO.cargarTodos();

            // 5. Cargar los servicios.
            List<Servicio> servicios =
                    servicioDAO.cargarTodos();

            // 6. Crear el gestor.
            GestorMovilidad gestor = new GestorMovilidad();

            // 7. Registrar los vehículos.
            for (Vehiculo vehiculo : vehiculos.values()) {
                gestor.registrarVehiculo(vehiculo);
            }

            // 8. Registrar usuarios y perfiles.
            for (Usuario usuario : usuarios.values()) {
                gestor.registrarCliente(usuario);

                if (usuario.getConductor() != null) {
                    gestor.registrarConductor(usuario);
                }
            }

            // 9. Registrar servicios.
            for (Servicio servicio : servicios) {
                gestor.agregarServicio(servicio);
            }

            // 10. Iniciar la interfaz.
            MenuPrincipal menu = new MenuPrincipal(gestor);
            menu.mostrarMenu();

        } catch (Exception e) {
            System.err.println(
                    "Error al iniciar Movilidad Digital: "
                    + e.getMessage()
            );
            e.printStackTrace();
        }
    }
}
