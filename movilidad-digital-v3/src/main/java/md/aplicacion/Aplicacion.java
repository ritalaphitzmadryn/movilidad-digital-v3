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
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Servicio;
import md.ui.api.IInterfazUsuario; // O UIEngine (contrato común)

public class Aplicacion {

    // Instancia estática o compartida para que la UI gráfica pueda acceder al gestor si lo necesita
    private static GestorMovilidad gestorCompartido;

    public static GestorMovilidad getGestor() {
        return gestorCompartido;
    }

    public static void main(String[] args) {
        try {
            // 1. Seleccionar la fábrica de persistencia.
            DAOFactory factory = DAOLoader.getFactory();

            // 2. Obtener los DAO.
            VehiculosDAO vehiculoDAO = factory.getVehiculoDAO();
            UsuariosDAO usuarioDAO = factory.getUsuarioDAO();
            ServicioDAO servicioDAO = factory.getServicioDAO();

            // 3. Cargar los datos del sistema.
            Map<String, Vehiculo> vehiculos = vehiculoDAO.cargarTodos();
            Map<String, Usuario> usuarios = usuarioDAO.cargarTodos();
            List<Servicio> servicios = servicioDAO.cargarTodos();

            // 4. Crear y poblar el gestor de movilidad.
            GestorMovilidad gestor = new GestorMovilidad();
            gestorCompartido = gestor; // Lo guardamos para que JavaFX lo pueda consultar

            for (Vehiculo vehiculo : vehiculos.values()) {
                gestor.registrarVehiculo(vehiculo);
            }

            for (Usuario usuario : usuarios.values()) {
                gestor.registrarCliente(usuario);
                if (usuario.getConductor() != null) {
                    gestor.registrarConductor(usuario);
                }
            }

            for (Servicio servicio : servicios) {
                gestor.agregarServicio(servicio);
            }

            // 5. Delegamos la creación de la UI en el UILoader (según pide la guía)
            // El UILoader lee el archivo config.properties y devuelve la implementación correspondiente
            IInterfazUsuario ui = UILoader.crearInterfaz(gestor);

            if (ui != null) {
                System.out.println("Iniciando interfaz de usuario...");
                ui.iniciar(); // O ui.start() si renombraste el método al contrato de la guía
            } else {
                System.err.println("Error: No se pudo cargar ninguna interfaz de usuario.");
            }

        } catch (Exception e) {
            System.err.println("Error al iniciar Movilidad Digital: " + e.getMessage());
            e.printStackTrace();
        }
    }
}