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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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
            
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    webEngine.executeScript("if(typeof map !== 'undefined') { map.invalidateSize(true); }");
                }
            });

            URL mapUrl = getClass().getResource("/ui/map/mapa.html");
            if (mapUrl != null) {
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
        String origen = cbOrigen != null ? cbOrigen.getValue() : null;
        String destino = cbDestino != null ? cbDestino.getValue() : null;
        String tipoServicio = cbTipoServicio != null ? cbTipoServicio.getValue() : "";
        
        // Validaciones básicas
        if (origen == null || destino == null || origen.isEmpty() || destino.isEmpty()) {
            System.err.println("Por favor seleccione un origen y un destino válidos.");
            return;
        }
        
        if (origen.equals(destino)) {
            System.err.println("El origen y el destino no pueden ser iguales.");
            return;
        }
        
        System.out.println("Solicitando viaje (" + tipoServicio + ") de: " + origen + " a " + destino);
        
        // Obtener coordenadas reales según el lugar seleccionado
        double[] coordsOrigen = obtenerCoordenadas(origen);
        double[] coordsDestino = obtenerCoordenadas(destino);
        
        // Ejecutar el script en el mapa de Leaflet para trazar la ruta
        if (webEngine != null) {
            String script = String.format(Locale.US, "trazarRuta(%f, %f, %f, %f);", 
                coordsOrigen[0], coordsOrigen[1], coordsDestino[0], coordsDestino[1]);
            webEngine.executeScript(script);
        }
    }
    
    // Mapeo completo de coordenadas reales para cada ubicación en Puerto Madryn
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
                return new double[]{-42.7692, -65.0385}; // Centro por defecto
        }
    }
}