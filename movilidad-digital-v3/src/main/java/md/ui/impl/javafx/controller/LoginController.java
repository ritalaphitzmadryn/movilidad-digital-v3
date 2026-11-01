package md.ui.impl.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private TextField txtUsuario;

    @FXML
    private Button btnIngresar;

    @FXML
    private void handleLogin() {
        String usuario = txtUsuario.getText();

        System.out.println("Intentando iniciar sesión con usuario: " + usuario);

        if (usuario != null && !usuario.isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/vistas/VentanaPrincipal.fxml"));
                Parent root = loader.load();

                Stage stage = (Stage) btnIngresar.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Movilidad Digital - Panel Principal");
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("No se pudo cargar la ventana principal.");
            }
        } else {
            System.out.println("Por favor, ingrese un usuario válido.");
        }
    }
}