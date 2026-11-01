package md.ui.impl.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Inicializando PanelClienteController...");
        cargarHistorialViajes();
    }

    @FXML
    private void handleSolicitarViajeCliente() {
        String origen = txtOrigenCliente.getText();
        String destino = txtDestinoCliente.getText();

        if (origen != null && !origen.isEmpty() && destino != null && !destino.isEmpty()) {
            System.out.println("Cliente solicita viaje de: " + origen + " a " + destino);
            
            // Agregamos un elemento visual de prueba a la lista de viajes
            listMisViajes.getItems().add("Solicitado: " + origen + " ➔ " + destino);
            
            // Limpiamos los campos
            txtOrigenCliente.clear();
            txtDestinoCliente.clear();
        } else {
            System.out.println("Por favor, completá tanto el origen como el destino.");
        }
    }

    private void cargarHistorialViajes() {
        // Datos de ejemplo para poblar la lista inicial del historial
        listMisViajes.getItems().addAll(
            "Completado: Terminal ➔ Centro",
            "Completado: UNPSJB ➔ Costanera"
        );
    }
}