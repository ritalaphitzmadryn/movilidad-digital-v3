package md.ui.impl.javafx.map;

import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import java.net.URL;
import java.util.function.BiConsumer;
import java.util.Locale;

public class VisualizadorRutasLeaflet {

    private final WebView webView;
    private final WebEngine webEngine;
    private boolean cargado = false;
    private BiConsumer<String, double[]> listenerSeleccion;

    public VisualizadorRutasLeaflet(WebView webView) {
        this.webView = webView;
        this.webEngine = webView.getEngine();
        URL mapUrl = getClass().getResource("/ui/map/mapa.html");
        if (mapUrl != null) {
            webEngine.load(mapUrl.toExternalForm());
        } else {
            System.err.println("No se pudo encontrar el archivo /ui/map/mapa.html");
        }

        webEngine.getLoadWorker().stateProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                cargado = true;
                conectarPuente();
                System.out.println("Mapa cargado exitosamente.");
            }
        });

        if (webEngine.getLoadWorker().getState() == Worker.State.SUCCEEDED) {
            cargado = true;
            conectarPuente();
        }
    }

    private void conectarPuente() {
        try {
            JSObject ventana = (JSObject) webEngine.executeScript("window");
            ventana.setMember("javaBridge", new PuenteSeleccionMapa());
        } catch (Exception e) {
            System.err.println("Error al conectar el puente JavaScript: " + e.getMessage());
        }
    }

    public void configurarSeleccion(BiConsumer<String, double[]> listener) {
        this.listenerSeleccion = listener;
    }

    public void activarSeleccion(String tipo) {
        if (cargado) {
            // Forzar actualización de tamaño antes de activar para corregir desvíos post-zoom
            invalidarTamañoMapa();
            webEngine.executeScript(String.format("activarSeleccion('%s');", tipo));
        } else {
            System.out.println("Advertencia: El mapa aún no está listo para activar selección.");
        }
    }

    public void mostrarMarcador(String tipo, double lat, double lon) {
        if (cargado) {
            invalidarTamañoMapa();
            webEngine.executeScript(String.format("mostrarMarcadorSeleccion('%s', %f, %f);", tipo, lat, lon));
        }
    }

    public void limpiarSeleccionYRutas() {
        if (cargado) {
            try {
                webEngine.executeScript("limpiarMapaCompleto();");
                System.out.println("Script limpiarMapaCompleto ejecutado en JS.");
            } catch (Exception e) {
                System.err.println("Error al limpiar el mapa: " + e.getMessage());
            }
        } else {
            System.err.println("No se pudo limpiar: el mapa aún no está cargado.");
        }
    }

    public void invalidarTamañoMapa() {
        if (cargado) {
            try {
                webEngine.executeScript("if(typeof actualizarTamanoMapa === 'function') { actualizarTamanoMapa(); }");
            } catch (Exception e) {
                System.err.println("Error al actualizar tamaño del mapa: " + e.getMessage());
            }
        }
    }

    public void trazarRutasCompletas(double latM, double lonM, double latO, double lonO, double latD, double lonD, String jsonCoordsAcercamiento, String jsonCoordsTraslado) {
        if (cargado) {
            String acercamiento = (jsonCoordsAcercamiento != null && !jsonCoordsAcercamiento.isEmpty()) ? jsonCoordsAcercamiento : "[]";
            String traslado = (jsonCoordsTraslado != null && !jsonCoordsTraslado.isEmpty()) ? jsonCoordsTraslado : "[]";

            String script = String.format(Locale.US, "trazarRutasCompletas(%f, %f, %f, %f, %f, %f, %s, %s);", 
                latM, lonM, latO, lonO, latD, lonD, acercamiento, traslado);
            webEngine.executeScript(script);
        }
    }

    public void trazarRutasCompletas(double latM, double lonM, double latO, double lonO, double latD, double lonD, String jsArrayCoordenadasTraslado) {
        trazarRutasCompletas(latM, lonM, latO, lonO, latD, lonD, "[]", jsArrayCoordenadasTraslado);
    }

    public class PuenteSeleccionMapa {
        public void seleccionarPunto(String tipo, double lat, double lon) {
            Platform.runLater(() -> {
                if (listenerSeleccion != null) {
                    listenerSeleccion.accept(tipo, new double[]{lat, lon});
                }
            });
        }
    }
}