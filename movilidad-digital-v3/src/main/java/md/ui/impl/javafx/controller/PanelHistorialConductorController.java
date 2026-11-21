package md.ui.impl.javafx.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import md.modelo.Usuario;
import md.modelo.Viaje;
import java.util.List;

public class PanelHistorialConductorController {

    @FXML
    private ListView<Viaje> listHistorialViajes;

    private String emailConductor;
    private Usuario usuarioActual;

    public void setUsuarioActual(Usuario usuarioActual) {
        this.usuarioActual = usuarioActual;
    }

    /**
     * Recibe el email del conductor logueado actualmente y carga su historial
     */
    public void initData(String emailConductor) {
        this.emailConductor = emailConductor;
        cargarHistorial();
    }

    @FXML
    public void initialize() {
        listHistorialViajes.setCellFactory(param -> new ListCell<Viaje>() {
            @Override
            protected void updateItem(Viaje viaje, boolean empty) {
                super.updateItem(viaje, empty);
                if (empty || viaje == null) {
                    setText(null);
                } else {
                    String textoCliente = "Cliente Registrado";
                    if (viaje.getCliente() != null) {
                        String str = viaje.getCliente().toString();
                        if (str != null && !str.contains("@")) {
                            textoCliente = str;
                        }
                    }

                    double km = 0.0;
                    if (viaje.getOrigen() != null && viaje.getDestino() != null) {
                        km = viaje.getOrigen().calcularDistancia(viaje.getDestino());
                    }

                    String origenTexto = viaje.getOrigen() != null ? viaje.getOrigen().getDireccion() : "Origen";
                    String destinoTexto = viaje.getDestino() != null ? viaje.getDestino().getDireccion() : "";

                    String rutaTexto = destinoTexto.isEmpty() || "Destino Registrado".equals(destinoTexto)
                            ? origenTexto
                            : origenTexto + " ➔ " + destinoTexto;

                    setText(String.format("👤 %s\n📍 Ruta: %s\n📏 Distancia: %.2f km  |  💰 Costo: $%.2f [%s]",
                            textoCliente,
                            rutaTexto,
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

            List<Viaje> historial = dao.cargarHistorialPorConductor(emailConductor);

            ObservableList<Viaje> items = FXCollections.observableArrayList(historial);
            listHistorialViajes.setItems(items);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverPanel() {
        try {
            java.net.URL url = getClass().getResource("/ui/javafx/vistas/PanelConductor.fxml");
            if (url == null) {
                System.err.println("No se encontró el FXML del panel.");
                return;
            }
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(url, java.util.ResourceBundle.getBundle("ui.i18n.labels"));
            javafx.scene.Parent root = loader.load();

            PanelConductorController panelController = loader.getController();
            if (panelController != null && usuarioActual != null) {
                panelController.setUsuarioActual(usuarioActual);
            }

            Stage stage = (Stage) listHistorialViajes.getScene().getWindow();
            stage.setTitle("Movilidad Digital - Panel de Conductor");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}