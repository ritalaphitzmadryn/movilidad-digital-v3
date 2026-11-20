package md.ui.impl.javafx.controller;

import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import com.graphhopper.ResponsePath;
import com.graphhopper.util.PointList;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.util.StringConverter;
import md.aplicacion.Aplicacion;
import md.logica.GestorMovilidad;
import md.modelo.CategoriaVehiculo;
import md.modelo.Conductor;
import md.modelo.EstadoConductor;
import md.modelo.RolUsuario;
import md.modelo.Servicio;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import md.modelo.Usuario;
import md.rutas.api.IRutasService;
import md.rutas.impl.RutasServiceImpl;
import md.ui.impl.javafx.map.VisualizadorRutasLeaflet;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class VentanaPrincipalController implements Initializable {

    @FXML private Label lblResultadoViaje;
    @FXML private Label lblOrigenInfo;
    @FXML private Label lblDestinoInfo;
    @FXML private Label lblBienvenida;
    @FXML private Label lblTituloServicio;
    @FXML private Label lblTituloVehiculo;
    @FXML private Label lblElegirUsuario;
    @FXML private Label lblOrigenTitulo;
    @FXML private Label lblDestinoTitulo;
    @FXML private Label lblTipoServicioTitulo;

    @FXML private ComboBox<String> cbTipoUsuario;
    @FXML private ComboBox<String> cbTipoServicio;
    @FXML private ComboBox<TipoServicio> cbTipoServicioEnum;
    @FXML private ComboBox<TipoVehiculo> cbTipoVehiculo;
    @FXML private Label lblEstadoFlota;
    @FXML private Button btnSolicitar;
    @FXML private Button btnPincharOrigen;
    @FXML private Button btnPincharDestino;
    @FXML private Button btnLimpiar;
    @FXML private Button btnVolverConductor;
    @FXML private WebView mapWebView;
    @FXML private ComboBox<String> cbIdioma;

    private VisualizadorRutasLeaflet visualizadorMapa;
    private IRutasService rutasService = new RutasServiceImpl();
    private Usuario usuarioActual;
    private ResourceBundle bundle;

    private double latOrigen = -42.7692;
    private double lonOrigen = -65.0385;
    private double latDestino = -42.7480;
    private double lonDestino = -65.0310;

    private boolean origenSeleccionado = false;
    private boolean destinoSeleccionado = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.bundle = resources;
        this.rutasService = new RutasServiceImpl();

        if (this.bundle == null) {
            this.bundle = ResourceBundle.getBundle("ui.i18n.labels", new Locale("es", "ES"));
        }

        if (cbTipoUsuario != null) {
            cbTipoUsuario.setVisible(false);
            cbTipoUsuario.setManaged(false);
        }
        if (lblElegirUsuario != null) {
            lblElegirUsuario.setVisible(false);
            lblElegirUsuario.setManaged(false);
        }

        if (cbTipoServicio != null) {
            cbTipoServicio.getItems().addAll("Standard", "Confort", "Premium");
            cbTipoServicio.setConverter(new StringConverter<String>() {
            	@Override
            	public String toString(String cat) {
            	    if (cat == null) return "";
            	    String lower = cat.toLowerCase();
            	    if ("economic".equals(lower) || "económico".equals(lower) || "economico".equals(lower)) {
            	        return texto("cat.economic", cat);
            	    } else if ("standard".equals(lower) || "estándar".equals(lower) || "estandar".equals(lower)) {
            	        return texto("cat.standard", cat);
            	    } else if ("confort".equals(lower)) {
            	        return texto("cat.confort", cat);
            	    } else if ("vip".equals(lower) || "premium".equals(lower)) {
            	        return texto("cat.vip", cat);
            	    }
            	    return cat;
            	}

                @Override
                public String fromString(String string) {
                    return cbTipoServicio.getValue();
                }
            });
            cbTipoServicio.getSelectionModel().selectFirst();
        }

        if (cbTipoServicioEnum != null) {
            cbTipoServicioEnum.getItems().addAll(TipoServicio.values());
            cbTipoServicioEnum.setConverter(new StringConverter<TipoServicio>() {
                @Override
                public String toString(TipoServicio ts) {
                    if (ts == null) return "";
                    if (ts == TipoServicio.PASAJEROS) {
                        return texto("service.passengers", "PASAJEROS");
                    } else if (ts == TipoServicio.ENVIOS) {
                        return texto("service.delivery", "ENVIOS");
                    }
                    return ts.name();
                }

                @Override
                public TipoServicio fromString(String string) {
                    return cbTipoServicioEnum.getValue();
                }
            });
            cbTipoServicioEnum.getSelectionModel().selectFirst();
        }

        if (cbTipoVehiculo != null) {
            cbTipoVehiculo.getItems().addAll(TipoVehiculo.values());
            cbTipoVehiculo.setConverter(new StringConverter<TipoVehiculo>() {
                @Override
                public String toString(TipoVehiculo tv) {
                    if (tv == null) return "";
                    if (tv == TipoVehiculo.AUTO) {
                        return texto("vehicle.car", "AUTO");
                    } else if (tv == TipoVehiculo.MOTO) {
                        return texto("vehicle.moto", "MOTO");
                    }
                    return tv.name();
                }

                @Override
                public TipoVehiculo fromString(String string) {
                    return cbTipoVehiculo.getValue();
                }
            });
            cbTipoVehiculo.getSelectionModel().selectFirst();
        }

        if (mapWebView != null) {
            visualizadorMapa = new VisualizadorRutasLeaflet(mapWebView);

            visualizadorMapa.configurarSeleccion((tipo, coords) -> {
                if ("origen".equals(tipo)) {
                    latOrigen = coords[0];
                    lonOrigen = coords[1];
                    origenSeleccionado = true;
                    visualizadorMapa.mostrarMarcador("origen", latOrigen, lonOrigen);

                    if (lblOrigenInfo != null) {
                        lblOrigenInfo.setText(String.format(Locale.US, "Lat: %.4f, Lon: %.4f", latOrigen, lonOrigen));
                    }
                } else if ("destino".equals(tipo)) {
                    latDestino = coords[0];
                    lonDestino = coords[1];
                    destinoSeleccionado = true;
                    visualizadorMapa.mostrarMarcador("destino", latDestino, lonDestino);

                    if (lblDestinoInfo != null) {
                        lblDestinoInfo.setText(String.format(Locale.US, "Lat: %.4f, Lon: %.4f", latDestino, lonDestino));
                    }
                }
            });
        }

        if (btnPincharOrigen != null) {
            btnPincharOrigen.setOnAction(e -> {
                if (visualizadorMapa != null) {
                    visualizadorMapa.activarSeleccion("origen");
                }
            });
        }

        if (btnPincharDestino != null) {
            btnPincharDestino.setOnAction(e -> {
                if (visualizadorMapa != null) {
                    visualizadorMapa.activarSeleccion("destino");
                }
            });
        }

        if (btnSolicitar != null) {
            btnSolicitar.setOnAction(event -> handleSolicitarViaje());
        }

        if (btnLimpiar != null) {
            btnLimpiar.setOnAction(e -> {
                if (visualizadorMapa != null) {
                    visualizadorMapa.limpiarSeleccionYRutas();
                }
                String txtNoSel = texto("label.not.selected", "No seleccionado");
                if (lblOrigenInfo != null) lblOrigenInfo.setText(txtNoSel);
                if (lblDestinoInfo != null) lblDestinoInfo.setText(txtNoSel);
                if (lblResultadoViaje != null) lblResultadoViaje.setText("");

                if (cbTipoServicioEnum != null && !cbTipoServicioEnum.getItems().isEmpty()) {
                    cbTipoServicioEnum.getSelectionModel().selectFirst();
                }
                if (cbTipoVehiculo != null && !cbTipoVehiculo.getItems().isEmpty()) {
                    cbTipoVehiculo.getSelectionModel().selectFirst();
                }
                if (cbTipoServicio != null && !cbTipoServicio.getItems().isEmpty()) {
                    cbTipoServicio.getSelectionModel().selectFirst();
                }

                origenSeleccionado = false;
                destinoSeleccionado = false;
                actualizarIndicadorFlota();
            });
        }

        if (cbIdioma != null) {
            cbIdioma.getItems().setAll("Español", "English");
            boolean esIngles = bundle != null && "en".equalsIgnoreCase(bundle.getLocale().getLanguage());
            cbIdioma.getSelectionModel().select(esIngles ? "English" : "Español");

            cbIdioma.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null && !newValue.equals(oldValue)) {
                    Locale locale = "English".equals(newValue) ? new Locale("en", "US") : new Locale("es", "ES");
                    recargarVentanaConIdioma(locale);
                }
            });
        }

        aplicarTextosDesdeBundle();
        actualizarIndicadorFlota();
        actualizarBienvenida();
    }

    private String texto(String clave, String fallback) {
        if (bundle != null && bundle.containsKey(clave)) {
            return bundle.getString(clave);
        }
        return fallback;
    }

    private void aplicarTextosDesdeBundle() {
        if (lblTituloServicio != null) {
            lblTituloServicio.setText(texto("label.service.question", lblTituloServicio.getText()));
        }
        if (lblTituloVehiculo != null) {
            lblTituloVehiculo.setText(texto("label.vehicle.selection", lblTituloVehiculo.getText()));
        }
        if (lblOrigenTitulo != null) {
            lblOrigenTitulo.setText(texto("label.origin", lblOrigenTitulo.getText()));
        }
        if (lblDestinoTitulo != null) {
            lblDestinoTitulo.setText(texto("label.destination", lblDestinoTitulo.getText()));
        }
        if (lblTipoServicioTitulo != null) {
            lblTipoServicioTitulo.setText(texto("label.service.type", lblTipoServicioTitulo.getText()));
        }

        if (btnPincharOrigen != null) {
            btnPincharOrigen.setText(texto("btn.select.origin", btnPincharOrigen.getText()));
        }
        if (btnPincharDestino != null) {
            btnPincharDestino.setText(texto("btn.select.destination", btnPincharDestino.getText()));
        }
        if (btnSolicitar != null) {
            btnSolicitar.setText(texto("btn.request", btnSolicitar.getText()));
        }
        if (btnLimpiar != null) {
            btnLimpiar.setText(texto("btn.clear", btnLimpiar.getText()));
        }

        String txtNoSel = texto("label.not.selected", "No seleccionado");
        if (!origenSeleccionado && lblOrigenInfo != null) {
            lblOrigenInfo.setText(txtNoSel);
        }
        if (!destinoSeleccionado && lblDestinoInfo != null) {
            lblDestinoInfo.setText(txtNoSel);
        }

        actualizarBienvenida();
        refrescarCombo(cbTipoServicioEnum);
        refrescarCombo(cbTipoVehiculo);
        refrescarCombo(cbTipoServicio);
    }

    private <T> void refrescarCombo(ComboBox<T> combo) {
        if (combo == null) return;
        T actual = combo.getValue();
        combo.setValue(null);
        combo.setValue(actual);
    }    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        if (this.usuarioActual != null && this.usuarioActual.getCliente() != null) {
            this.usuarioActual.cambiarRolActivo(RolUsuario.CLIENTE);
        }
        actualizarBienvenida();
    }

    private void actualizarBienvenida() {
        if (lblBienvenida != null) {
            if (usuarioActual != null && usuarioActual.getNombre() != null && !usuarioActual.getNombre().isBlank()) {
                lblBienvenida.setText("¡Bienvenido " + usuarioActual.getNombre() + "!");
            } else {
                lblBienvenida.setText("¡Bienvenido!");
            }
        }
    }

    @FXML
    private void handleVolverLoguearConductor(javafx.event.ActionEvent event) {
        try {
            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/vistas/Login.fxml"));
            Parent root = loader.load();
            stage.setTitle("Movilidad Digital - Iniciar Sesión");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void recargarVentanaConIdioma(Locale locale) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/vistas/VentanaPrincipal.fxml"), ResourceBundle.getBundle("ui.i18n.labels", locale));
            Parent root = loader.load();
            VentanaPrincipalController controller = loader.getController();
            controller.setUsuarioActual(this.usuarioActual);

            Stage stage = (Stage) btnSolicitar.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSolicitarViaje() {
        try {
            if (!origenSeleccionado || !destinoSeleccionado) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(bundle != null && bundle.containsKey("alert.warning.title") ? bundle.getString("alert.warning.title") : "Atención");
                alert.setHeaderText(bundle != null && bundle.containsKey("alert.locations.header") ? bundle.getString("alert.locations.header") : "Ubicaciones no seleccionadas");
                alert.setContentText(bundle != null && bundle.containsKey("alert.locations.content") ? bundle.getString("alert.locations.content") : "Por favor, selecciona tanto el Origen como el Destino haciendo clic en el mapa antes de solicitar el viaje.");
                alert.showAndWait();
                return;
            }

            GestorMovilidad gestor = Aplicacion.getGestor();
            if (gestor == null) return;

            double distanciaViajeKm = 0.0;
            ResponsePath rutaViajeGH = null;
            boolean usoGraphHopperViaje = false;

            try {
                rutaViajeGH = md.ui.impl.javafx.map.RutaGraphHopperService.calcularRuta(
                        latOrigen, lonOrigen, latDestino, lonDestino
                );
                distanciaViajeKm = rutaViajeGH.getDistance() / 1000.0;
                usoGraphHopperViaje = true;
                System.out.println("🗺️ [GraphHopper] Distancia viaje: " + String.format(Locale.US, "%.2f km", distanciaViajeKm));
            } catch (Exception exGH) {
                System.err.println("Aviso GraphHopper (usando respaldo): " + exGH.getMessage());
                exGH.printStackTrace();
            }

            if (!usoGraphHopperViaje && rutasService != null) {
                distanciaViajeKm = rutasService.calcularDistanciaKm(latOrigen, lonOrigen, latDestino, lonDestino);
            }

            TipoServicio tipoServicioElegido = cbTipoServicioEnum != null ? cbTipoServicioEnum.getValue() : TipoServicio.PASAJEROS;
            TipoVehiculo tipoVehiculoElegido = cbTipoVehiculo != null ? cbTipoVehiculo.getValue() : TipoVehiculo.AUTO;
            String categoriaElegida = cbTipoServicio != null ? cbTipoServicio.getValue() : "Standard";

            double menorDistanciaMovil = Double.MAX_VALUE;
            double latMovil = latOrigen - 0.008;
            double lonMovil = lonOrigen + 0.008;
            String patenteMovil = "AA-123-BB";
            ResponsePath rutaAcercamientoGH = null;

            List<Usuario> usuarios = gestor.obtenerUsuarios();
            if (usuarios != null) {
                for (Usuario u : usuarios) {
                    if (u.getConductor() != null) {
                        Conductor c = u.getConductor();
                        double cLat = latOrigen + 0.015;
                        double cLon = lonOrigen + 0.015;

                        double dist = 1.0;
                        ResponsePath rutaCandidataGH = null;
                        try {
                            rutaCandidataGH = md.ui.impl.javafx.map.RutaGraphHopperService.calcularRuta(
                                    cLat, cLon, latOrigen, lonOrigen
                            );
                            dist = rutaCandidataGH.getDistance() / 1000.0;
                        } catch (Exception exMovil) {
                            System.err.println("Aviso GraphHopper móvil: " + exMovil.getMessage());
                            if (rutasService != null) {
                                dist = rutasService.calcularDistanciaKm(cLat, cLon, latOrigen, lonOrigen);
                            }
                        }

                        if (dist < menorDistanciaMovil) {
                            menorDistanciaMovil = dist;
                            latMovil = cLat;
                            lonMovil = cLon;
                            rutaAcercamientoGH = rutaCandidataGH;
                            patenteMovil = c.getVehiculoActivo() != null ? c.getVehiculoActivo().getPatente() : "Móvil Genérico";
                        }
                    }
                }
            }

            double tiempoViajeMin = (distanciaViajeKm / 30.0) * 60.0;
            CategoriaVehiculo categoriaEnum = CategoriaVehiculo.ESTANDAR;
            if ("Confort".equalsIgnoreCase(categoriaElegida)) {
                categoriaEnum = CategoriaVehiculo.CONFORT;
            } else if ("Premium".equalsIgnoreCase(categoriaElegida) || "VIP".equalsIgnoreCase(categoriaElegida)) {
                categoriaEnum = CategoriaVehiculo.PREMIUM;
            }

            Servicio servicioReal = gestor.buscarServicioAdecuado(tipoServicioElegido, tipoVehiculoElegido, categoriaEnum);
            if (servicioReal == null) {
                servicioReal = new Servicio(categoriaElegida, tipoServicioElegido, tipoVehiculoElegido, categoriaEnum, 2000.0, 200.0, 100.0);
            }

            double costoTotal = servicioReal.calcularCosto(distanciaViajeKm, tiempoViajeMin);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(bundle != null && bundle.containsKey("dialog.confirm.title") ? bundle.getString("dialog.confirm.title") : "Movilidad Digital - Confirmación de Servicio");

            VBox vbox = new VBox(15);
            vbox.setStyle("-fx-background-color: #ffffff; -fx-padding: 25px; -fx-border-color: #2e7d32; -fx-border-width: 3px;");
            vbox.setAlignment(Pos.TOP_LEFT);

            String headerTemplate = bundle != null && bundle.containsKey("dialog.confirm.header") ? bundle.getString("dialog.confirm.header") : "Resumen del Servicio ({0})";
            String tipoServicioStr = cbTipoServicioEnum != null && cbTipoServicioEnum.getConverter() != null ? cbTipoServicioEnum.getConverter().toString(tipoServicioElegido) : tipoServicioElegido.name();
            Label lblTitulo = new Label(MessageFormat.format(headerTemplate, tipoServicioStr));
            lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

            String detailsTemplate = bundle != null && bundle.containsKey("dialog.confirm.details") ? bundle.getString("dialog.confirm.details") :
                "Vehículo seleccionado: {0} ({1})\n" +
                "Distancia del trayecto: {2} km\n" +
                "Tiempo Estimado: ~{3} min\n" +
                "Costo Total Estimado: ${4}\n\n" +
                "¿Desea confirmar la solicitud y buscar un móvil?";

            String tipoVehiculoStr = cbTipoVehiculo != null && cbTipoVehiculo.getConverter() != null ? cbTipoVehiculo.getConverter().toString(tipoVehiculoElegido) : tipoVehiculoElegido.name();
            String textoDetalle = MessageFormat.format(
                detailsTemplate,
                tipoVehiculoStr, categoriaElegida, String.format(Locale.US, "%.2f", distanciaViajeKm), String.format(Locale.US, "%.0f", tiempoViajeMin), String.format(Locale.US, "%.2f", costoTotal)
            );
            Label lblContenido = new Label(textoDetalle);
            lblContenido.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-line-spacing: 5px;");

            Button btnConfirmar = new Button(bundle != null && bundle.containsKey("btn.confirm.search") ? bundle.getString("btn.confirm.search") : "Confirmar y Buscar");
            btnConfirmar.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-cursor: hand;");

            Button btnCancelar = new Button(bundle != null && bundle.containsKey("btn.cancel") ? bundle.getString("btn.cancel") : "Cancelar");
            btnCancelar.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-cursor: hand;");

            HBox botonesBox = new HBox(12, btnConfirmar, btnCancelar);
            botonesBox.setAlignment(Pos.CENTER_RIGHT);

            vbox.getChildren().addAll(lblTitulo, lblContenido, botonesBox);

            Scene scene = new Scene(vbox, 480, 260);
            dialogStage.setScene(scene);

            final boolean[] confirmado = {false};

            btnConfirmar.setOnAction(e -> {
                confirmado[0] = true;
                dialogStage.close();
            });

            btnCancelar.setOnAction(e -> {
                confirmado[0] = false;
                dialogStage.close();
            });

            dialogStage.showAndWait();

            if (confirmado[0]) {
                if (visualizadorMapa != null) {
                    String jsonAcercamiento = convertirPointListAJson(
                            rutaAcercamientoGH != null ? rutaAcercamientoGH.getPoints() : null,
                            latMovil, lonMovil, latOrigen, lonOrigen
                    );
                    String jsonTraslado = convertirPointListAJson(
                            rutaViajeGH != null ? rutaViajeGH.getPoints() : null,
                            latOrigen, lonOrigen, latDestino, lonDestino
                    );

                    visualizadorMapa.trazarRutasCompletas(
                            latMovil, lonMovil,
                            latOrigen, lonOrigen,
                            latDestino, lonDestino,
                            jsonAcercamiento,
                            jsonTraslado
                    );
                }

                Alert avisoFinalizado = new Alert(Alert.AlertType.INFORMATION);
                avisoFinalizado.setTitle("Estado del viaje");
                avisoFinalizado.setHeaderText("Viaje FINALIZADO!");
                avisoFinalizado.setContentText("El recorrido concluyó. A continuación se mostrará el detalle y la calificación.");
                avisoFinalizado.showAndWait();

                mostrarResumenEHistorialViaje(distanciaViajeKm, costoTotal, patenteMovil, tipoServicioElegido, tipoVehiculoElegido, categoriaElegida, tiempoViajeMin);
                actualizarIndicadorFlota();
            }
        } catch (Exception e) {
            System.err.println("Error al procesar el servicio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String convertirPointListAJson(PointList pts, double lat1, double lon1, double lat2, double lon2) {
        if (pts != null && !pts.isEmpty()) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < pts.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(String.format(Locale.US, "[%.6f, %.6f]", pts.getLat(i), pts.getLon(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        return String.format(Locale.US, "[[%f, %f], [%f, %f], [%f, %f]]", lat1, lon1, lat1, lon2, lat2, lon2);
    }

    private void mostrarResumenEHistorialViaje(double distanciaKm, double costoTotal, String patenteMovil, TipoServicio tipoServicio, TipoVehiculo tipoVehiculo, String categoria, double tiempoMin) {
        Dialog<ButtonType> dialogo = new Dialog<>();
        dialogo.setTitle(bundle != null && bundle.containsKey("dialog.track.title") ? bundle.getString("dialog.track.title") : "Movilidad Digital - Seguimiento de mi pedido");
        dialogo.setHeaderText(bundle != null && bundle.containsKey("dialog.track.header") ? bundle.getString("dialog.track.header") : "El viaje finalizó. ¡Gracias por usar Movilidad Digital!");

        DialogPane pane = dialogo.getDialogPane();
        pane.setPrefWidth(520);
        pane.setPrefHeight(640);
        pane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #2e7d32; -fx-border-width: 2px;");

        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f9f9f9;");

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #ffffff; -fx-background-color: transparent;");

        String progTitle = bundle != null && bundle.containsKey("section.progress") ? bundle.getString("section.progress") : "Progreso del viaje";
        Label lblProgresoTitulo = new Label(progTitle);
        lblProgresoTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333; -fx-font-size: 13px;");

        VBox boxProgreso = new VBox(8);
        boxProgreso.setAlignment(Pos.CENTER_LEFT);

        String st1 = bundle != null && bundle.containsKey("status.requested") ? bundle.getString("status.requested") : "Solicitado";
        String st2 = bundle != null && bundle.containsKey("status.accepted") ? bundle.getString("status.accepted") : "Aceptado";
        String st3 = bundle != null && bundle.containsKey("status.intrip") ? bundle.getString("status.intrip") : "En viaje";
        String st4 = bundle != null && bundle.containsKey("status.finished") ? bundle.getString("status.finished") : "Finalizado";

        String[] estados = {"1-" + st1, "2-" + st2, "3-" + st3, "4-" + st4};
        for (String estado : estados) {
            Label lblEstado = new Label(estado);
            lblEstado.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-padding: 4px 10px; -fx-background-radius: 12px; -fx-font-size: 11px; -fx-font-weight: bold;");
            boxProgreso.getChildren().add(lblEstado);
        }

        HBox boxMetricas = new HBox(10);
        boxMetricas.setAlignment(Pos.CENTER);

        String mTime = bundle != null && bundle.containsKey("section.metrics.time") ? bundle.getString("section.metrics.time") : "TIEMPO ESTIMADO";
        String mDist = bundle != null && bundle.containsKey("section.metrics.distance") ? bundle.getString("section.metrics.distance") : "DISTANCIA APROX";
        String mCost = bundle != null && bundle.containsKey("section.metrics.cost") ? bundle.getString("section.metrics.cost") : "COSTO ESTIMADO";

        VBox cardTiempo = crearTarjetaMetrica(mTime, String.format(Locale.US, "~%.0f min", tiempoMin));
        VBox cardDistancia = crearTarjetaMetrica(mDist, String.format(Locale.US, "%.2f km", distanciaKm));
        VBox cardCosto = crearTarjetaMetrica(mCost, String.format(Locale.US, "$ %.2f", costoTotal));
        cardCosto.setStyle("-fx-background-color: white; -fx-border-color: #2e7d32; -fx-border-radius: 4px; -fx-padding: 8px 12px; -fx-alignment: center;");

        boxMetricas.getChildren().addAll(cardTiempo, cardDistancia, cardCosto);

        String dTitle = bundle != null && bundle.containsKey("section.details") ? bundle.getString("section.details") : "Detalles del pedido";
        Label lblDetallesTitulo = new Label(dTitle);
        lblDetallesTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333; -fx-font-size: 13px;");

        VBox boxDetalles = new VBox(5);
        boxDetalles.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: #dddddd; -fx-border-radius: 5px;");

        String nombreCliente = usuarioActual != null && usuarioActual.getNombre() != null ? usuarioActual.getNombre() : "Cliente";
        String textoConductor = "Conductor: No hay unidades disponibles";
        try {
            GestorMovilidad gestorBusqueda = Aplicacion.getGestor();
            if (gestorBusqueda != null && gestorBusqueda.obtenerUsuarios() != null) {
                List<Usuario> conductoresDisponibles = new java.util.ArrayList<>();
                for (Usuario u : gestorBusqueda.obtenerUsuarios()) {
                    if (u.getConductor() != null && u.getConductor().getEstadoConductor() == EstadoConductor.DISPONIBLE) {
                        conductoresDisponibles.add(u);
                    }
                }
                if (!conductoresDisponibles.isEmpty()) {
                    Usuario usuarioConductor = conductoresDisponibles.get(new java.util.Random().nextInt(conductoresDisponibles.size()));
                    String nombreCond = usuarioConductor.getNombre() != null ? usuarioConductor.getNombre() : "Conductor Asignado";
                    String telCond = usuarioConductor.getTelefono() != null ? usuarioConductor.getTelefono() : "+549110000000";
                    textoConductor = "Conductor: " + nombreCond + " - " + telCond;
                }
            }
        } catch (Exception e) {
            System.err.println("Error al buscar conductor al azar: " + e.getMessage());
        }

        String tipoVehiculoStr = cbTipoVehiculo != null && cbTipoVehiculo.getConverter() != null ? cbTipoVehiculo.getConverter().toString(tipoVehiculo) : tipoVehiculo.name();
        String tipoServicioStr = cbTipoServicioEnum != null && cbTipoServicioEnum.getConverter() != null ? cbTipoServicioEnum.getConverter().toString(tipoServicio) : tipoServicio.name();

        Label l1 = new Label("Pasajero: " + nombreCliente + " - +549115545146");
        Label l2 = new Label(textoConductor);
        Label l3 = new Label("Vehículo: " + tipoVehiculoStr + " - " + patenteMovil + " - " + categoria);
        Label l4 = new Label("Servicio: " + categoria + " (" + tipoServicioStr + ")");
        Label l5 = new Label(String.format(Locale.US, "Origen: Lat %.4f, Lon %.4f", latOrigen, lonOrigen));
        Label l6 = new Label(String.format(Locale.US, "Destino: Lat %.4f, Lon %.4f", latDestino, lonDestino));
        Label l7 = new Label("Calificaciones: Cliente: ⭐⭐⭐⭐⭐ · Conductor: ⭐⭐⭐⭐☆");
        l7.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        boxDetalles.getChildren().addAll(l1, l2, l3, l4, l5, l6, l7);
        for (javafx.scene.Node node : boxDetalles.getChildren()) {
            if (node instanceof Label && node != l7) {
                ((Label) node).setStyle("-fx-text-fill: #333333; -fx-font-size: 11px;");
            }
        }

        String hTitle = bundle != null && bundle.containsKey("section.history") ? bundle.getString("section.history") : "Historial de eventos";
        Label lblHistorialTitulo = new Label(hTitle);
        lblHistorialTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333; -fx-font-size: 13px;");

        VBox boxHistorial = new VBox(3);
        boxHistorial.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: #dddddd; -fx-border-radius: 5px; -fx-font-family: 'Consolas', monospace;");

        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String t1 = java.time.LocalDateTime.now().minusMinutes(3).format(dtf);
        String t2 = java.time.LocalDateTime.now().minusMinutes(2).format(dtf);
        String t3 = java.time.LocalDateTime.now().minusMinutes(1).format(dtf);
        String t4 = java.time.LocalDateTime.now().format(dtf);

        boxHistorial.getChildren().addAll(
            new Label(t1 + "  " + st1),
            new Label(t2 + "  " + st2),
            new Label(t3 + "  " + st3),
            new Label(t4 + "  " + st4)
        );
        for (javafx.scene.Node node : boxHistorial.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: #333333; -fx-font-size: 11px;");
            }
        }

        String rTitle = bundle != null && bundle.containsKey("section.rating") ? bundle.getString("section.rating") : "Califica tu experiencia de viaje:";
        Label lblCalifica = new Label(rTitle);
        lblCalifica.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        HBox panelEstrellas = new HBox(8);
        panelEstrellas.setAlignment(Pos.CENTER);
        final int[] califSeleccionada = { 5 };
        ToggleGroup grupoEstrellas = new ToggleGroup();
        for (int i = 1; i <= 5; i++) {
            ToggleButton btnEstrella = new ToggleButton(i + " ⭐");
            btnEstrella.setToggleGroup(grupoEstrellas);
            btnEstrella.setStyle("-fx-base: #e8f5e9; -fx-cursor: hand;");
            final int valor = i;
            btnEstrella.setOnAction(e -> califSeleccionada[0] = valor);
            if (i == 5) btnEstrella.setSelected(true);
            panelEstrellas.getChildren().add(btnEstrella);
        }

        root.getChildren().addAll(lblProgresoTitulo, boxProgreso, boxMetricas, lblDetallesTitulo, boxDetalles, lblHistorialTitulo, boxHistorial, lblCalifica, panelEstrellas);
        pane.setContent(scrollPane);

        ButtonType btnFinalizar = new ButtonType("Enviar Calificación y Cerrar", ButtonBar.ButtonData.OK_DONE);
        pane.getButtonTypes().add(btnFinalizar);

        dialogo.setResultConverter(dialogButton -> {
            if (dialogButton == btnFinalizar) {
                System.out.println("--- REGISTRO DE CALIFICACIÓN ---");
                System.out.println("Puntuación guardada: " + califSeleccionada[0] + " estrellas.");
            }
            return null;
        });

        dialogo.showAndWait();
    }

    private VBox crearTarjetaMetrica(String titulo, String valor) {
        VBox card = new VBox(3);
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-border-color: #dddddd; -fx-border-radius: 4px; -fx-padding: 8px 12px;");

        Label lblTit = new Label(titulo);
        lblTit.setStyle("-fx-font-size: 10px; -fx-text-fill: #666666; -fx-font-weight: bold;");

        Label lblVal = new Label(valor);
        lblVal.setStyle("-fx-font-size: 13px; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");

        card.getChildren().addAll(lblTit, lblVal);
        return card;
    }

    private void actualizarIndicadorFlota() {
        if (lblEstadoFlota == null) return;
        int disponibles = 0;
        try {
            GestorMovilidad gestor = Aplicacion.getGestor();
            if (gestor != null && gestor.obtenerUsuarios() != null) {
                for (Usuario u : gestor.obtenerUsuarios()) {
                    if (u.getConductor() != null && u.getConductor().getEstadoConductor() == EstadoConductor.DISPONIBLE) {
                        disponibles++;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        lblEstadoFlota.setText("Conductores disponibles: " + disponibles);
    }
}