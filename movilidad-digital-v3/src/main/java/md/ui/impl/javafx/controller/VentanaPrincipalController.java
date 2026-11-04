package md.ui.impl.javafx.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

// Importamos la interfaz y la implementación concreta del servicio de rutas
import md.rutas.api.IRutasService;
import md.rutas.impl.IRutasServiceImpl;

public class VentanaPrincipalController implements Initializable {
	
    @FXML
    private ComboBox<String> cbOrigen;

    @FXML
    private ComboBox<String> cbDestino;

    @FXML
    private ComboBox<String> cbTipoServicio;

    @FXML
    private Button btnSolicitar;

    @FXML
    private WebView mapWebView;

    private WebEngine webEngine;

    // Instanciamos directamente nuestra implementación concreta
    private IRutasService rutasService = new IRutasServiceImpl();

    private void actualizarTamanoMapa() {
        if (webEngine != null) {
            javafx.application.Platform.runLater(() -> {
                try {
                    webEngine.executeScript(
                        "if (typeof actualizarTamanoMapa === 'function') {" +
                        "    actualizarTamanoMapa();" +
                        "}"
                    );
                } catch (Exception e) {
                    System.err.println(
                        "Error al actualizar el tamaño del mapa: " + e.getMessage()
                    );
                }
            });
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Inicializamos el servicio de rutas de forma explícita
        this.rutasService = new IRutasServiceImpl();

        // 1. Cargar las opciones del ComboBox de tipo de servicio
        if (cbTipoServicio != null) {
            cbTipoServicio.getItems().addAll("Económico", "Standard", "Confort", "VIP");
            cbTipoServicio.getSelectionModel().selectFirst();
        }

        // 2. Cargar las opciones completas en los ComboBox de Origen y Destino
        if (cbOrigen != null && cbDestino != null) {
            ObservableList<String> ubicaciones = FXCollections.observableArrayList(
                "Municipalidad de Puerto Madryn",
                "Puerto Almirante Brown / Storni",
                "Universidad del Chubut (UDC)",
                "Universidad Tecnológica Nacional (UTN)",
                "Plaza San Martín",
                "Terminal de Ómnibus",
                "Ecocentro",
                "Muelle Luis Piedra Buena",
                "Universidad (UNPSJB)",
                "Parque Industrial",
                "Supermercado La Anónima",
                "Hipermercado Changomás",
                "Cine Teatro Auditorium",
                "Escuela N° 728",
                "Escuela N° 7707",
                "Polideportivo Municipal"
            );
            
            cbOrigen.setItems(ubicaciones);
            cbDestino.setItems(ubicaciones);
        }
        
        // 3. Configuración del mapa (WebView)
        if (mapWebView != null) {
            webEngine = mapWebView.getEngine();

            URL mapUrl = getClass().getResource("/ui/map/mapa.html");

            if (mapUrl != null) {
                // Escuchar cambios de tamaño del WebView
                mapWebView.widthProperty().addListener(
                    (obs, oldVal, newVal) -> actualizarTamanoMapa()
                );

                mapWebView.heightProperty().addListener(
                    (obs, oldVal, newVal) -> actualizarTamanoMapa()
                );

                // Esperar a que el HTML termine de cargar
                webEngine.getLoadWorker().stateProperty().addListener(
                    (obs, oldState, newState) -> {
                        if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                            actualizarTamanoMapa();
                        }
                    }
                );

                // Cargar el archivo HTML en el motor
                webEngine.load(mapUrl.toExternalForm());
            } else {
                System.err.println("No se pudo encontrar el archivo mapa.html");
            }
        }
            
        // 4. Vincular el botón
        if (btnSolicitar != null) {
            btnSolicitar.setOnAction(event -> handleSolicitarViaje());
        }
    }

