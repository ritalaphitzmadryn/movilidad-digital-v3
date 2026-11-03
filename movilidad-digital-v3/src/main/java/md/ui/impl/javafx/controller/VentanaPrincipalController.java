package md.ui.impl.javafx.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class VentanaPrincipalController implements Initializable {

    @FXML
    private TextField txtOrigen;

    @FXML
    private TextField txtDestino;

    @FXML
    private ComboBox<String> cbTipoServicio;

    @FXML
    private Button btnSolicitar;

    @FXML
    private WebView mapWebView; // <- Declarado correctamente con @FXML

    private WebEngine webEngine;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (mapWebView != null) {
            webEngine = mapWebView.getEngine();
            
            // Listener para asegurar que Leaflet recargue el tamaño al cargar la página y elimine los grises
            webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                    webEngine.executeScript("if(typeof map !== 'undefined') { map.invalidateSize(true); }");
                }
            });

            // Cargar el archivo HTML del mapa
            URL mapUrl = getClass().getResource("/ui/map/mapa.html");
            if (mapUrl != null) {
                webEngine.load(mapUrl.toExternalForm());
            } else {
                System.err.println("No se pudo encontrar el archivo mapa.html");
            }
        }
        
        // Vincular el evento del botón
        if (btnSolicitar != null) {
            btnSolicitar.setOnAction(event -> handleSolicitarViaje());
        }
    }

    @FXML
    private void handleSolicitarViaje() {
        String origen = txtOrigen.getText();
        String destino = txtDestino.getText();
        
        System.out.println("Solicitando viaje de: " + origen + " a " + destino);
        
        // Coordenadas de prueba en Puerto Madryn
        double latOrigen = -42.7692;
        double lonOrigen = -65.0385;
        double latDestino = -42.7750;
        double lonDestino = -65.0500;
        
        // Usar Locale.US asegura que los decimales usen punto (.) y no coma (,) para JavaScript
        if (webEngine != null) {
            String script = String.format(Locale.US, "trazarRuta(%f, %f, %f, %f);", latOrigen, lonOrigen, latDestino, lonDestino);
            webEngine.executeScript(script);
        }
    }
}