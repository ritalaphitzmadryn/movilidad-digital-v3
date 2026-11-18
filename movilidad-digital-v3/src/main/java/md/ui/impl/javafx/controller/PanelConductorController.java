package md.ui.impl.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Label;
import md.modelo.Conductor;
import md.modelo.EstadoConductor;
import md.modelo.Usuario;
import md.ui.impl.javafx.JavaFXUserInterface;

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

    @FXML
    private void handleAceptarViaje() {
        if (conductorActual == null) return;

        EstadoConductor estadoActual = conductorActual.getEstadoConductor();

        if (estadoActual == EstadoConductor.DISPONIBLE || estadoActual == EstadoConductor.FUERA_DE_SERVICIO) {
            // FASE 1: Acepta el viaje de la lista
            String viajeSeleccionado = listViajesDisponibles.getSelectionModel().getSelectedItem();
            
            if (viajeSeleccionado != null) {
                System.out.println("¡Viaje aceptado con éxito!: " + viajeSeleccionado);
                
                // Cambia a viaje a origen
                conductorActual.setEstadoConductor(EstadoConductor.VIAJE_A_ORIGEN);
                actualizarTextoEstado(EstadoConductor.VIAJE_A_ORIGEN);
                
                // Removemos el viaje de la lista de disponibles
                listViajesDisponibles.getItems().remove(viajeSeleccionado);
                
                // Cambiamos el texto del botón para guiar al conductor al siguiente paso
                btnAceptarViaje.setText("Confirmar Llegada a Destino / Finalizar");
            } else {
                System.out.println("Seleccioná un viaje de la lista para poder aceptarlo.");
            }
            
        } else if (estadoActual == EstadoConductor.VIAJE_A_ORIGEN) {
            // FASE 2: El conductor llegó al origen y arranca hacia el destino
            conductorActual.setEstadoConductor(EstadoConductor.VIAJE_A_DESTINO);
            actualizarTextoEstado(EstadoConductor.VIAJE_A_DESTINO);
            System.out.println("Trayecto en curso hacia el destino...");
            btnAceptarViaje.setText("Finalizar Viaje");
            
        } else if (estadoActual == EstadoConductor.VIAJE_A_DESTINO) {
            // FASE 3: Se completa el viaje, vuelve a estar disponible y resetea el botón
            conductorActual.setEstadoConductor(EstadoConductor.DISPONIBLE);
            actualizarTextoEstado(EstadoConductor.DISPONIBLE);
            System.out.println("¡Viaje finalizado con éxito! Conductor nuevamente disponible.");
            
            btnAceptarViaje.setText("Aceptar Viaje Seleccionado");
        }
    }
    
    @FXML
    private void handleVerHistorial(javafx.event.ActionEvent event) {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/vistas/PanelHistorialConductor.fxml"));
            javafx.scene.Parent root = loader.load();
            stage.setTitle("Movilidad Digital - Historial de Viajes");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverLogin(javafx.event.ActionEvent event) {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/vistas/Login.fxml"));
            javafx.scene.Parent root = loader.load();
            stage.setTitle("Movilidad Digital - Iniciar Sesión");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}