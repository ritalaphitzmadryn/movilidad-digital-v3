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
            if (tglDisponibilidad != null) {
                tglDisponibilidad.setSelected(estadoActual == EstadoConductor.DISPONIBLE);
            }
        }
    }

    @FXML
    private void handleCambiarDisponibilidad() {
        boolean activo = tglDisponibilidad != null && tglDisponibilidad.isSelected();
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
        if (listViajesDisponibles == null) return;
        listViajesDisponibles.getItems().clear();

        java.util.List<String> nombresClientes = new java.util.ArrayList<>();
        try {
            md.logica.GestorMovilidad gestor = md.aplicacion.Aplicacion.getGestor();
            if (gestor != null && gestor.obtenerUsuarios() != null) {
                for (Usuario u : gestor.obtenerUsuarios()) {
                    if (u.getCliente() != null && u.getNombre() != null && !u.getNombre().isBlank()) {
                        nombresClientes.add(u.getNombre());
                    }
                }
            }
        } catch (Exception ignored) {
        }

        if (nombresClientes.isEmpty()) {
            nombresClientes.addAll(java.util.List.of("Rita", "Lucía", "Martín", "Sofía", "Carlos"));
        }

        String[] puntos = {"UNPSJB", "Costanera", "Terminal", "Centro", "Ecocentro", "Puerto Madryn"};
        java.util.Random rnd = new java.util.Random();

        int cantidadViajes = 3;
        for (int i = 1; i <= cantidadViajes; i++) {
            String cliente = nombresClientes.get(rnd.nextInt(nombresClientes.size()));
            String origen = puntos[rnd.nextInt(puntos.length)];
            String destino = puntos[rnd.nextInt(puntos.length)];
            while (destino.equals(origen)) {
                destino = puntos[rnd.nextInt(puntos.length)];
            }

            double distKm = 1.5 + (rnd.nextDouble() * 6.5); // entre 1.5 y 8.0 km
            String viajeTexto = String.format(
                    Locale.US,
                    "Viaje #%d (%s): %s ➔ %s (Aprox. %.1f km)",
                    i, cliente, origen, destino, distKm
            );
            listViajesDisponibles.getItems().add(viajeTexto);
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
        origenActual = "Origen del viaje";
        destinoActual = "Destino del viaje";
        distanciaActualKm = 0.0;
        costoActual = 2000.0;

        if (textoViaje == null || textoViaje.isBlank()) {
            return;
        }

        java.util.regex.Matcher matcherDist = java.util.regex.Pattern
                .compile("(\\d+(?:[.,]\\d+)?)\\s*km", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(textoViaje);
        if (matcherDist.find()) {
            String num = matcherDist.group(1).replace(',', '.');
            try {
                distanciaActualKm = Double.parseDouble(num);
            } catch (NumberFormatException ignored) {
                distanciaActualKm = 0.0;
            }
        }

        String sinPrefijo = textoViaje.replaceFirst("^Viaje\\s*#?\\d*\\s*:?\\s*", "");
        String sinParentesis = sinPrefijo.replaceAll("\\(.*?\\)", "").trim();

        String[] partes = sinParentesis.split("\\s*(➔|->|→|—|–|-)\\s*");
        if (partes.length >= 2) {
            origenActual = partes[0].trim();
            destinoActual = partes[1].trim();
        } else if (!sinParentesis.isEmpty()) {
            origenActual = sinParentesis;
            destinoActual = "Destino";
        }

        double[] cOrigen = obtenerCoordenadasConocidas(origenActual);
        double[] cDestino = obtenerCoordenadasConocidas(destinoActual);
        if (cOrigen != null && cDestino != null) {
            try {
                com.graphhopper.ResponsePath ruta = md.ui.impl.javafx.map.RutaGraphHopperService.calcularRuta(
                        cOrigen[0], cOrigen[1], cDestino[0], cDestino[1]
                );
                if (ruta != null && ruta.getDistance() > 0) {
                    distanciaActualKm = ruta.getDistance() / 1000.0;
                }
            } catch (Exception ignored) {
            }
        }

        costoActual = 2000.0 + (distanciaActualKm * 200.0);
        System.out.println("📏 [Resumen] Origen: " + origenActual + " | Destino: " + destinoActual
                + " | Distancia: " + distanciaActualKm + " km | Costo: $" + costoActual);
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

                double[] cO = obtenerCoordenadasConocidas(origenActual);
                double[] cD = obtenerCoordenadasConocidas(destinoActual);

                Ubicacion origen = new Ubicacion(
                        origenActual,
                        cO != null ? cO[0] : 0.0,
                        cO != null ? cO[1] : 0.0
                );
                Ubicacion destino = new Ubicacion(
                        destinoActual,
                        cD != null ? cD[0] : 0.0,
                        cD != null ? cD[1] : 0.0
                );


                md.modelo.Servicio servicioViaje = null;
                try {
                    md.logica.GestorMovilidad gestor = md.aplicacion.Aplicacion.getGestor();
                    if (gestor != null) {
                        servicioViaje = gestor.buscarServicioAdecuado(
                                md.modelo.TipoServicio.PASAJEROS,
                                md.modelo.TipoVehiculo.AUTO,
                                md.modelo.CategoriaVehiculo.ESTANDAR
                        );
                    }
                } catch (Exception ignored) {
                }

                if (servicioViaje == null) {
                    servicioViaje = new md.modelo.Servicio(
                            "Standard",
                            md.modelo.TipoServicio.PASAJEROS,
                            md.modelo.TipoVehiculo.AUTO,
                            md.modelo.CategoriaVehiculo.ESTANDAR,
                            2000.0,
                            200.0,
                            100.0
                    );
                }
                
                Viaje viajeTerminado = new Viaje(usuarioActual, servicioViaje, origen, destino);
                String emailConductor = usuarioActual.getEmail();

                dao.guardarViajeRealizado(viajeTerminado, emailConductor);
                System.out.println("💾 [DAO] Viaje guardado para conductor (" + emailConductor + "): "
                        + origenActual + " ➔ " + destinoActual);
            } else {
                System.out.println("AVISO: usuarioActual es NULL, no se pudo guardar el registro.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al guardar viaje realizado: " + e.getMessage());
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
    @FXML
    private void handleVolverInicio(javafx.event.ActionEvent event) {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/vistas/VentanaPrincipal.fxml"));
            javafx.scene.Parent root = loader.load();

            VentanaPrincipalController principalController = loader.getController();
            if (principalController != null && usuarioActual != null) {
                principalController.setUsuarioActual(usuarioActual);
            }

            stage.setTitle("Movilidad Digital - Inicio");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}