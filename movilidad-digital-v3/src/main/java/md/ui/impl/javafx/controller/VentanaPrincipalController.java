package md.ui.impl.javafx.controller;

import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import md.aplicacion.Aplicacion;
import md.logica.GestorMovilidad;
import md.modelo.CategoriaVehiculo;
import md.modelo.Conductor;
import md.modelo.Servicio;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import md.modelo.Usuario;
import md.rutas.api.IRutasService;
import md.rutas.impl.IRutasServiceImpl;
import md.ui.impl.javafx.map.VisualizadorRutasLeaflet;

public class VentanaPrincipalController implements Initializable {
	
    @FXML private Label lblResultadoViaje; 
    @FXML private Label lblOrigenInfo;
    @FXML private Label lblDestinoInfo;
    
    @FXML private ComboBox<String> cbTipoUsuario;
    @FXML private ComboBox<String> cbTipoServicio;
    
    @FXML private Button btnSolicitar;
    @FXML private Button btnPincharOrigen;
    @FXML private Button btnPincharDestino;
    @FXML private WebView mapWebView; 

    private VisualizadorRutasLeaflet visualizadorMapa;
    private IRutasService rutasService = new IRutasServiceImpl();

    // Coordenadas iniciales por defecto en Puerto Madryn
    private double latOrigen = -42.7692;
    private double lonOrigen = -65.0385;
    private double latDestino = -42.7480;
    private double lonDestino = -65.0310;
    
    private boolean origenSeleccionado = false;
    private boolean destinoSeleccionado = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.rutasService = new IRutasServiceImpl();

        if (cbTipoUsuario != null) {
            cbTipoUsuario.getItems().addAll("Cliente (Pasajero)", "Conductor");
            cbTipoUsuario.getSelectionModel().selectFirst();
        }

        if (cbTipoServicio != null) {
            cbTipoServicio.getItems().addAll("Económico", "Standard", "Confort", "VIP");
            cbTipoServicio.getSelectionModel().selectFirst();
        }
        
        // Inicializar el Visualizador pasando mapWebView directamente
        if (mapWebView != null) {
            visualizadorMapa = new VisualizadorRutasLeaflet(mapWebView);

            // Configurar el listener de clics devueltos por el mapa (Sección 4.9 del apunte)
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
                    
                    // Activar automáticamente la selección de destino para el segundo clic
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

        if (btnSolicitar != null) {
            btnSolicitar.setOnAction(event -> handleSolicitarViaje());
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

            // 2. Buscar el conductor / móvil más cercano disponible
            double menorDistanciaMovil = Double.MAX_VALUE;
            double latMovil = latOrigen - 0.01; 
            double lonMovil = lonOrigen - 0.01;
            String patenteMovil = "AA-123-BB";

            List<Usuario> usuarios = gestor.obtenerUsuarios();
            for (Usuario u : usuarios) {
                if (u.getConductor() != null) {
                    Conductor c = u.getConductor();
                    double cLat = latOrigen - 0.015; 
                    double cLon = lonOrigen - 0.015;
                    
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

            // 3. Costo y tiempos estimados
            double tiempoViajeMin = (distanciaViajeKm / 30.0) * 60.0;
            double tiempoAcercamientoMin = (menorDistanciaMovil / 30.0) * 60.0;

            Servicio servicioEjemplo = new Servicio(
                cbTipoServicio != null ? cbTipoServicio.getValue() : "Standard", 
                TipoServicio.PASAJEROS, TipoVehiculo.AUTO, CategoriaVehiculo.ESTANDAR, 
                100.0, 50.0, 10.0
            ); 
            double costoTotal = servicioEjemplo.calcularCosto(distanciaViajeKm, tiempoViajeMin);

            // 4. Mostrar información en la etiqueta inferior (`lblResultadoViaje`)
            String resultadoTexto = String.format(
                Locale.US, 
                "Móvil más cercano: %s (a %.2f km | ~%.0f min)\n" +
                "Distancia del viaje: %.2f km (~%.0f min)\n" +
                "Costo Total: $%.2f\n%s", 
                patenteMovil, menorDistanciaMovil, tiempoAcercamientoMin,
                distanciaViajeKm, tiempoViajeMin, 
                costoTotal, detalleRuta
            );
            
            if (lblResultadoViaje != null) {
                lblResultadoViaje.setText(resultadoTexto);
            }

            // 5. Enviar instrucciones al mapa para dibujar acercamiento (azul) y traslado (verde)
            if (visualizadorMapa != null) {
                visualizadorMapa.trazarRutasCompletas(latMovil, lonMovil, latOrigen, lonOrigen, latDestino, lonDestino);
            }

        } catch (Exception e) {
            System.err.println("Error al procesar el viaje: " + e.getMessage());
            e.printStackTrace();
        }
    }
}