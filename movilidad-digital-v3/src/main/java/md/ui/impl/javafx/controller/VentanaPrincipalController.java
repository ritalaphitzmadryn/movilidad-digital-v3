package md.ui.impl.javafx.controller;

import java.util.List;
import javafx.scene.control.Label;
import md.logica.GestorMovilidad; 
import md.modelo.*;
import md.aplicacion.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.ResourceBundle;

// Importamos la interfaz y la implementación concreta del servicio de rutas
import md.rutas.api.IRutasService;
import md.rutas.impl.IRutasServiceImpl;

public class VentanaPrincipalController implements Initializable {
	
    @FXML
    private Label lblResultadoViaje; 
    
    @FXML
    private ComboBox<String> cbTipoUsuario; // Selector verde de perfil

    @FXML
    private ComboBox<String> cbOrigen;

    @FXML
    private ComboBox<String> cbDestino;

    @FXML
    private ComboBox<String> cbTipoServicio;

    @FXML
    private Button btnSolicitar;

    @FXML
    public WebView mapWebView;

    public WebEngine webEngine;
    
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

        // 1. Cargar las opciones del ComboBox de Tipo de Usuario
        if (cbTipoUsuario != null) {
            cbTipoUsuario.getItems().addAll("Cliente (Pasajero)", "Conductor");
            cbTipoUsuario.getSelectionModel().selectFirst();
        }

        // 2. Cargar las opciones del ComboBox de tipo de servicio
        if (cbTipoServicio != null) {
            cbTipoServicio.getItems().addAll("Económico", "Standard", "Confort", "VIP");
            cbTipoServicio.getSelectionModel().selectFirst();
        }
        
        // 3. Cargar las opciones completas en los ComboBox de Origen y Destino
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
            
            cbOrigen.getSelectionModel().selectFirst(); 
            if (ubicaciones.size() > 1) {
                cbDestino.getSelectionModel().select(1); 
            }
        }
        
        // 4. Configuración del mapa (WebView)
        if (mapWebView != null) {
            webEngine = mapWebView.getEngine();
            URL mapUrl = getClass().getResource("/ui/map/mapa.html");

            if (mapUrl != null) {
                mapWebView.widthProperty().addListener((obs, oldVal, newVal) -> actualizarTamanoMapa());
                mapWebView.heightProperty().addListener((obs, oldVal, newVal) -> actualizarTamanoMapa());

                webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                        actualizarTamanoMapa();
                    }
                });

                webEngine.load(mapUrl.toExternalForm());
            } else {
                System.err.println("No se pudo encontrar el archivo mapa.html");
            }
        }
            
        // 5. Vincular el botón
        if (btnSolicitar != null) {
            btnSolicitar.setOnAction(event -> handleSolicitarViaje());
        }
    }

    @FXML
    private void handleSolicitarViaje() {
        // 1. Obtener los valores seleccionados de los ComboBox
        String tipoUsuarioSeleccionado = cbTipoUsuario != null ? cbTipoUsuario.getValue() : "Cliente (Pasajero)";
        boolean esConductor = tipoUsuarioSeleccionado.contains("Conductor");

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
        
        try {
            // 3. Obtenemos el gestor de movilidad digital compartido
            GestorMovilidad gestor = Aplicacion.getGestor();
            
            if (gestor == null) {
                System.err.println("Error: El GestorMovilidad no ha sido inicializado.");
                return;
            }
        
            // 4. Obtener las coordenadas geográficas
            double[] coordsOrigen = obtenerCoordenadas(origen);
            double[] coordsDestino = obtenerCoordenadas(destino);
            
            double distanciaKm = 0.0;
            String detalleRuta = "";
            
            // 5. Utilizar IRutasService para calcular la ruta
            if (rutasService != null) {
                detalleRuta = rutasService.calcularRuta(coordsOrigen[0], coordsOrigen[1], coordsDestino[0], coordsDestino[1]);
                distanciaKm = rutasService.calcularDistanciaKm(coordsOrigen[0], coordsOrigen[1], coordsDestino[0], coordsDestino[1]);
            }
            
            // Estimamos el tiempo en minutos (asumiendo velocidad promedio urbana de 30 km/h)
            double tiempoMinutos = (distanciaKm / 30.0) * 60.0;

            // Instanciamos el Servicio con sus 7 parámetros para calcular el costo real
            Servicio servicioEjemplo = new Servicio(
                tipoServicio, 
                TipoServicio.PASAJEROS, 
                TipoVehiculo.AUTO, 
                CategoriaVehiculo.ESTANDAR, 
                100.0,  // Tarifa base
                50.0,   // Precio por km
                10.0    // Precio por minuto
            ); 

            // Calculamos el costo utilizando tu método de dominio
            double costoTotal = servicioEjemplo.calcularCosto(distanciaKm, tiempoMinutos);
            
            // 6. Buscar el usuario adecuado (Cliente o Conductor) en el gestor según la interfaz
            List<Usuario> usuarioList = gestor.obtenerUsuarios();
            Usuario usuarioEncontrado = null;
            
            for (Usuario u : usuarioList) {
                if (esConductor) {
                    if (u.getConductor() != null) {
                        usuarioEncontrado = u;
                        break;
                    }
                } else {
                    if (u.getCliente() != null) {
                        usuarioEncontrado = u;
                        break;
                    }
                }
            }
            
            if (usuarioEncontrado != null) {
                // Creamos objetos de ubicación
                Ubicacion origenObj = new Ubicacion(origen, coordsOrigen[0], coordsOrigen[1]);
                Ubicacion destinoObj = new Ubicacion(destino, coordsDestino[0], coordsDestino[1]);

                // Registramos el viaje en el gestor con el usuario correspondiente
                Viaje nuevoViaje = gestor.solicitarViaje(usuarioEncontrado, servicioEjemplo, origenObj, destinoObj, LocalDateTime.now());
                
                System.out.println("¡Viaje procesado con éxito! Perfil activo: " + 
                                   (esConductor ? "Conductor" : "Cliente") + 
                                   " | ID de Viaje: " + nuevoViaje.getId());
            } else {
                System.out.println("Aviso: No se encontró un perfil de " + (esConductor ? "conductor" : "cliente") + " registrado en el sistema.");
            }

            // 7. Mostrar resultados en la etiqueta visual (`lblResultadoViaje`) y en consola
            String resultadoTexto = String.format(
                Locale.US, 
                "Perfil: %s\nDistancia: %.2f km\nTiempo aprox: %.0f min\nCosto Total: $%.2f\n%s", 
                (esConductor ? "Conductor" : "Cliente"),
                distanciaKm, 
                tiempoMinutos, 
                costoTotal, 
                detalleRuta
            );
            
            if (lblResultadoViaje != null) {
                lblResultadoViaje.setText(resultadoTexto);
            }
            System.out.println(resultadoTexto);

         // 8. Trazar ruta en el mapa JavaScript (WebView)
            if (webEngine != null) {
                String script = String.format(Locale.US, "trazarRuta(%f, %f, %f, %f);", 
                    coordsOrigen[0], coordsOrigen[1], 
                    coordsDestino[0], coordsDestino[1]
                );
                webEngine.executeScript(script);
            }
        }
        catch (Exception e) {
            System.err.println("Error al procesar el viaje en el gestor: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Método auxiliar fuera de handleSolicitarViaje
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
                return new double[]{-42.7692, -65.0385}; 
        }
    }
}