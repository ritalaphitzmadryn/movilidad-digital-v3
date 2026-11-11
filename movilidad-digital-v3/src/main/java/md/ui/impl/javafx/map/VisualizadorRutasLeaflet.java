package md.ui.impl.javafx.map;

import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class VisualizadorRutasLeaflet extends StackPane {

    private final WebView webView;
    private final WebEngine webEngine;
    private boolean cargado = false;

    // Guardamos las últimas coordenadas por si el mapa se termina de cargar después
    private double ultimaLatOrigen;
    private double ultimaLonOrigen;
    private double ultimaLatDestino;
    private double ultimaLonDestino;
    private boolean pendienteTrazar = false;

    public VisualizadorRutasLeaflet() {
        webView = new WebView();
        webEngine = webView.getEngine();

        // Cargamos el archivo HTML del mapa desde los recursos (ej: /mapa.html o src/main/resources)
        // Asegurate de que el archivo HTML esté en resources o en una ruta accesible
        String urlMapa = getClass().getResource("/mapa.html").toExternalForm();
        webEngine.load(urlMapa);

        // Listener para asegurarnos de que el DOM esté listo antes de inyectar scripts
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                cargado = true;
                // Si se intentó trazar una ruta antes de que cargue el mapa, la ejecutamos ahora
                if (pendienteTrazar) {
                    ejecutarTrazarRuta(ultimaLatOrigen, ultimaLonOrigen, ultimaLatDestino, ultimaLonDestino);
                    pendienteTrazar = false;
                }
            }
        });

        this.getChildren().add(webView);
    }

    /**
     * Método público para trazar la ruta asegurando el orden correcto:
     * 1° Latitud, 2° Longitud.
     */
    public void mostrarRuta(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
        if (!cargado) {
            // Guardamos temporalmente si el WebView aún se está cargando
            this.ultimaLatOrigen = latOrigen;
            this.ultimaLonOrigen = lonOrigen;
            this.ultimaLatDestino = latDestino;
            this.ultimaLonDestino = lonDestino;
            this.pendienteTrazar = true;
            return;
        }

        ejecutarTrazarRuta(latOrigen, lonOrigen, latDestino, lonDestino);
    }

    private void ejecutarTrazarRuta(double latO, double lonO, double latD, double lonD) {
        // Inyectamos el script asegurando que los argumentos vayan en orden: (lat, lon, lat, lon)
        String script = String.format("trazarRuta(%.6f, %.6f, %.6f, %.6f);", latO, lonO, latD, lonD);
        webEngine.executeScript(script);
    }
}