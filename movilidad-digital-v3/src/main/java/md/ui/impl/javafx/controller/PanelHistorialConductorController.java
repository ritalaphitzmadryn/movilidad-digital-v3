package md.ui.impl.javafx.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import md.modelo.Viaje;
import md.modelo.Usuario;

import java.util.List;

public class PanelHistorialConductorController {

    @FXML
    private ListView<Viaje> listHistorialViajes;

    private String emailConductor;

    /**
     * Recibe el email del conductor logueado actualmente y carga su historial
     */
    public void initData(String emailConductor) {
        this.emailConductor = emailConductor; // 1. Guardamos el email correctamente
        cargarHistorial();                     // 2. Disparamos la carga una vez que tenemos el email
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
                    String nombreCliente = "Cliente Desconocido";
                    if (viaje.getCliente() != null) {
                        nombreCliente = viaje.getCliente().toString(); 
                    }

                    double km = 0.0;
                    if (viaje.getOrigen() != null && viaje.getDestino() != null) {
                        km = viaje.getOrigen().calcularDistancia(viaje.getDestino());
                    }

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
    }

    private void cargarHistorial() {
        try {
            if (emailConductor == null) return;

            String url = "jdbc:postgresql://pgs.fi.mdn.unp.edu.ar:30000/bd1";
            String user = "estudiante";
            String password = "bd1estudiante";
            
            md.datos.DAO.bd.ConexionBD conexion = new md.datos.DAO.bd.ConexionBD(url, user, password);
            md.datos.DAO.bd.ServicioDaoBd dao = new md.datos.DAO.bd.ServicioDaoBd(conexion);
            
            // 3. Obtenemos la lista desde la base de datos
            List<Viaje> historial = dao.cargarHistorialPorConductor(emailConductor);
            
            // 4. Convertimos a ObservableList y cargamos en el ListView
            ObservableList<Viaje> items = FXCollections.observableArrayList(historial);
            listHistorialViajes.setItems(items);

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