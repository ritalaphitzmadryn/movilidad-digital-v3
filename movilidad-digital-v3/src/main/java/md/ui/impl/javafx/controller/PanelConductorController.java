package md.ui.impl.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Label;
import md.modelo.Conductor;
import md.modelo.EstadoConductor;
import md.modelo.Usuario;

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

    private Usuario usuarioActual;
    private Conductor conductorActual;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Inicializando PanelConductorController...");
        
        // Estado inicial por defecto
        if (tglDisponibilidad != null) {
            tglDisponibilidad.setSelected(false);
        }
        actualizarTextoEstado(EstadoConductor.FUERA_DE_SERVICIO);
        
        // Cargar algunos viajes de prueba pendientes
        cargarViajesPendientes();
    }

    // Método para recibir el usuario activo y asociar su perfil de conductor
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        if (this.usuarioActual != null && this.usuarioActual.getConductor() != null) {
            this.conductorActual = this.usuarioActual.getConductor();
            
            // Sincronizar UI con el estado real del conductor del modelo
            EstadoConductor estadoActual = conductorActual.getEstadoConductor();
            if (estadoActual == null) {
                estadoActual = EstadoConductor.FUERA_DE_SERVICIO;
                conductorActual.setEstadoConductor(estadoActual);
            }
            
            actualizarTextoEstado(estadoActual);
            tglDisponibilidad.setSelected(estadoActual == EstadoConductor.DISPONIBLE);
        }
    }

    @FXML
    private void handleCambiarDisponibilidad() {
        boolean activo = tglDisponibilidad.isSelected();
        EstadoConductor nuevoEstado = activo ? EstadoConductor.DISPONIBLE : EstadoConductor.FUERA_DE_SERVICIO;
        
        // Actualizar en el objeto Conductor del modelo
        if (conductorActual != null) {
            conductorActual.setEstadoConductor(nuevoEstado);
        }
        
        actualizarTextoEstado(nuevoEstado);
        
        if (activo) {
            System.out.println("Conductor en línea. Estado: DISPONIBLE");
        } else {
            System.out.println("Conductor desconectado. Estado: FUERA_DE_SERVICIO");
        }
    }

    @FXML
    private void handleAceptarViaje() {
        String viajeSeleccionado = listViajesDisponibles.getSelectionModel().getSelectedItem();
        
        if (viajeSeleccionado != null) {
            System.out.println("¡Viaje aceptado con éxito!: " + viajeSeleccionado);
            
            // Al aceptar el viaje, el conductor pasa automáticamente a VIAJE_A_ORIGEN según el diagrama
            if (conductorActual != null) {
                conductorActual.setEstadoConductor(EstadoConductor.VIAJE_A_ORIGEN);
                actualizarTextoEstado(EstadoConductor.VIAJE_A_ORIGEN);
            }
            
            // Removemos el viaje de la lista de disponibles
            listViajesDisponibles.getItems().remove(viajeSeleccionado);
        } else {
            System.out.println("Seleccioná un viaje de la lista para poder aceptarlo.");
        }
    }

    private void actualizarTextoEstado(EstadoConductor estado) {
        if (lblEstadoConductor == null) return;
        
        switch (estado) {
            case DISPONIBLE:
                tglDisponibilidad.setText("Disponible");
                lblEstadoConductor.setText("Estado: DISPONIBLE");
                lblEstadoConductor.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                break;
            case FUERA_DE_SERVICIO:
                tglDisponibilidad.setText("No Disponible");
                lblEstadoConductor.setText("Estado: FUERA_DE_SERVICIO");
                lblEstadoConductor.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                break;
            case VIAJE_A_ORIGEN:
                tglDisponibilidad.setText("En Servicio (Origen)");
                lblEstadoConductor.setText("Estado: VIAJE_A_ORIGEN");
                lblEstadoConductor.setStyle("-fx-text-fill: #ef6c00; -fx-font-weight: bold;");
                break;
            case VIAJE_A_DESTINO:
                tglDisponibilidad.setText("En Servicio (Destino)");
                lblEstadoConductor.setText("Estado: VIAJE_A_DESTINO");
                lblEstadoConductor.setStyle("-fx-text-fill: #1565c0; -fx-font-weight: bold;");
                break;
        }
    }

    private void cargarViajesPendientes() {
        if (listViajesDisponibles != null) {
            listViajesDisponibles.getItems().addAll(
                "Viaje #1: Terminal ➔ Centro (Aprox. 2.5 km)",
                "Viaje #2: UNPSJB ➔ Costanera (Aprox. 4.0 km)"
            );
        }
    }
}