package md.ui.impl.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import md.modelo.RolUsuario;
import md.modelo.Usuario;

import java.net.URL;
import java.util.ResourceBundle;

public class PanelClienteController implements Initializable {

    @FXML
    private Label lblUsuarioCliente;

    @FXML
    private TextField txtOrigenCliente;

    @FXML
    private TextField txtDestinoCliente;

    @FXML
    private Button btnSolicitarViajeCliente;

    @FXML
    private ListView<String> listMisViajes;

    private Usuario usuarioActual;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Inicializando PanelClienteController...");
        cargarHistorialViajes();
    }

    // Método para recibir el usuario autenticado desde el Login o ventana principal
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        if (this.usuarioActual != null) {
            if (lblUsuarioCliente != null) {
                lblUsuarioCliente.setText("Cliente: " + usuarioActual.getNombre());
            }
            System.out.println("Panel de cliente cargado para: " + usuarioActual.getNombre());
        }
    }

    @FXML
    private void handleSolicitarViajeCliente() {
        String origen = txtOrigenCliente != null ? txtOrigenCliente.getText() : "";
        String destino = txtDestinoCliente != null ? txtDestinoCliente.getText() : "";

        if (!origen.isEmpty() && !destino.isEmpty()) {
            System.out.println("Cliente solicita viaje de: " + origen + " a " + destino);
            
            if (listMisViajes != null) {
                listMisViajes.getItems().add(0, "En curso: " + origen + " ➔ " + destino);
            }
            
            if (txtOrigenCliente != null) txtOrigenCliente.clear();
            if (txtDestinoCliente != null) txtDestinoCliente.clear();
        } else {
            System.out.println("Por favor, completá tanto el origen como el destino.");
        }
    }

    private void cargarHistorialViajes() {
        if (listMisViajes != null) {
            listMisViajes.getItems().addAll(
                "Completado: Terminal ➔ Centro (⭐⭐⭐⭐⭐)",
                "Completado: UNPSJB ➔ Costanera (⭐⭐⭐⭐☆)"
            );
        }
    }
}