    @FXML
    private void handleSolicitarViaje() {
        // 1. Obtener los valores seleccionados de los ComboBox
        String origen = cbOrigen != null ? cbOrigen.getValue() : null;
        String destino = cbDestino != null ? cbDestino.getValue() : null;
        String tipoServicio = cbTipoServicio != null ? cbTipoServicio.getValue() : "Standard";
        
        // 2. Validaciones básicas de seguridad
        if (origen == null || origen.isEmpty() || destino == null || destino.isEmpty()) {
            System.err.println("Error: Debe seleccionar un origen y un destino.");
            return;
        }
        
        if (origen.equals(destino)) {
            System.err.println("Error: El origen y el destino no pueden ser el mismo lugar.");
            return;
        }
        
        System.out.println("--- SOLICITUD DE VIAJE (" + tipoServicio + ") ---");
        System.out.println("Desde: " + origen + " -> Hacia: " + destino);
        
        // 3. Obtener las coordenadas geográficas de los lugares seleccionados
        double[] coordsOrigen = obtenerCoordenadas(origen);
        double[] coordsDestino = obtenerCoordenadas(destino);
        
        // 4. Utilizar IRutasService para calcular la ruta y la distancia estimada
        if (rutasService != null) {
            String detalleRuta = rutasService.calcularRuta(coordsOrigen[0], coordsOrigen[1], coordsDestino[0], coordsDestino[1]);
            double distanciaKm = rutasService.calcularDistanciaKm(coordsOrigen[0], coordsOrigen[1], coordsDestino[0], coordsDestino[1]);
            
            System.out.println("Detalle de ruta (Servicio): " + detalleRuta);
            System.out.println(String.format(Locale.US, "Distancia calculada: %.2f km", distanciaKm));
        } else {
            System.out.println("Aviso: IRutasService no está inicializado todavía.");
        }
        
        // 5. Ejecutar la función de JavaScript en el WebView para trazar la ruta en Leaflet
        if (webEngine != null) {
            String script = String.format(Locale.US, "trazarRuta(%f, %f, %f, %f);", 
                coordsOrigen[0], coordsOrigen[1], 
                coordsDestino[0], coordsDestino[1]
            );
            webEngine.executeScript(script);
        } else {
            System.err.println("El motor del mapa (WebEngine) no está inicializado.");
        }
    }
    
    // Método auxiliar para mapear cada ubicación con sus coordenadas reales en Puerto Madryn
    private double[] obtenerCoordenadas(String lugar) {
        switch (lugar) {
            case "Municipalidad de Puerto Madryn": 
                return new double[]{-42.7692, -65.0385};
            case "Puerto Almirante Brown / Storni": 
                return new double[]{-42.7480, -65.0310};
            case "Universidad del Chubut (UDC)": 
                return new double[]{-42.7820, -65.0420};
            case "Universidad Tecnológica Nacional (UTN)": 
                return new double[]{-42.7735, -65.0610};
            case "Plaza San Martín": 
                return new double[]{-42.7685, -65.0350};
            case "Terminal de Ómnibus": 
                return new double[]{-42.7710, -65.0700};
            case "Ecocentro": 
                return new double[]{-42.7915, -65.0220};
            case "Muelle Luis Piedra Buena": 
                return new double[]{-42.7575, -65.0350};
            case "Universidad (UNPSJB)": 
                return new double[]{-42.7750, -65.0500};
            case "Parque Industrial": 
                return new double[]{-42.7950, -65.0800};
            case "Supermercado La Anónima": 
                return new double[]{-42.7640, -65.0430};
            case "Hipermercado Changomás": 
                return new double[]{-42.7720, -65.0650};
            case "Cine Teatro Auditorium": 
                return new double[]{-42.7670, -65.0360};
            case "Escuela N° 728": 
                return new double[]{-42.7660, -65.0500};
            case "Escuela N° 7707": 
                return new double[]{-42.7780, -65.0550};
            case "Polideportivo Municipal": 
                return new double[]{-42.7610, -65.0720};
            default: 
                return new double[]{-42.7692, -65.0385}; // Coordenada por defecto (Centro)
        }
    }
}