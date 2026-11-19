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
    
   // Declarar el puente como atributo de instancia para evitar que el GC lo destruya
    private final PuenteSeleccionMapa puente = new PuenteSeleccionMapa();

    // Constructor que recibe el WebView del FXML (mapWebView)
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
            }
        });
    }

    private void conectarPuente() {
        try {
            JSObject ventana = (JSObject) webEngine.executeScript("window");
            // se utiliza el objeto puente instanciado ( Linea 19)
            ventana.setMember("javaBridge", this.puente);
            System.out.println("Puente JavaScript (javaBridge) conectado exitosamente desde Java.");
        } catch (Exception e) {
            System.err.println("Error al conectar el puente JavaScript: " + e.getMessage());
        }
    }

    public void configurarSeleccion(BiConsumer<String, double[]> listener) {
        this.listenerSeleccion = listener;
    }

    public void activarSeleccion(String tipo) {
        if (cargado) {
            webEngine.executeScript(String.format("activarSeleccion('%s');", tipo));
        }
    }

    public void mostrarMarcador(String tipo, double lat, double lon) {
        if (cargado) {
            webEngine.executeScript(String.format(Locale.US, "mostrarMarcadorSeleccion('%s', %f, %f);", tipo, lat, lon));
        }
    }
    public void trazarRutasCompletas(double latM, double lonM, double latO, double lonO, double latD, double lonD, String jsonCoordsAcercamiento, String jsonCoordsTraslado) {
        if (cargado) {
            String acercamiento = (jsonCoordsAcercamiento != null && !jsonCoordsAcercamiento.isEmpty()) ? jsonCoordsAcercamiento : "[]";
            String traslado = (jsonCoordsTraslado != null && !jsonCoordsTraslado.isEmpty()) ? jsonCoordsTraslado : "[]";

            // Se envían los arreglos reales a JavaScript tal como indica la sección 4.5 del apunte
            String script = String.format(Locale.US, "trazarRutasCompletas(%f, %f, %f, %f, %f, %f, %s, %s);", 
                latM, lonM, latO, lonO, latD, lonD, acercamiento, traslado);
            webEngine.executeScript(script);
        }
    }
    public void trazarRutasCompletas(double latM, double lonM, double latO, double lonO, double latD, double lonD, String jsArrayCoordenadasTraslado) {
        if (cargado) {
            // Si no pasamos puntos viales, usa un array vacío []
            String coords = (jsArrayCoordenadasTraslado != null && !jsArrayCoordenadasTraslado.isEmpty()) 
                            ? jsArrayCoordenadasTraslado : "[]";

            // Enviamos los datos a la función JavaScript con la geometría real de las calles
            String script = String.format(Locale.US, "trazarRutasCompletas(%f, %f, %f, %f, %f, %f, [], %s);", 
                latM, lonM, latO, lonO, latD, lonD, coords);
            webEngine.executeScript(script);
        }
    }

    public class PuenteSeleccionMapa {
        public void seleccionarPunto(String tipo, double lat, double lon) {
        	System.out.println("🟢 ¡ÉXITO! El puente JavaScript conectó con Java. Tipo: " + tipo + " | Lat: " + lat + " | Lon: " + lon);
            Platform.runLater(() -> {
                if (listenerSeleccion != null) {
                    listenerSeleccion.accept(tipo, new double[]{lat, lon});
                }
            });
        }
    }
    public void limpiarSeleccionYRutas() {
        if (cargado) {
            try {
            	webEngine.executeScript("limpiarYReactivarOrigen();");
            } catch (Exception e) {
                System.err.println("Error al limpiar el mapa: " + e.getMessage());
            }
        } else {
            System.out.println("El mapa aún se está cargando.");
        }
    }
    public void mostrarRutaGraphHopper(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
        com.graphhopper.util.PointList puntos = RutaGraphHopperService.obtenerPuntosRuta(
                latOrigen, lonOrigen, latDestino, lonDestino
        );
        if (puntos == null || puntos.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < puntos.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(java.util.Locale.US)
              .append("[")
              .append(String.format(java.util.Locale.US, "%.6f", puntos.getLat(i)))
              .append(",")
              .append(String.format(java.util.Locale.US, "%.6f", puntos.getLon(i)))
              .append("]");
        }
        sb.append("]");

        String latLngsJson = sb.toString();

        javafx.application.Platform.runLater(() -> {
            if (cargado && webEngine != null) {
                try {
                    String js =
                        "(function() {" +
                        "  if (typeof map === 'undefined' || !map) return;" +
                        "  if (window.rutaGHLayer) { map.removeLayer(window.rutaGHLayer); }" +
                        "  var coords = " + latLngsJson + ";" +
                        "  window.rutaGHLayer = L.polyline(coords, {color: '#1976d2', weight: 5, opacity: 0.85}).addTo(map);" +
                        "  map.fitBounds(window.rutaGHLayer.getBounds(), {padding: [25, 25]});" +
                        "})();";
                    webEngine.executeScript(js);
                } catch (Exception e) {
                    System.err.println("Error al dibujar ruta de GraphHopper en Leaflet: " + e.getMessage());
                }
            } else {
                System.out.println("El mapa aún se está cargando.");
            }
        });
    }
}