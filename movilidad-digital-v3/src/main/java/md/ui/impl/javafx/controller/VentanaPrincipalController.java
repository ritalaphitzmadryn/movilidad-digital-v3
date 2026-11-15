package md.ui.impl.javafx.controller;

import java.net.URL;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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
import javafx.scene.control.TextArea;
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

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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
    @FXML private ComboBox<String> cbTipoServicio; // Para Categoría (Standard, Confort etc.)
    
    // ComboBox para los Enums del diagrama de clases
    @FXML private ComboBox<TipoServicio> cbTipoServicioEnum; // PASAJEROS, ENVIOS
    @FXML private ComboBox<TipoVehiculo> cbTipoVehiculo;     // AUTO, MOTO
    @FXML private Label lblEstadoFlota;
    @FXML private Button btnSolicitar;
    @FXML private Button btnPincharOrigen;
    @FXML private Button btnPincharDestino;
    @FXML private Button btnLimpiar; 
    @FXML private WebView mapWebView; 

    private VisualizadorRutasLeaflet visualizadorMapa;
    private IRutasService rutasService = new RutasServiceImpl();
    private Usuario usuarioActual;
    private Label lblDistanciaValor;
    private Label lblCostoValor;
    private Label lblConductorValor;
    private Label lblVehiculoValor;
    
    // Coordenadas iniciales por defecto en Puerto Madryn
    private double latOrigen = -42.7692;
    private double lonOrigen = -65.0385;
    private double latDestino = -42.7480;
    private double lonDestino = -65.0310;
    
    private boolean origenSeleccionado = false;
    private boolean destinoSeleccionado = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.rutasService = new RutasServiceImpl();

        if (cbTipoUsuario != null) {
            cbTipoUsuario.getItems().addAll("Cliente (Pasajero)", "Conductor");
            cbTipoUsuario.getSelectionModel().selectFirst();
            
            // Evento para cambiar el rol desde el ComboBox
            cbTipoUsuario.setOnAction(event -> {
                if (usuarioActual != null) {
                    String seleccion = cbTipoUsuario.getValue();
                    if ("Conductor".equals(seleccion) || "Driver".equals(seleccion)) {
                        if (usuarioActual.getConductor() != null) {
                            usuarioActual.cambiarRolActivo(RolUsuario.CONDUCTOR);
                            System.out.println("Rol cambiado a: CONDUCTOR");
                        } else {
                            System.out.println("El usuario no tiene perfil de conductor.");
                            cbTipoUsuario.getSelectionModel().select(bundle.containsKey("user.client") ? bundle.getString("user.client") : "Cliente (Pasajero)");
                        }
                    } else {
                        if (usuarioActual.getCliente() != null) {
                            usuarioActual.cambiarRolActivo(RolUsuario.CLIENTE);
                            System.out.println("Rol cambiado a: CLIENTE");
                        } else {
                            System.out.println("El usuario no tiene perfil de cliente.");
                            cbTipoUsuario.getSelectionModel().select(bundle.containsKey("user.driver") ? bundle.getString("user.driver") : "Conductor");
                        }
                    }
                }
            });
        }

        // Poblar ComboBox de Categoría
        if (cbTipoServicio != null) {
            cbTipoServicio.getItems().addAll("Standard", "Confort", "Premium");
            cbTipoServicio.getSelectionModel().selectFirst();
        }

        // Poblar ComboBox para TipoServicio (Enum: PASAJEROS, ENVIOS) con conversor i18n
        if (cbTipoServicioEnum != null) {
            cbTipoServicioEnum.getItems().addAll(TipoServicio.values());
            cbTipoServicioEnum.setConverter(new StringConverter<TipoServicio>() {
                @Override
                public String toString(TipoServicio ts) {
                    if (ts == null) return "";
                    if (ts == TipoServicio.PASAJEROS) {
                        return bundle != null && bundle.containsKey("service.passengers") ? bundle.getString("service.passengers") : "PASAJEROS";
                    } else if (ts == TipoServicio.ENVIOS) {
                        return bundle != null && bundle.containsKey("service.delivery") ? bundle.getString("service.delivery") : "ENVIOS";
                    }
                    return ts.name();
                }
                @Override
                public TipoServicio fromString(String string) { return null; }
            });
            cbTipoServicioEnum.getSelectionModel().selectFirst();
        }

        // Poblar ComboBox para TipoVehiculo (Enum: AUTO, MOTO) con conversor i18n
        if (cbTipoVehiculo != null) {
            cbTipoVehiculo.getItems().addAll(TipoVehiculo.values());
            cbTipoVehiculo.setConverter(new StringConverter<TipoVehiculo>() {
                @Override
                public String toString(TipoVehiculo tv) {
                    if (tv == null) return "";
                    if (tv == TipoVehiculo.AUTO) {
                        return bundle != null && bundle.containsKey("vehicle.car") ? bundle.getString("vehicle.car") : "AUTO";
                    } else if (tv == TipoVehiculo.MOTO) {
                        return bundle != null && bundle.containsKey("vehicle.moto") ? bundle.getString("vehicle.moto") : "MOTO";
                    }
                    return tv.name();
                }
                @Override
                public TipoVehiculo fromString(String string) { return null; }
            });
            cbTipoVehiculo.getSelectionModel().selectFirst();
        }
        
        // Inicializar el Visualizador pasando mapWebView directamente
        if (mapWebView != null) {
            visualizadorMapa = new VisualizadorRutasLeaflet(mapWebView);

            // Configurar el listener de clics devueltos por el mapa
            visualizadorMapa.configurarSeleccion((tipo, coords) -> {
                if ("origen".equals(tipo)) {
                    latOrigen = coords[0];
                    lonOrigen = coords[1];
                    origenSeleccionado = true;
                    
                    visualizadorMapa.mostrarMarcador("origen", latOrigen, lonOrigen);
                    
                    if (lblOrigenInfo != null) {
                        lblOrigenInfo.setText(String.format(Locale.US, "Lat: %.4f, Lon: %.4f", latOrigen, lonOrigen));
                    }
                    System.out.println("Origen fijado desde el mapa: " + latOrigen + ", " + lonOrigen);
                    
                } else if ("destino".equals(tipo)) {
                    latDestino = coords[0];
                    lonDestino = coords[1];
                    destinoSeleccionado = true;
                    
                    visualizadorMapa.mostrarMarcador("destino", latDestino, lonDestino);
                    
                    if (lblDestinoInfo != null) {
                        lblDestinoInfo.setText(String.format(Locale.US, "Lat: %.4f, Lon: %.4f", latDestino, lonDestino));
                    }
                    System.out.println("Destino fijado desde el mapa: " + latDestino + ", " + lonDestino);
                }
            });
        }
            
        // Vincular los botones de pinchar en el mapa
        if (btnPincharOrigen != null) {
            btnPincharOrigen.setOnAction(e -> {
                if (visualizadorMapa != null) {
                    visualizadorMapa.activarSeleccion("origen");
                    System.out.println("Modo selección activado: Haga clic en el mapa para marcar el Origen.");
                }
            });
        }

        if (btnPincharDestino != null) {
            btnPincharDestino.setOnAction(e -> {
                if (visualizadorMapa != null) {
                    visualizadorMapa.activarSeleccion("destino");
                    System.out.println("Modo selección activado: Haga clic en el mapa para marcar el Destino.");
                }
            });
        }

        // Vincular el botón de solicitar viaje
        if (btnSolicitar != null) {
            btnSolicitar.setOnAction(event -> handleSolicitarViaje());
        }
        
        // Vincular el botón de limpiar selecciones
        if (btnLimpiar != null) {
            btnLimpiar.setOnAction(e -> {
                if (visualizadorMapa != null) {
                    visualizadorMapa.limpiarSeleccionYRutas();
                }
                String txtNoSel = bundle != null && bundle.containsKey("label.not.selected") ? bundle.getString("label.not.selected") : "No seleccionado";
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
                System.out.println("Mapa restablecido correctamente. Listo para nuevo origen.");
                
                actualizarIndicadorFlota();
            });
        }

        // LLAMADA CLAVE PARA QUE MUESTRE LOS CONDUCTORES DISPONIBLES AL ARRANCAR
        actualizarIndicadorFlota();
        
        if (cbIdioma != null) {
            cbIdioma.getItems().addAll("Español", "English");
            cbIdioma.getSelectionModel().select("Español");
            
            cbIdioma.valueProperty().addListener((obs, oldValue, newValue) -> {
            	if (newValue != null && !newValue.equals(oldValue)) {
            		Locale locale = "English".equals(newValue) ? new Locale("en", "US") : new Locale("es", "ES");
            		recargarVentanaConIdioma(locale);
            	}
            });
        }
        
    }
    
    @FXML private ComboBox<String> cbIdioma;
    
    @FXML
    private void handleSolicitarViaje() {
        try {
        	// VALIDACIÓN CLAVE: Evita que use coordenadas por defecto si no se seleccionó en el mapa
            if (!origenSeleccionado || !destinoSeleccionado) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle(bundle != null && bundle.containsKey("alert.warning.title") ? bundle.getString("alert.warning.title") : "Atención");
                alert.setHeaderText(bundle != null && bundle.containsKey("alert.locations.header") ? bundle.getString("alert.locations.header") : "Ubicaciones no seleccionadas");
                alert.setContentText(bundle != null && bundle.containsKey("alert.locations.content") ? bundle.getString("alert.locations.content") : "Por favor, selecciona tanto el Origen como el Destino haciendo clic en el mapa antes de solicitar el viaje.");
                alert.showAndWait();
                return;
            }
            
            GestorMovilidad gestor = Aplicacion.getGestor();
            if (gestor == null) {
                System.err.println("Error: El GestorMovilidad no ha sido inicializado.");
                return;
            }

            // 1. Calcular distancia y detalle de la ruta principal (Origen -> Destino)
            double distanciaViajeKm = 0.0;
            String detalleRuta = "";
            if (rutasService != null) {
                distanciaViajeKm = rutasService.calcularDistanciaKm(latOrigen, lonOrigen, latDestino, lonDestino);
                detalleRuta = rutasService.calcularRuta(latOrigen, lonOrigen, latDestino, lonDestino);
            }

            // Capturar las selecciones de los ComboBox de Enums
            TipoServicio tipoServicioElegido = cbTipoServicioEnum != null ? cbTipoServicioEnum.getValue() : TipoServicio.PASAJEROS;
            TipoVehiculo tipoVehiculoElegido = cbTipoVehiculo != null ? cbTipoVehiculo.getValue() : TipoVehiculo.AUTO;
            String categoriaElegida = cbTipoServicio != null ? cbTipoServicio.getValue() : "Standard";

            // 2. Buscar el conductor / móvil más cercano disponible
            double menorDistanciaMovil = Double.MAX_VALUE;
            double latMovil = latOrigen - 0.008; 
            double lonMovil = lonOrigen + 0.008;
            String patenteMovil = "AA-123-BB";

            List<Usuario> usuarios = gestor.obtenerUsuarios();
            if (usuarios != null) {
                for (Usuario u : usuarios) {
                    if (u.getConductor() != null) {
                        Conductor c = u.getConductor();
                        double cLat = latOrigen + 0.015; 
                        double cLon = lonOrigen + 0.015;
                        
                        double dist = rutasService != null ? 
                            rutasService.calcularDistanciaKm(cLat, cLon, latOrigen, lonOrigen) :
                            Math.sqrt(Math.pow(cLat - latOrigen, 2) + Math.pow(cLon - lonOrigen, 2)) * 111.0;

                        if (dist < menorDistanciaMovil) {
                            menorDistanciaMovil = dist;
                            latMovil = cLat;
                            lonMovil = cLon;
                            patenteMovil = c.getVehiculoActivo() != null ? c.getVehiculoActivo().getPatente() : "Móvil Genérico";
                        }
                    }
                }
            }

            // 3. Costo y tiempos estimados usando servicios reales de la persistencia
            double tiempoViajeMin = (distanciaViajeKm / 30.0) * 60.0;
            double tiempoAcercamientoMin = (menorDistanciaMovil / 30.0) * 60.0;

            // Mapear el String del ComboBox de categoría al Enum CategoriaVehiculo
            CategoriaVehiculo categoriaEnum = CategoriaVehiculo.ESTANDAR;
            if ("Confort".equalsIgnoreCase(categoriaElegida)) {
                categoriaEnum = CategoriaVehiculo.CONFORT;
            } else if ("Premium".equalsIgnoreCase(categoriaElegida) || "VIP".equalsIgnoreCase(categoriaElegida)) {
                categoriaEnum = CategoriaVehiculo.PREMIUM;
            }

            // Buscar el servicio real que coincida con los selectores del usuario
            Servicio servicioReal = gestor.buscarServicioAdecuado(tipoServicioElegido, tipoVehiculoElegido, categoriaEnum);
            
            // Fallback por seguridad si no se encuentra en la lista
            if (servicioReal == null) {
                servicioReal = new Servicio(categoriaElegida, tipoServicioElegido, tipoVehiculoElegido, categoriaEnum, 2000.0, 200.0, 100.0);
            }

            // Calcular el costo real usando la tarifa propia del servicio seleccionado
            double costoTotal = servicioReal.calcularCosto(distanciaViajeKm, tiempoViajeMin);

            // DIÁLOGO DE CONFIRMACIÓN TRADUCIDO
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

            // Si el usuario presionó confirmar, ejecutamos el flujo completo de seguimiento
            if (confirmado[0]) {
            	if (visualizadorMapa != null) {
            	    String geometriaCalleSimulada = String.format(
            	        Locale.US, 
            	        "[[%f, %f], [%f, %f], [%f, %f]]",
            	        latOrigen, lonOrigen,   
            	        latOrigen, lonDestino,  
            	        latDestino, lonDestino  
            	    );
            	    visualizadorMapa.trazarRutasCompletas(latMovil, lonMovil, latOrigen, lonOrigen, latDestino, lonDestino, geometriaCalleSimulada);
            	}

                mostrarResumenEHistorialViaje(distanciaViajeKm, costoTotal, patenteMovil, tipoServicioElegido, tipoVehiculoElegido, categoriaElegida, tiempoViajeMin);
                actualizarIndicadorFlota();
            }
        } catch (Exception e) {
            System.err.println("Error al procesar el servicio: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // =========================================================================
    // VENTANA DE SEGUIMIENTO, RESUMEN, HISTORIAL Y CALIFICACIÓN TRADUCIDA
    // =========================================================================
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

        // 1. PROGRESO DEL VIAJE
        String progTitle = bundle != null && bundle.containsKey("section.progress") ? bundle.getString("section.progress") : "Progreso del viaje";
        Label lblProgresoTitulo = new Label(progTitle);
        lblProgresoTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333; -fx-font-size: 13px;");
        
        HBox boxProgreso = new HBox(8);
        boxProgreso.setAlignment(Pos.CENTER);
        
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

        // 2. MÉTRICAS
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

        // 3. DETALLES DEL PEDIDO
        String dTitle = bundle != null && bundle.containsKey("section.details") ? bundle.getString("section.details") : "Detalles del pedido";
        Label lblDetallesTitulo = new Label(dTitle);
        lblDetallesTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333; -fx-font-size: 13px;");

        VBox boxDetalles = new VBox(5);
        boxDetalles.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: #dddddd; -fx-border-radius: 5px;");
        
        String nombreCliente = usuarioActual != null ? usuarioActual.getNombre() : "Cliente General";
        String tipoVehiculoStr = cbTipoVehiculo != null && cbTipoVehiculo.getConverter() != null ? cbTipoVehiculo.getConverter().toString(tipoVehiculo) : tipoVehiculo.name();
        String tipoServicioStr = cbTipoServicioEnum != null && cbTipoServicioEnum.getConverter() != null ? cbTipoServicioEnum.getConverter().toString(tipoServicio) : tipoServicio.name();
        
        Label l1 = new Label("Passenger: " + nombreCliente + " - +549115545146");
        Label l2 = new Label("Driver: Diego Sanchez - +549113872427");
        Label l3 = new Label("Vehicle: " + tipoVehiculoStr + " - " + patenteMovil + " - " + categoria);
        Label l4 = new Label("Service: " + categoria + " (" + tipoServicioStr + ")");
        Label l5 = new Label(String.format(Locale.US, "Origin: Lat %.4f, Lon %.4f", latOrigen, lonOrigen));
        Label l6 = new Label(String.format(Locale.US, "Destination: Lat %.4f, Lon %.4f", latDestino, lonDestino));
        Label l7 = new Label("Ratings: Client: ⭐⭐⭐⭐⭐ · Driver: ⭐⭐⭐⭐☆");
        l7.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");

        boxDetalles.getChildren().addAll(l1, l2, l3, l4, l5, l6, l7);
        for (javafx.scene.Node node : boxDetalles.getChildren()) {
            if (node instanceof Label && node != l7) {
                ((Label) node).setStyle("-fx-text-fill: #333333; -fx-font-size: 11px;");
            }
        }

        // 4. HISTORIAL DE EVENTOS
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

        // 5. CALIFICACIÓN
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

        String btnSubText = bundle != null && bundle.containsKey("btn.submit.rating") ? bundle.getString("btn.submit.rating") : "Enviar Calificación y Cerrar";
        ButtonType btnFinalizar = new ButtonType(btnSubText, ButtonBar.ButtonData.OK_DONE);
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
        card.setPadding(new Insets(6, 12, 6, 12));
        card.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-radius: 4px;");
        
        Label lblT = new Label(titulo);
        lblT.setStyle("-fx-font-size: 9px; -fx-text-fill: #666666; -fx-font-weight: bold;");
        Label lblV = new Label(valor);
        lblV.setStyle("-fx-font-size: 13px; -fx-text-fill: #2e7d32; -fx-font-weight: bold;");
        
        card.getChildren().addAll(lblT, lblV);
        return card;
    }

    // ==========================================
    // ACTUALIZAR PANEL DE RESUMEN Y COSTOS
    // ==========================================
    private void actualizarDatosViaje(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
        try {
            GestorMovilidad gestor = Aplicacion.getGestor();
            if (gestor == null) return;

            double distanciaKm = rutasService.calcularDistanciaKm(latOrigen, lonOrigen, latDestino, lonDestino);
            double costoEstimado = gestor.calcularCostoEstimado(distanciaKm);
            
            Conductor conductorAsignado = gestor.asignarConductorAutomatico();
            
            if (lblDistanciaValor != null) {
                lblDistanciaValor.setText(String.format("%.2f km", distanciaKm));
            }
            
            if (lblCostoValor != null) {
                lblCostoValor.setText(String.format("$ %.2f", costoEstimado));
                lblCostoValor.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
            }
            
            if (conductorAsignado != null && lblConductorValor != null) {
                String infoConductor = "Conductor asignado (Lic: " + conductorAsignado.getLicenciaConducir() + ")";
                lblConductorValor.setText(infoConductor + " - ⭐ 4.8");
            } else if (lblConductorValor != null) {
                lblConductorValor.setText("Buscando conductor cercano...");
            }

            System.out.println("Panel actualizado con éxito. Distancia: " + distanciaKm + "km | Costo: $" + costoEstimado);

        } catch (Exception e) {
            System.err.println("Error al actualizar los datos del viaje: " + e.getMessage());
        }
    }
    
    // Atributo global para mantener el bundle activo en la clase
    private ResourceBundle bundle = ResourceBundle.getBundle("ui.i18n.labels", new Locale("es", "ES"));

    // ==========================================
    // RECARGAR VENTANA USANDO RESOURCEBUNDLE (i18n)
    // ==========================================
    private void recargarVentanaConIdioma(Locale locale) {
        try {
            // 1. Cargar el diccionario correspondiente según el Locale
            bundle = ResourceBundle.getBundle("ui.i18n.labels", locale);
            
            // 2. Traducir textos principales de etiquetas y botones
            if (lblBienvenida != null && usuarioActual != null) {
                lblBienvenida.setText("¡Bienvenido, " + usuarioActual.getNombre() + "!");
            } else if (lblBienvenida != null) {
                lblBienvenida.setText(bundle.getString("label.welcome.default"));
            }
            
            if (btnSolicitar != null) btnSolicitar.setText(bundle.getString("btn.request"));
            if (btnLimpiar != null) btnLimpiar.setText(bundle.getString("btn.clear"));
            if (btnPincharOrigen != null) btnPincharOrigen.setText(bundle.getString("btn.select.origin"));
            if (btnPincharDestino != null) btnPincharDestino.setText(bundle.getString("btn.select.destination"));

            // Etiquetas del panel lateral
            if (lblTituloServicio != null) lblTituloServicio.setText(bundle.getString("label.service.question"));
            if (lblTituloVehiculo != null) lblTituloVehiculo.setText(bundle.getString("label.vehicle.selection"));
            if (lblElegirUsuario != null) lblElegirUsuario.setText(bundle.getString("label.user.type"));
            if (lblOrigenTitulo != null) lblOrigenTitulo.setText(bundle.getString("label.origin"));
            if (lblDestinoTitulo != null) lblDestinoTitulo.setText(bundle.getString("label.destination"));
            if (lblTipoServicioTitulo != null) lblTipoServicioTitulo.setText(bundle.getString("label.service.type"));

            // Actualizar texto de "No seleccionado"
            String txtNoSel = bundle.getString("label.not.selected");
            if (lblOrigenInfo != null && (lblOrigenInfo.getText().contains("No seleccionado") || lblOrigenInfo.getText().contains("Not selected"))) {
                lblOrigenInfo.setText(txtNoSel);
            }
            if (lblDestinoInfo != null && (lblDestinoInfo.getText().contains("No seleccionado") || lblDestinoInfo.getText().contains("Not selected"))) {
                lblDestinoInfo.setText(txtNoSel);
            }

            // 3. Traducir las opciones del ComboBox de Tipo de Usuario
            if (cbTipoUsuario != null) {
                String selUsuario = cbTipoUsuario.getValue();
                cbTipoUsuario.getItems().clear();
                cbTipoUsuario.getItems().addAll(bundle.getString("user.client"), bundle.getString("user.driver"));
                cbTipoUsuario.getSelectionModel().selectFirst();
            }

            // 4. Traducir las opciones del ComboBox de Categoría
            if (cbTipoServicio != null) {
                String selCat = cbTipoServicio.getValue();
                cbTipoServicio.getItems().clear();
                cbTipoServicio.getItems().addAll(
                    bundle.containsKey("cat.standard") ? bundle.getString("cat.standard") : "Standard",
                    bundle.containsKey("cat.confort") ? bundle.getString("cat.confort") : "Confort",
                    bundle.containsKey("cat.vip") ? bundle.getString("cat.vip") : (bundle.containsKey("cat.Premium") ? bundle.getString("cat.Premium") : "Premium")
                );
                if (selCat != null && cbTipoServicio.getItems().contains(selCat)) {
                    cbTipoServicio.setValue(selCat);
                } else {
                    cbTipoServicio.getSelectionModel().selectFirst();
                }
            }

            // 5. Refrescar ComboBox de Enums
            if (cbTipoServicioEnum != null) {
                TipoServicio selS = cbTipoServicioEnum.getValue();
                cbTipoServicioEnum.getItems().clear();
                cbTipoServicioEnum.getItems().addAll(TipoServicio.values());
                if (selS != null) cbTipoServicioEnum.getSelectionModel().select(selS);
                else cbTipoServicioEnum.getSelectionModel().selectFirst();
            }

            if (cbTipoVehiculo != null) {
                TipoVehiculo selV = cbTipoVehiculo.getValue();
                cbTipoVehiculo.getItems().clear();
                cbTipoVehiculo.getItems().addAll(TipoVehiculo.values());
                if (selV != null) cbTipoVehiculo.getSelectionModel().select(selV);
                else cbTipoVehiculo.getSelectionModel().selectFirst();
            }

            System.out.println("Idioma aplicado correctamente mediante ResourceBundle: " + locale.getLanguage());
            
        } catch (Exception e) {
            System.err.println("Error al aplicar el idioma con ResourceBundle: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Método para recibir el usuario y actualizar el saludo de bienvenida en pantalla
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        
        if (this.usuarioActual != null) {
            System.out.println("Usuario activo en ventana principal: " + usuarioActual.getNombre());
            
            if (lblBienvenida != null) {
                lblBienvenida.setText("¡Bienvenido, " + usuarioActual.getNombre() + "!");
            }
            
            if (usuarioActual.getRolActivo() == RolUsuario.CONDUCTOR && cbTipoUsuario != null) {
                cbTipoUsuario.getSelectionModel().select("Conductor");
            } else if (cbTipoUsuario != null) {
                cbTipoUsuario.getSelectionModel().select("Cliente (Pasajero)");
            }
        }
        
        // Actualizar indicador de flota al recibir el usuario
        actualizarIndicadorFlota();
    }

    private void actualizarIndicadorFlota() {
        try {
            GestorMovilidad gestor = Aplicacion.getGestor();
            if (gestor == null || gestor.obtenerUsuarios() == null) return;

            long cantDisponibles = gestor.obtenerUsuarios().stream()
                .filter(u -> u.getConductor() != null)
                .filter(u -> u.getConductor().getEstadoConductor() == EstadoConductor.DISPONIBLE)
                .count();

            if (lblEstadoFlota != null) {
                if (cantDisponibles > 0) {
                    lblEstadoFlota.setText("🟢 Conductores disponibles: " + cantDisponibles);
                    lblEstadoFlota.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;");
                } else {
                    lblEstadoFlota.setText("🔴 No hay conductores disponibles");
                    lblEstadoFlota.setStyle("-fx-font-weight: bold; -fx-text-fill: #d32f2f;");
                }
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar la flota: " + e.getMessage());
        }
    }    
}