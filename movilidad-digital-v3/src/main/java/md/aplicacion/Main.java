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

public class Main {

    public static void main(String[] args) {
        try {
            GestorMovilidad gestor = inicializarGestor();
            IInterfazUsuario ui = UILoader.crearInterfaz(gestor);
            ui.iniciar();
        } catch (Exception e) {
            System.err.println("Error crítico al iniciar Movilidad Digital: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static GestorMovilidad inicializarGestor() throws Exception {
        DAOFactory factory = DAOLoader.getFactory();

        VehiculosDAO vehiculoDAO = factory.getVehiculoDAO();
        UsuariosDAO usuarioDAO = factory.getUsuarioDAO();
        ServicioDAO servicioDAO = factory.getServicioDAO();

        Map<String, Vehiculo> vehiculos = vehiculoDAO.cargarTodos();
        Map<String, Usuario> usuarios = usuarioDAO.cargarTodos();
        List<Servicio> servicios = servicioDAO.cargarTodos();

        GestorMovilidad gestor = new GestorMovilidad();

        if (vehiculos != null) {
            for (Vehiculo v : vehiculos.values()) {
                gestor.registrarVehiculo(v);
            }
        }

        if (usuarios != null) {
            for (Usuario u : usuarios.values()) {
                gestor.registrarCliente(u);
                if (u.getConductor() != null) {
                    gestor.registrarConductor(u);
                }
            }
        }

        if (servicios != null) {
            for (Servicio s : servicios) {
                gestor.agregarServicio(s);
            }
        }

        return gestor;
    }
}