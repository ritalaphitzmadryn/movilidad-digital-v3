package md.ui.impl.javafx.controller;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import md.aplicacion.Aplicacion;
import md.logica.GestorMovilidad;
import md.modelo.CategoriaVehiculo;
import md.modelo.Conductor;
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
    
    @FXML private Button btnSolicitar;
    @FXML private Button btnPincharOrigen;
    @FXML private Button btnPincharDestino;
    @FXML private Button btnLimpiar; 
    @FXML private WebView mapWebView; 

    private VisualizadorRutasLeaflet visualizadorMapa;
    private IRutasService rutasService = new RutasServiceImpl();
    private Usuario usuarioActual;

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

        // Vincular el botón de limpiar selecciones
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
                
                origenSeleccionado = false;
                destinoSeleccionado = false;
                System.out.println("Mapa y selecciones limpiadas correctamente.");
            });
        }
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

            // Si el usuario presionó confirmar, ejecutamos el resto del flujo
            if (confirmado[0]) {
                
                // --- PASO 2: ACTUALIZACIÓN DINÁMICA DE ESTADOS DEL VIAJE ---
                if (lblResultadoViaje != null) {
                    lblResultadoViaje.setText("Estado: [SOLICITUD] - Buscando móvil cercano...");
                }

                String resultadoTexto = String.format(
                    Locale.US, 
                    "Estado: [ACEPTADO] / [INICIADO]\n" +
                    "Modalidad: %s | Vehículo: %s\n" +
                    "Móvil asignado: %s (a %.2f km | ~%.0f min)\n" +
                    "Distancia del viaje: %.2f km (~%.0f min)\n" +
                    "Costo Total: $%.2f\n%s", 
                    tipoServicioElegido, tipoVehiculoElegido,
                    patenteMovil, menorDistanciaMovil, tiempoAcercamientoMin,
                    distanciaViajeKm, tiempoViajeMin, 
                    costoTotal, detalleRuta
                );
                
                if (lblResultadoViaje != null) {
                    lblResultadoViaje.setText(resultadoTexto);
                }

                if (visualizadorMapa != null) {
                    String geometriaCalleSimulada = String.format(Locale.US, "[[%f, %f], [%f, %f], [%f, %f]]",
                        latOrigen, lonOrigen,
                        (latOrigen + latDestino) / 2 + 0.002, (lonOrigen + lonDestino) / 2 - 0.002,
                        latDestino, lonDestino
                    );

                    visualizadorMapa.trazarRutasCompletas(latMovil, lonMovil, latOrigen, lonOrigen, latDestino, lonDestino, geometriaCalleSimulada);
                }

                mostrarDialogoCalificacion();
            }
        } catch (Exception e) {
            System.err.println("Error al procesar el servicio: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método auxiliar para la calificación del servicio (simulando CalificacionViaje)
    private void mostrarDialogoCalificacion() {
        Alert dialogoCalificacion = new Alert(Alert.AlertType.INFORMATION);
        dialogoCalificacion.setTitle("Servicio Finalizado");
        dialogoCalificacion.setHeaderText("Estado: [FINALIZADO] - ¡Destino alcanzado!");
        dialogoCalificacion.setContentText("Gracias por usar Movilidad Digital. Calificación y registro completados con éxito.");
        
        // Estilizar también el diálogo de finalización
        DialogPane paneCalificacion = dialogoCalificacion.getDialogPane();
        paneCalificacion.setPrefWidth(450);
        paneCalificacion.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-background-color: #ffffff; " +
            "-fx-border-color: #2e7d32; " +
            "-fx-border-width: 2px;"
        );
        paneCalificacion.lookup(".content.label").setStyle("-fx-font-size: 14px; -fx-text-fill: #333333;");

        dialogoCalificacion.showAndWait();
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
    }
}