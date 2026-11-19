package md.ui.impl.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Label;
import md.modelo.Conductor;
import md.modelo.EstadoConductor;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Viaje;

import java.net.URL;
import java.util.Locale;
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

    // Datos del viaje en curso para el resumen al finalizar
    private String origenActual = "Terminal / Origen";
    private String destinoActual = "Centro / Destino";
    private double distanciaActualKm = 2.5;
    private double costoActual = 0.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Inicializando PanelConductorController...");

        if (tglDisponibilidad != null) {
            tglDisponibilidad.setSelected(false);
        }
        actualizarTextoEstado(EstadoConductor.FUERA_DE_SERVICIO);
        cargarViajesPendientes();
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        if (this.usuarioActual != null && this.usuarioActual.getConductor() != null) {
            this.conductorActual = this.usuarioActual.getConductor();

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
                if (tglDisponibilidad != null) tglDisponibilidad.setText("Disponible");
                lblEstadoConductor.setText("Estado: DISPONIBLE");
                lblEstadoConductor.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                break;
            case FUERA_DE_SERVICIO:
                if (tglDisponibilidad != null) tglDisponibilidad.setText("No Disponible");
                lblEstadoConductor.setText("Estado: FUERA_DE_SERVICIO");
                lblEstadoConductor.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                break;
            case VIAJE_A_ORIGEN:
                if (tglDisponibilidad != null) tglDisponibilidad.setText("En Servicio (Origen)");
                lblEstadoConductor.setText("Estado: VIAJE_A_ORIGEN");
                lblEstadoConductor.setStyle("-fx-text-fill: #ef6c00; -fx-font-weight: bold;");
                break;
            case VIAJE_A_DESTINO:
                if (tglDisponibilidad != null) tglDisponibilidad.setText("En Servicio (Destino)");
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
    private double[] obtenerCoordenadasConocidas(String lugar) {
        if (lugar == null) return null;
        String l = lugar.toLowerCase(Locale.ROOT);
        if (l.contains("unpsjb")) {
            return new double[]{-42.7856, -65.0056};
        }
        if (l.contains("costanera")) {
            return new double[]{-42.7685, -65.0320};
        }
        if (l.contains("terminal")) {
            return new double[]{-42.7758, -65.0525};
        }
        if (l.contains("centro")) {
            return new double[]{-42.7692, -65.0385};
        }
        return null;
    }

    private void extraerDatosViajeSeleccionado(String textoViaje) {
        if (textoViaje == null || textoViaje.isBlank()) {
            origenActual = "Origen del viaje";
            destinoActual = "Destino del viaje";
            distanciaActualKm = 0.0;
            costoActual = 0.0;
            return;
        }

        String limpio = textoViaje.replaceFirst("^Viaje #\\d+:\\s*", "");
        String distanciaParte = "";
        int idxPar = limpio.indexOf('(');
        if (idxPar != -1) {
            distanciaParte = limpio.substring(idxPar + 1).replace(")", "").trim();
            limpio = limpio.substring(0, idxPar).trim();
        }

        String[] tramos = limpio.split("\\s*➔\\s*");
        if (tramos.length >= 2) {
            origenActual = tramos[0].trim();
            destinoActual = tramos[1].trim();
        } else {
            origenActual = limpio;
            destinoActual = "Destino";
        }

        distanciaActualKm = 0.0;
        if (!distanciaParte.isEmpty()) {
            String numStr = distanciaParte.replaceAll("[^0-9.]", "");
            if (!numStr.isEmpty()) {
                try {
                    distanciaActualKm = Double.parseDouble(numStr);
                } catch (NumberFormatException ignored) {
                    distanciaActualKm = 0.0;
                }
            }
        }

        // Tarifa estimada de respaldo si no hay cálculo externo previo
        costoActual = 2000.0 + (distanciaActualKm * 200.0);
    }

    @FXML
    private void handleAceptarViaje() {
        if (conductorActual == null) return;

        EstadoConductor estadoActual = conductorActual.getEstadoConductor();

        if (estadoActual == EstadoConductor.DISPONIBLE || estadoActual == EstadoConductor.FUERA_DE_SERVICIO) {
            String viajeSeleccionado = listViajesDisponibles != null
                    ? listViajesDisponibles.getSelectionModel().getSelectedItem()
                    : null;

            if (viajeSeleccionado == null && listViajesDisponibles != null && !listViajesDisponibles.getItems().isEmpty()) {
                viajeSeleccionado = listViajesDisponibles.getItems().get(0);
                System.out.println("AVISO: No habías seleccionado un viaje, se tomó el primero automáticamente.");
            }

            if (viajeSeleccionado != null) {
                extraerDatosViajeSeleccionado(viajeSeleccionado);
                System.out.println("¡Viaje aceptado con éxito!: " + viajeSeleccionado);

                conductorActual.setEstadoConductor(EstadoConductor.VIAJE_A_ORIGEN);
                actualizarTextoEstado(EstadoConductor.VIAJE_A_ORIGEN);

                if (listViajesDisponibles != null) {
                    listViajesDisponibles.getItems().remove(viajeSeleccionado);
                }

                if (btnAceptarViaje != null) {
                    btnAceptarViaje.setText("Confirmar Llegada a Destino / Finalizar");
                }
            } else {
                System.out.println("No hay viajes disponibles en la lista para aceptar.");
            }

        } else if (estadoActual == EstadoConductor.VIAJE_A_ORIGEN) {
            conductorActual.setEstadoConductor(EstadoConductor.VIAJE_A_DESTINO);
            actualizarTextoEstado(EstadoConductor.VIAJE_A_DESTINO);
            System.out.println("Trayecto en curso hacia el destino...");
            if (btnAceptarViaje != null) {
                btnAceptarViaje.setText("Finalizar Viaje");
            }

        } else if (estadoActual == EstadoConductor.VIAJE_A_DESTINO) {
            finalizarYMostrarResumen();
        }
    }

    @FXML
    private void handleFinalizarViaje() {
        finalizarYMostrarResumen();
    }

    private void finalizarYMostrarResumen() {
        if (conductorActual != null) {
            conductorActual.setEstadoConductor(EstadoConductor.DISPONIBLE);
            actualizarTextoEstado(EstadoConductor.DISPONIBLE);
        }
        System.out.println("¡Viaje finalizado con éxito! Conductor nuevamente disponible.");

        try {
            if (usuarioActual != null) {
                String url = "jdbc:postgresql://pgs.fi.mdn.unp.edu.ar:30000/bd1";
                String user = "estudiante";
                String password = "bd1estudiante";

                md.datos.DAO.bd.ConexionBD conexion = new md.datos.DAO.bd.ConexionBD(url, user, password);
                md.datos.DAO.bd.ServicioDaoBd dao = new md.datos.DAO.bd.ServicioDaoBd(conexion);

                Ubicacion origen = new Ubicacion(origenActual, 0.0, 0.0);
                Ubicacion destino = new Ubicacion(destinoActual, 0.0, 0.0);
                Viaje viajeTerminado = new Viaje(null, null, origen, destino);

                dao.guardarViajeRealizado(viajeTerminado, usuarioActual.getEmail());
                System.out.println("💾 [DAO] ¡Viaje guardado exitosamente en la tabla de servicios!");
            } else {
                System.out.println("AVISO: usuarioActual es NULL, no se pudo guardar el registro.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (btnAceptarViaje != null) {
            btnAceptarViaje.setText("Aceptar Viaje Seleccionado");
        }

        // 1. Mensaje "Viaje Finalizado!"
        Alert alertaFinalizado = new Alert(Alert.AlertType.INFORMATION);
        alertaFinalizado.setTitle("Estado del Viaje");
        alertaFinalizado.setHeaderText("Viaje Finalizado!");
        alertaFinalizado.setContentText("El viaje se completó correctamente.");
        alertaFinalizado.showAndWait();

        // 2. Resumen del viaje: origen, destino, distancia y costo
        String resumen = String.format(
                Locale.US,
                "📍 Origen: %s%n" +
                "🏁 Destino: %s%n" +
                "📏 Distancia: %.2f km%n" +
                "💰 Costo: $%.2f",
                origenActual,
                destinoActual,
                distanciaActualKm,
                costoActual
        );

        Alert alertaResumen = new Alert(Alert.AlertType.INFORMATION);
        alertaResumen.setTitle("Resumen del Viaje");
        alertaResumen.setHeaderText("Detalle del viaje finalizado");
        alertaResumen.setContentText(resumen);
        alertaResumen.showAndWait();
    }

    @FXML
    private void handleVerHistorial(javafx.event.ActionEvent event) {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/vistas/PanelHistorialConductor.fxml"));
            javafx.scene.Parent root = loader.load();

            PanelHistorialConductorController historialController = loader.getController();
            if (historialController != null && usuarioActual != null) {
                historialController.initData(usuarioActual.getEmail());
            }

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