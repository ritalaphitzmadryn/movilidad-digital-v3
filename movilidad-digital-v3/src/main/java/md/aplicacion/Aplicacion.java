package md.aplicacion;

import java.util.List;
import java.util.Map;

import md.config.DAOLoader;
import md.config.UILoader;
import md.datos.DAO.DAOFactory;
import md.datos.DAO.ServicioDAO;
import md.datos.DAO.UsuariosDAO;
import md.datos.DAO.VehiculosDAO;
import md.logica.GestorMovilidad;
import md.modelo.Servicio;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.ui.api.IInterfazUsuario;

public class Aplicacion {

    // Instancia estática para que las vistas o controladores puedan acceder al gestor
    private static GestorMovilidad gestorCompartido;

    public static GestorMovilidad getGestor() {
        return gestorCompartido;
    }

    public static void main(String[] args) {
        try {
            // 1. Seleccionar la fábrica de persistencia (lee de application.properties)
            DAOFactory factory = DAOLoader.getFactory();

            // 2. Obtener los DAO
            VehiculosDAO vehiculoDAO = factory.getVehiculoDAO();
            UsuariosDAO usuarioDAO = factory.getUsuarioDAO();
            ServicioDAO servicioDAO = factory.getServicioDAO();

            // 3. Cargar los datos del sistema
            Map<String, Vehiculo> vehiculos = vehiculoDAO.cargarTodos();
            Map<String, Usuario> usuarios = usuarioDAO.cargarTodos();
            List<Servicio> servicios = servicioDAO.cargarTodos();

            // 4. Crear y poblar el gestor de movilidad
            GestorMovilidad gestor = new GestorMovilidad();

            if (vehiculos != null) {
                for (Vehiculo vehiculo : vehiculos.values()) {
                    gestor.registrarVehiculo(vehiculo);
                }
            }

            if (usuarios != null) {
                for (Usuario usuario : usuarios.values()) {
                    gestor.registrarCliente(usuario);
                    if (usuario.getConductor() != null) {
                        gestor.registrarConductor(usuario);
                    }
                }
            }

            if (servicios != null) {
                for (Servicio servicio : servicios) {
                    gestor.agregarServicio(servicio);
                }
            }

            // Se asigna una vez terminado el poblado inicial
            gestorCompartido = gestor;
            System.out.println("¡Datos cargados correctamente en el gestor de movilidad!");

            // 5. Crear la interfaz mediante la fábrica UILoader (cli / javafx)
            IInterfazUsuario interfaz = UILoader.crearInterfaz(gestor);
            interfaz.iniciar(); // Verificá el nombre del método en tu IInterfazUsuario

        } catch (Exception e) {
            System.err.println("Error crítico al iniciar Movilidad Digital: " + e.getMessage());
            e.printStackTrace();
        }
    }
}