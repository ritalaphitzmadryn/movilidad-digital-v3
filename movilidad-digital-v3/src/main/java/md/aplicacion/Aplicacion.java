package md.aplicacion;

import java.util.List;
import java.util.Map;
import md.config.Configuracion;
import md.config.DAOLoader;
import md.datos.DAO.DAOFactory;
import md.datos.DAO.ServicioDAO;
import md.datos.DAO.UsuariosDAO;
import md.datos.DAO.VehiculosDAO;
import md.logica.GestorMovilidad;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Servicio;
import md.ui.impl.cli.MenuPrincipal;
import md.ui.impl.javafx.MainApp;

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

            // 5. Leer la configuración para saber qué interfaz levantar
            Configuracion config = new Configuracion("config.properties");
            String tipoUi = config.getTipoUI();

            // Si no está definido en el archivo, por defecto usamos "javafx"
            if (tipoUi == null || tipoUi.isBlank()) {
                tipoUi = "javafx";
            }
            tipoUi = tipoUi.trim().toLowerCase();

            System.out.println("Configuración detectada - tipoUI = " + tipoUi);

            // 6. Decidir qué interfaz iniciar
            if (tipoUi.equals("cli")) {
                System.out.println("Iniciando interfaz por Consola (CLI)...");
                MenuPrincipal menu = new MenuPrincipal(gestor);
                menu.mostrarMenu();
            } else if (tipoUi.equals("javafx")) {
                System.out.println("Iniciando interfaz gráfica (JavaFX)...");
                javafx.application.Application.launch(MainApp.class, args);
            } else {
                throw new IllegalStateException("Tipo de interfaz no reconocido: " + tipoUi);
            }

        } catch (Exception e) {
            System.err.println("Error al iniciar Movilidad Digital: " + e.getMessage());
            e.printStackTrace();
        }
    } 
} 