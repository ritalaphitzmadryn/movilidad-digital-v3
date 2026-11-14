package md.aplicacion;

import java.util.List;
import java.util.Map;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import md.config.DAOLoader;
import md.datos.DAO.DAOFactory;
import md.datos.DAO.ServicioDAO;
import md.datos.DAO.UsuariosDAO;
import md.datos.DAO.VehiculosDAO;
import md.logica.GestorMovilidad;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Servicio;

public class Aplicacion extends Application {

    // Instancia estática para que las vistas o controladores puedan acceder al gestor
    private static GestorMovilidad gestorCompartido;

    public static GestorMovilidad getGestor() {
        return gestorCompartido;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // 1. Seleccionar la fábrica de persistencia (lee de application.properties -> usuarios.txt)
            DAOFactory factory = DAOLoader.getFactory();

            // 2. Obtener los DAO
            VehiculosDAO vehiculoDAO = factory.getVehiculoDAO();
            UsuariosDAO usuarioDAO = factory.getUsuarioDAO();
            ServicioDAO servicioDAO = factory.getServicioDAO();

            // 3. Cargar los datos del sistema desde los archivos de texto
            Map<String, Vehiculo> vehiculos = vehiculoDAO.cargarTodos();
            Map<String, Usuario> usuarios = usuarioDAO.cargarTodos();
            List<Servicio> servicios = servicioDAO.cargarTodos();

            // 4. Crear y poblar el gestor de movilidad
            GestorMovilidad gestor = new GestorMovilidad();
            gestorCompartido = gestor; 

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

            System.out.println("¡Datos cargados correctamente en el gestor de movilidad!");

            // 5. Cargar la pantalla de Login como ventana inicial
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/vistas/Login.fxml"));
            Parent root = loader.load();

            primaryStage.setTitle("Movilidad Digital - Iniciar Sesión");
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (Exception e) {
            System.err.println("Error crítico al iniciar Movilidad Digital: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}