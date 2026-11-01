package md.ui.impl.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class PanelConductorController implements Initializable {

    @FXML
    private Label lblEstadoConductor;

    @FXML
    private ToggleButton tglDisponibilidad;

    @FXML
    private ListView<String> listViajesDisponibles;

    @FXML
    private Button btnAceptarViaje;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Inicializando PanelConductorController...");
        
        // Estado inicial
        tglDisponibilidad.setSelected(false);
        actualizarTextoEstado(false);
        
        // Cargar algunos viajes de prueba pendientes
        cargarViajesPendientes();
    }

    @FXML
    private void handleCambiarDisponibilidad() {
        boolean activo = tglDisponibilidad.isSelected();
        actualizarTextoEstado(activo);
        
        if (activo) {
            System.out.println("Conector en línea. Recibiendo alertas de viajes...");
        } else {
            System.out.println("Conductor desconectado.");
        }
    }

    @FXML
    private void handleAceptarViaje() {
        String viajeSeleccionado = listViajesDisponibles.getSelectionModel().getSelectedItem();
        
        if (viajeSeleccionado != null) {
            System.out.println("¡Viaje aceptado con éxito!: " + viajeSeleccionado);
            
            // Removemos el viaje de la lista de disponibles
            listViajesDisponibles.getItems().remove(viajeSeleccionado);
        } else {
            System.out.println("Seleccioná un viaje de la lista para poder aceptarlo.");
        }
    }

    private void actualizarTextoEstado(boolean activo) {
        if (activo) {
            tglDisponibilidad.setText("Disponible");
            if (lblEstadoConductor != null) lblEstadoConductor.setText("Estado: En servicio");
        } else {
            tglDisponibilidad.setText("No Disponible");
            if (lblEstadoConductor != null) lblEstadoConductor.setText("Estado: Desconectado");
        }
    }

    private void cargarViajesPendientes() {
        // Datos de ejemplo para poblar la vista del conductor
        listViajesDisponibles.getItems().addAll(
            "Viaje #1: Terminal ➔ Centro (Aprox. 2.5 km)",
            "Viaje #2: UNPSJB ➔ Costanera (Aprox. 4.0 km)"
        );
    }
}