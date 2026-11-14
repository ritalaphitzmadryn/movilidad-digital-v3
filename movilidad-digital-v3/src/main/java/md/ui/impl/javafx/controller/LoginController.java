package md.ui.impl.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import md.config.DAOLoader;
import md.datos.DAO.DAOFactory;
import md.datos.DAO.UsuariosDAO;
import md.modelo.Usuario;

import java.io.IOException;
import java.util.Map;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private TextField txtNombre;
    @FXML private TextField txtTelefono;
    @FXML private ComboBox<String> cmbRol;

    @FXML
    public void initialize() {
        if (cmbRol != null) {
            cmbRol.getItems().addAll("Cliente (Pasajero)", "Conductor");
            cmbRol.getSelectionModel().selectFirst();
        }
    }

    @FXML
    private void handleIngresar() {
        String texto = txtEmail.getText();
        if (texto == null || texto.trim().isEmpty()) {
            mostrarAlerta("Por favor, ingrese su correo electrónico o nombre.");
            return;
        }
        texto = texto.trim();
        System.out.println("Intentando iniciar sesión con: " + texto);

        try {
            DAOFactory factory = DAOLoader.getFactory();
            UsuariosDAO usuarioDAO = factory.getUsuarioDAO();
            Map<String, Usuario> mapaUsuarios = usuarioDAO.cargarTodos();
            Usuario usuarioEncontrado = null;

            if (mapaUsuarios != null) {
                for (Usuario u : mapaUsuarios.values()) {
                    boolean coincideNombre = u.getNombre() != null && u.getNombre().equalsIgnoreCase(texto);
                    boolean coincideEmail = u.getEmail() != null && u.getEmail().equalsIgnoreCase(texto);
                    if (coincideNombre || coincideEmail) {
                        usuarioEncontrado = u;
                        break;
                    }
                }
            }

            if (usuarioEncontrado != null) {
                System.out.println("¡Usuario encontrado! Bienvenido: " + usuarioEncontrado.getNombre());
                abrirVentanaPrincipal(usuarioEncontrado);
            } else {
                mostrarAlerta("El usuario o correo '" + texto + "' no está registrado en el archivo.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error al buscar el usuario: " + e.getMessage());
        }
    }

    @FXML
    private void handleCrearUsuario() {
        String email = txtEmail != null ? txtEmail.getText().trim() : "";
        String nombre = txtNombre != null ? txtNombre.getText().trim() : "";
        String telefono = txtTelefono != null ? txtTelefono.getText().trim() : "";

        if (email.isEmpty() || nombre.isEmpty() || telefono.isEmpty()) {
            mostrarAlerta("Para registrarse debe completar el correo arriba y el nombre y teléfono abajo.");
            return;
        }

        try {
            DAOFactory factory = DAOLoader.getFactory();
            UsuariosDAO usuarioDAO = factory.getUsuarioDAO();

            // Creamos el usuario adaptado a tu modelo (nombre, id/nombre, teléfono, email)
            Usuario nuevoUsuario = new Usuario(nombre, nombre, telefono, email);
            usuarioDAO.guardar(nuevoUsuario);

            mostrarAlerta("¡Usuario registrado con éxito!");
            abrirVentanaPrincipal(nuevoUsuario);

        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error al guardar el nuevo usuario: " + e.getMessage());
        }
    }

    @FXML
    private void handleSalir() {
        System.exit(0);
    }

    private void abrirVentanaPrincipal(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/vistas/VentanaPrincipal.fxml"));
            Parent root = loader.load();

            // Opcional: Si tu controlador principal recibe el usuario, puedes inyectarlo aquí.

            Stage stage = (Stage) txtEmail.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Movilidad Digital - Panel Principal (" + usuario.getNombre() + ")");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("No se pudo cargar la ventana principal.");
        }
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensaje, ButtonType.OK);
        alert.showAndWait();
    }
}