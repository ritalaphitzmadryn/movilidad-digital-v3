package md.ui.impl.javafx.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import md.modelo.Viaje;
import md.modelo.Usuario;
import md.datos.DAO.ServicioDAO; // O el DAO/Gestor desde donde obtengas los viajes

import java.util.List;

public class PanelHistorialConductorController {

    @FXML
    private ListView<Viaje> listHistorialViajes;

    private String emailConductor;

    /**
     * Recibe el email o datos del conductor logueado actualmente
     */
    public void initData(String emailConductor) {
        this.emailConductor = emailConductor;
        cargarHistorial();
    }

    @FXML
    public void initialize() {
        // Configurar una celda personalizada para mostrar los detalles requeridos de forma limpia
        listHistorialViajes.setCellFactory(param -> new ListCell<Viaje>() {
            @Override
            protected void updateItem(Viaje viaje, boolean empty) {
                super.updateItem(viaje, empty);
                if (empty || viaje == null) {
                    setText(null);
                } else {
                    // Extraer nombre del cliente (asumiendo que Usuario tiene un método getNombre() o similar)
                    String nombreCliente = "Cliente Desconocido";
                    if (viaje.getCliente() != null) {
                        // Ajustá el método según cómo devuelva el nombre tu clase Usuario (ej: getNombre(), getNombreCompleto(), etc.)
                        nombreCliente = viaje.getCliente().toString(); 
                    }

                    // Calcular kilómetros usando el método que ya tiene la clase Viaje/Ubicacion
                    double km = 0.0;
                    if (viaje.getOrigen() != null && viaje.getDestino() != null) {
                        km = viaje.getOrigen().calcularDistancia(viaje.getDestino());
                    }

                    // Formatear el texto que se verá en cada ítem de la lista
                    setText(String.format("👤 Cliente: %s\n📍 Ruta: %s ➔ %s\n📏 Distancia: %.2f km  |  💰 Costo: $%.2f [%s]",
                            nombreCliente,
                            viaje.getOrigen() != null ? viaje.getOrigen().getDireccion() : "Origen",
                            viaje.getDestino() != null ? viaje.getDestino().getDireccion() : "Destino",
                            km,
                            viaje.getCostoCalculado(),
                            viaje.getEstadoActual()
                    ));
                }
            }
        });

        // Carga inicial por defecto (si no se invoca initData antes)
        cargarHistorial();
    }

    private void cargarHistorial() {
        try {
            // 1. Obtener la lista de viajes desde tu DAO o gestor de datos filtrando por el conductor
            // Ejemplo:
            // List<Viaje> listaViajes = ServicioDAO.obtenerViajesDelConductor(emailConductor);
            
            // 2. Convertir a ObservableList y cargar en el ListView
            // ObservableList<Viaje> items = FXCollections.observableArrayList(listaViajes);
            // listHistorialViajes.setItems(items);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverPanel() {
        // Cierra la ventana actual y vuelve al panel principal del conductor
        Stage stage = (Stage) listHistorialViajes.getScene().getWindow();
        stage.close();
    }
}