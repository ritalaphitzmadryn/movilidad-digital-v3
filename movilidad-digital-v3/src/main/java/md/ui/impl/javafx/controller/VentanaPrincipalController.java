package md.ui.impl.javafx.controller;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
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

public class VentanaPrincipalController implements Initializable {
	
    @FXML private Label lblResultadoViaje; 
    @FXML private Label lblOrigenInfo;
    @FXML private Label lblDestinoInfo;
    @FXML private Label lblBienvenida; 
    
    @FXML private ComboBox<String> cbTipoUsuario;
    @FXML private ComboBox<String> cbTipoServicio; // Para Categoría (Económico, Standard, etc.)
    
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
                    if ("Conductor".equals(seleccion)) {
                        if (usuarioActual.getConductor() != null) {
                            usuarioActual.cambiarRolActivo(RolUsuario.CONDUCTOR);
                            System.out.println("Rol cambiado a: CONDUCTOR");
                        } else {
                            System.out.println("El usuario no tiene perfil de conductor.");
                            cbTipoUsuario.getSelectionModel().select("Cliente (Pasajero)");
                        }
                    } else {
                        if (usuarioActual.getCliente() != null) {
                            usuarioActual.cambiarRolActivo(RolUsuario.CLIENTE);
                            System.out.println("Rol cambiado a: CLIENTE");
                        } else {
                            System.out.println("El usuario no tiene perfil de cliente.");
                            cbTipoUsuario.getSelectionModel().select("Conductor");
                        }
                    }
                }
            });
        }

        // Poblar ComboBox de Categoría
        if (cbTipoServicio != null) {
            cbTipoServicio.getItems().addAll("Económico", "Standard", "Confort", "VIP");
            cbTipoServicio.getSelectionModel().selectFirst();
        }

        // Poblar ComboBox para TipoServicio (Enum: PASAJEROS, ENVIOS)
        if (cbTipoServicioEnum != null) {
            cbTipoServicioEnum.getItems().addAll(TipoServicio.values());
            cbTipoServicioEnum.getSelectionModel().selectFirst();
        }

        // Poblar ComboBox para TipoVehiculo (Enum: AUTO, MOTO)
        if (cbTipoVehiculo != null) {
            cbTipoVehiculo.getItems().addAll(TipoVehiculo.values());
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
                    
                    visualizadorMapa.activarSeleccion("destino");
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

        // Vincular el botón de limpiar selecciones / arrepentimiento de opciones
        if (btnLimpiar != null) {
            btnLimpiar.setOnAction(e -> {
                if (visualizadorMapa != null) {
                    visualizadorMapa.limpiarSeleccionYRutas();
                }
                if (lblOrigenInfo != null) {
                    lblOrigenInfo.setText("No seleccionado");
                }
                if (lblDestinoInfo != null) {
                    lblDestinoInfo.setText("No seleccionado");
                }
                if (lblResultadoViaje != null) {
                    lblResultadoViaje.setText("");
                }
                
                // Arrepentimiento: Devolver los ComboBox a su selección inicial por defecto
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
                System.out.println("Mapa, selecciones y opciones restablecidas correctamente.");
                
                actualizarIndicadorFlota(); // Actualizar indicador al limpiar
            });
        }

        // Llamar al indicador de flota al arrancar la ventana
        actualizarIndicadorFlota();
    }

    @FXML
    private void handleSolicitarViaje() {
        try {
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
            double latMovil = latOrigen + 0.01; 
            double lonMovil = lonOrigen + 0.01;
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

            // 3. Costo y tiempos estimados
            double tiempoViajeMin = (distanciaViajeKm / 30.0) * 60.0;
            double tiempoAcercamientoMin = (menorDistanciaMovil / 30.0) * 60.0;

            Servicio servicioEjemplo = new Servicio(
                categoriaElegida, 
                tipoServicioElegido, 
                tipoVehiculoElegido, 
                CategoriaVehiculo.ESTANDAR, 
                100.0, 50.0, 10.0
            ); 
            double costoTotal = servicioEjemplo.calcularCosto(distanciaViajeKm, tiempoViajeMin);

            javafx.stage.Stage dialogStage = new javafx.stage.Stage();
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Movilidad Digital - Confirmación de Servicio");

            javafx.scene.layout.VBox vbox = new javafx.scene.layout.VBox(15);
            vbox.setStyle("-fx-background-color: #ffffff; -fx-padding: 25px; -fx-border-color: #2e7d32; -fx-border-width: 3px;");
            vbox.setAlignment(javafx.geometry.Pos.TOP_LEFT);

            javafx.scene.control.Label lblTitulo = new javafx.scene.control.Label("Resumen del Servicio (" + tipoServicioElegido + ")");
            lblTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2e7d32;");

            String textoDetalle = String.format(
                Locale.US, 
                "Vehículo seleccionado: %s (%s)\n" +
                "Distancia del trayecto: %.2f km\n" +
                "Tiempo Estimado: ~%.0f min\n" +
                "Costo Total Estimado: $%.2f\n\n" +
                "¿Desea confirmar la solicitud y buscar un móvil?",
                tipoVehiculoElegido, categoriaElegida, distanciaViajeKm, tiempoViajeMin, costoTotal
            );
            javafx.scene.control.Label lblContenido = new javafx.scene.control.Label(textoDetalle);
            lblContenido.setStyle("-fx-font-size: 14px; -fx-text-fill: #333333; -fx-line-spacing: 5px;");

            javafx.scene.control.Button btnConfirmar = new javafx.scene.control.Button("Confirmar y Buscar");
            btnConfirmar.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-cursor: hand;");

            javafx.scene.control.Button btnArrepentirse = new javafx.scene.control.Button("🔄 Arrepentirse");
            btnArrepentirse.setStyle("-fx-background-color: #ff9800; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-cursor: hand;");
            
            javafx.scene.control.Button btnCancelar = new javafx.scene.control.Button("Cancelar");
            btnCancelar.setStyle("-fx-background-color: #d32f2f; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8px 16px; -fx-cursor: hand;");

            javafx.scene.layout.HBox botonesBox = new javafx.scene.layout.HBox(12, btnConfirmar, btnCancelar);
            botonesBox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);

            vbox.getChildren().addAll(lblTitulo, lblContenido, botonesBox);

            javafx.scene.Scene scene = new javafx.scene.Scene(vbox, 480, 260);
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

            // Si el usuario presionó confirmar, ejecutamos el flujo completo de seguimiento, mapa y diálogo unificado
            if (confirmado[0]) {
            	if (visualizadorMapa != null) {
            	    // Recorrido en "L" invertido (Primero se mueve horizontal y luego vertical)
            	    String geometriaCalleSimulada = String.format(
            	        Locale.US, 
            	        "[[%f, %f], [%f, %f], [%f, %f]]",
            	        latOrigen, lonOrigen,   // 1. Inicio
            	        latOrigen, lonDestino,  // 2. Esquina
            	        latDestino, lonDestino  // 3. Destino final
            	    );

            	    visualizadorMapa.trazarRutasCompletas(latMovil, lonMovil, latOrigen, lonOrigen, latDestino, lonDestino, geometriaCalleSimulada);
            	}

                // Desplegar el panel completo con Progreso, Métricas, Detalles, Historial y Estrellas
                mostrarResumenEHistorialViaje(distanciaViajeKm, costoTotal, patenteMovil, tipoServicioElegido, tipoVehiculoElegido, categoriaElegida, tiempoViajeMin);
                
                // Refrescar indicador de flota
                actualizarIndicadorFlota();
            }
        } catch (Exception e) {
            System.err.println("Error al procesar el servicio: " + e.getMessage());
            e.printStackTrace();
        }
    }
    

    // =========================================================================
    // VENTANA DE SEGUIMIENTO, RESUMEN, HISTORIAL Y CALIFICACIÓN UNIFICADA
    // =========================================================================
    private void mostrarResumenEHistorialViaje(double distanciaKm, double costoTotal, String patenteMovil, TipoServicio tipoServicio, TipoVehiculo tipoVehiculo, String categoria, double tiempoMin) {
        javafx.scene.control.Dialog<ButtonType> dialogo = new javafx.scene.control.Dialog<>();
        dialogo.setTitle("Movilidad Digital - Seguimiento de mi pedido");
        dialogo.setHeaderText("El viaje finalizó. ¡Gracias por usar Movilidad Digital!");

        javafx.scene.control.DialogPane pane = dialogo.getDialogPane();
        pane.setPrefWidth(520);
        pane.setPrefHeight(640);
        pane.setStyle("-fx-background-color: #ffffff; -fx-border-color: #2e7d32; -fx-border-width: 2px;");

        VBox root = new VBox(12);
        root.setPadding(new Insets(15));
        root.setStyle("-fx-background-color: #f9f9f9;");

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: #ffffff; -fx-background-color: transparent;");

        // 1. PROGRESO DEL VIAJE (Stepper)
        Label lblProgresoTitulo = new Label("Progreso del viaje");
        lblProgresoTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333; -fx-font-size: 13px;");

        VBox boxProgreso = new VBox(8); // Cambiado a VBox para que se apilen verticalmente
        boxProgreso.setAlignment(Pos.CENTER_LEFT); // Puedes usar Pos.CENTER si los quieres centrados
        String[] estados = {"1-Solicitado", "2-Aceptado", "3-En viaje", "4-Finalizado"};
        for (String estado : estados) {
            Label lblEstado = new Label(estado);
            lblEstado.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-padding: 4px 10px; -fx-background-radius: 12px; -fx-font-size: 11px; -fx-font-weight: bold;");
            boxProgreso.getChildren().add(lblEstado);
        }

        // 2. MÉTRICAS (Tiempo, Distancia, Costo)
        HBox boxMetricas = new HBox(10);
        boxMetricas.setAlignment(Pos.CENTER);
        
        VBox cardTiempo = crearTarjetaMetrica("TIEMPO ESTIMADO", String.format(Locale.US, "~%.0f min", tiempoMin));
        VBox cardDistancia = crearTarjetaMetrica("DISTANCIA APROX", String.format(Locale.US, "%.2f km", distanciaKm));
        VBox cardCosto = crearTarjetaMetrica("COSTO ESTIMADO", String.format(Locale.US, "$ %.2f", costoTotal));
        cardCosto.setStyle("-fx-background-color: white; -fx-border-color: #2e7d32; -fx-border-radius: 4px; -fx-padding: 8px 12px; -fx-alignment: center;");
        
        boxMetricas.getChildren().addAll(cardTiempo, cardDistancia, cardCosto);

        // 3. DETALLES DEL PEDIDO
        Label lblDetallesTitulo = new Label("Detalles del pedido");
        lblDetallesTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333; -fx-font-size: 13px;");

        VBox boxDetalles = new VBox(5);
        boxDetalles.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: #dddddd; -fx-border-radius: 5px;");
        
        String nombreCliente = usuarioActual != null ? usuarioActual.getNombre() : "Cliente General";
        Label l1 = new Label("Pasajero: " + nombreCliente + " - +549115545146");
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

        Label l2 = new Label(textoConductor);
        Label l3 = new Label("Vehículo: " + tipoVehiculo + " - " + patenteMovil + " - " + categoria);
        Label l4 = new Label("Servicio: " + categoria + " (" + tipoServicio + ")");
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

        // 4. HISTORIAL DE EVENTOS
        Label lblHistorialTitulo = new Label("Historial de eventos");
        lblHistorialTitulo.setStyle("-fx-font-weight: bold; -fx-text-fill: #333333; -fx-font-size: 13px;");

        VBox boxHistorial = new VBox(3);
        boxHistorial.setStyle("-fx-background-color: white; -fx-padding: 10px; -fx-border-color: #dddddd; -fx-border-radius: 5px; -fx-font-family: 'Consolas', monospace;");
        
        java.time.format.DateTimeFormatter dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        String t1 = java.time.LocalDateTime.now().minusMinutes(3).format(dtf);
        String t2 = java.time.LocalDateTime.now().minusMinutes(2).format(dtf);
        String t3 = java.time.LocalDateTime.now().minusMinutes(1).format(dtf);
        String t4 = java.time.LocalDateTime.now().format(dtf);

        boxHistorial.getChildren().addAll(
            new Label(t1 + "  Solicitado"),
            new Label(t2 + "  Aceptado"),
            new Label(t3 + "  En viaje"),
            new Label(t4 + "  Finalizado")
        );
        for (javafx.scene.Node node : boxHistorial.getChildren()) {
            if (node instanceof Label) {
                ((Label) node).setStyle("-fx-text-fill: #333333; -fx-font-size: 11px;");
            }
        }

        // 5. CALIFICACIÓN INTERACTIVA (Estrellas)
        Label lblCalifica = new Label("Califica tu experiencia de viaje:");
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

            // Contar cuántos conductores tienen el estado DISPONIBLE
            long cantDisponibles = gestor.obtenerUsuarios().stream()
                .filter(u -> u.getConductor() != null)
                .filter(u -> u.getConductor().getEstadoConductor() == EstadoConductor.DISPONIBLE)
                .count();

            // Actualizar la etiqueta visual en la interfaz
            if (lblEstadoFlota != null) {
                if (cantDisponibles > 0) {
                    lblEstadoFlota.setText("🟢 Conductores disponibles: " + cantDisponibles);
                    lblEstadoFlota.setStyle("-fx-font-weight: bold; -fx-text-fill: #2e7d32;"); // Verde
                } else {
                    lblEstadoFlota.setText("🔴 No hay conductores disponibles");
                    lblEstadoFlota.setStyle("-fx-font-weight: bold; -fx-text-fill: #d32f2f;"); // Rojo
                }
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar la flota: " + e.getMessage());
        }
    }    
}