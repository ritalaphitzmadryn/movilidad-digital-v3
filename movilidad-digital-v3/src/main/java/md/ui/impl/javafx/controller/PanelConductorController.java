package md.ui.impl.javafx.controller;

import com.graphhopper.ResponsePath;
import com.graphhopper.util.PointList;
import javafx.concurrent.Worker;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ToggleButton;
import javafx.scene.web.WebView;
import md.modelo.Conductor;
import md.modelo.EstadoConductor;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Viaje;
import md.ui.impl.javafx.map.RutaGraphHopperService;
import md.ui.impl.javafx.map.VisualizadorRutasLeaflet;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PanelConductorController implements Initializable {

    private static final List<String> VIAJES_DISPONIBLES_SESION = new ArrayList<>();
    private static boolean viajesInicializadosEnSesion = false;
    private md.modelo.TipoVehiculo vehiculoActual = md.modelo.TipoVehiculo.AUTO;
    
    @FXML
    private Label lblEstadoConductor;

    @FXML
    private ToggleButton tglDisponibilidad;

    @FXML
    private ListView<String> listViajesDisponibles;

    @FXML
    private Button btnAceptarViaje;

    @FXML
    private WebView mapaConductorWebView;

    private VisualizadorRutasLeaflet visualizadorMapa;
    private String viajePendienteDeDibujar;

    private double latConductor = -42.7692;
    private double lonConductor = -65.0385;

    private Usuario usuarioActual;
    private Conductor conductorActual;

    private String origenActual = "Terminal / Origen";
    private String destinoActual = "Centro / Destino";
    private double distanciaActualKm = 2.5;
    private double costoActual = 0.0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        System.out.println("Inicializando PanelConductorController...");

        if (tglDisponibilidad != null) {
            tglDisponibilidad.setSelected(false);
        }
        actualizarTextoEstado(EstadoConductor.FUERA_DE_SERVICIO);
        inicializarMapaConductor();
        configurarSeleccionMapaEnLista();
        cargarViajesPendientes();
    }

    private void inicializarMapaConductor() {
        if (mapaConductorWebView == null) {
            return;
        }
        visualizadorMapa = new VisualizadorRutasLeaflet(mapaConductorWebView);
        mapaConductorWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED && viajePendienteDeDibujar != null) {
                String pendiente = viajePendienteDeDibujar;
                viajePendienteDeDibujar = null;
                trazarViajeSeleccionadoEnMapa(pendiente);
            }
        });
    }

    private void configurarSeleccionMapaEnLista() {
        if (listViajesDisponibles == null) {
            return;
        }
        listViajesDisponibles.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.isBlank()) {
                trazarViajeSeleccionadoEnMapa(newVal);
            }
        });
    }

    private void trazarViajeSeleccionadoEnMapa(String textoViaje) {
        if (visualizadorMapa == null || textoViaje == null || textoViaje.isBlank()) {
            return;
        }
        if (mapaConductorWebView != null
                && mapaConductorWebView.getEngine().getLoadWorker().getState() != Worker.State.SUCCEEDED) {
            viajePendienteDeDibujar = textoViaje;
            return;
        }

        String[] lugares = parsearOrigenDestino(textoViaje);
        String origenTexto = lugares[0];
        String destinoTexto = lugares[1];

        double[] cOrigen = obtenerCoordenadasConocidas(origenTexto);
        double[] cDestino = obtenerCoordenadasConocidas(destinoTexto);
        if (cOrigen == null) {
            cOrigen = new double[]{-42.7758, -65.0525};
        }
        if (cDestino == null) {
            cDestino = new double[]{-42.7685, -65.0320};
        }

        double latO = cOrigen[0];
        double lonO = cOrigen[1];
        double latD = cDestino[0];
        double lonD = cDestino[1];

        if (Double.compare(latO, latD) == 0 && Double.compare(lonO, lonD) == 0) {
            latD = latO + 0.008;
            lonD = lonO + 0.008;
        }

        ResponsePath rutaAcercamiento = null;
        ResponsePath rutaTraslado = null;

        try {
            rutaAcercamiento = RutaGraphHopperService.calcularRuta(latConductor, lonConductor, latO, lonO);
        } catch (Exception e) {
            System.err.println("Aviso GraphHopper acercamiento (conductor -> origen): " + e.getMessage());
        }

        try {
            rutaTraslado = RutaGraphHopperService.calcularRuta(latO, lonO, latD, lonD);
        } catch (Exception e) {
            System.err.println("Aviso GraphHopper traslado (origen -> destino): " + e.getMessage());
        }

        String jsonAcercamiento = convertirPointListAJson(
                rutaAcercamiento != null ? rutaAcercamiento.getPoints() : null,
                latConductor, lonConductor, latO, lonO
        );
        String jsonTraslado = convertirPointListAJson(
                rutaTraslado != null ? rutaTraslado.getPoints() : null,
                latO, lonO, latD, lonD
        );

        visualizadorMapa.trazarRutasCompletas(
                latConductor, lonConductor,
                latO, lonO,
                latD, lonD,
                jsonAcercamiento,
                jsonTraslado
        );
    }

    private String[] parsearOrigenDestino(String textoViaje) {
        String origen = "Centro";
        String destino = "Costanera";

        if (textoViaje == null || textoViaje.isBlank()) {
            return new String[]{origen, destino};
        }

        String sinPrefijo = textoViaje.replaceFirst("^Viaje\\s*#?\\d*\\s*:?\\s*", "");
        String sinParentesis = sinPrefijo.replaceAll("\\(.*?\\)", "").trim();
        String[] partes = sinParentesis.split("\\s*(➔|->|→|—|–|-)\\s*");
        if (partes.length >= 2) {
            origen = partes[0].replaceFirst("^:+\\s*", "").trim();
            destino = partes[1].replaceFirst("^:+\\s*", "").trim();
        } else if (!sinParentesis.isEmpty()) {
            origen = sinParentesis.replaceFirst("^:+\\s*", "").trim();
        }
        return new String[]{origen, destino};
    }

    private String convertirPointListAJson(PointList pts, double lat1, double lon1, double lat2, double lon2) {
        if (pts != null && !pts.isEmpty()) {
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < pts.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(String.format(Locale.US, "[%.6f, %.6f]", pts.getLat(i), pts.getLon(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        return String.format(Locale.US, "[[%f, %f], [%f, %f]]", lat1, lon1, lat2, lon2);
    }

    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
        if (this.usuarioActual != null && this.usuarioActual.getConductor() != null) {
            this.conductorActual = this.usuarioActual.getConductor();

            EstadoConductor estadoActual = conductorActual.getEstadoConductor();
            if (estadoActual == null) {
                estadoActual = EstadoConductor.FUERA_DE_SERVICIO;
                conductorActual.setEstadoConductor(estadoActual);
            }

            actualizarTextoEstado(estadoActual);
            if (tglDisponibilidad != null) {
                tglDisponibilidad.setSelected(estadoActual == EstadoConductor.DISPONIBLE);
            }
        }
        cargarViajesPendientes();
    }

    @FXML
    private void handleCambiarDisponibilidad() {
        boolean activo = tglDisponibilidad != null && tglDisponibilidad.isSelected();
        EstadoConductor nuevoEstado = activo ? EstadoConductor.DISPONIBLE : EstadoConductor.FUERA_DE_SERVICIO;

        if (conductorActual != null) {
            conductorActual.setEstadoConductor(nuevoEstado);
        }

        actualizarTextoEstado(nuevoEstado);

        if (activo) {
            System.out.println("Conductor en línea. Estado: DISPONIBLE");
        } else {
            System.out.println("Conductor desconectado. Estado: FUERA_DE_SERVICIO");
        }
    }

    private void actualizarTextoEstado(EstadoConductor estado) {
        if (lblEstadoConductor == null) return;

        switch (estado) {
            case DISPONIBLE:
                if (tglDisponibilidad != null) tglDisponibilidad.setText("Disponible");
                lblEstadoConductor.setText("Estado: DISPONIBLE");
                lblEstadoConductor.setStyle("-fx-text-fill: #2e7d32; -fx-font-weight: bold;");
                break;
            case FUERA_DE_SERVICIO:
                if (tglDisponibilidad != null) tglDisponibilidad.setText("No Disponible");
                lblEstadoConductor.setText("Estado: FUERA_DE_SERVICIO");
                lblEstadoConductor.setStyle("-fx-text-fill: #d32f2f; -fx-font-weight: bold;");
                break;
            case VIAJE_A_ORIGEN:
                if (tglDisponibilidad != null) tglDisponibilidad.setText("En Servicio (Origen)");
                lblEstadoConductor.setText("Estado: VIAJE_A_ORIGEN");
                lblEstadoConductor.setStyle("-fx-text-fill: #ef6c00; -fx-font-weight: bold;");
                break;
            case VIAJE_A_DESTINO:
                if (tglDisponibilidad != null) tglDisponibilidad.setText("En Servicio (Destino)");
                lblEstadoConductor.setText("Estado: VIAJE_A_DESTINO");
                lblEstadoConductor.setStyle("-fx-text-fill: #1565c0; -fx-font-weight: bold;");
                break;
        }
    }

    private boolean esMismoUsuarioActual(Usuario candidato) {
        if (usuarioActual == null || candidato == null) {
            return false;
        }
        String emailActual = usuarioActual.getEmail();
        String emailCand = candidato.getEmail();
        if (emailActual != null && !emailActual.isBlank()
                && emailCand != null && !emailCand.isBlank()
                && emailActual.trim().equalsIgnoreCase(emailCand.trim())) {
            return true;
        }
        String nombreActual = usuarioActual.getNombre();
        String nombreCand = candidato.getNombre();
        return nombreActual != null && !nombreActual.isBlank()
                && nombreCand != null && !nombreCand.isBlank()
                && nombreActual.trim().equalsIgnoreCase(nombreCand.trim());
    }

    private boolean esNombreDelConductorActual(String nombre) {
        if (usuarioActual == null || usuarioActual.getNombre() == null || nombre == null) {
            return false;
        }
        return usuarioActual.getNombre().trim().equalsIgnoreCase(nombre.trim());
    }

    private void cargarViajesPendientes() {
        if (listViajesDisponibles == null) return;
        listViajesDisponibles.getItems().clear();

        if (!viajesInicializadosEnSesion) {
            VIAJES_DISPONIBLES_SESION.clear();

            List<String> nombresClientes = new ArrayList<>();
            try {
                md.logica.GestorMovilidad gestor = md.aplicacion.Aplicacion.getGestor();
                if (gestor != null && gestor.obtenerUsuarios() != null) {
                    for (Usuario u : gestor.obtenerUsuarios()) {
                        if (!esMismoUsuarioActual(u)
                                && u.getCliente() != null
                                && u.getNombre() != null
                                && !u.getNombre().isBlank()
                                && !esNombreDelConductorActual(u.getNombre())) {
                            nombresClientes.add(u.getNombre().trim());
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            if (nombresClientes.isEmpty()) {
                for (String fallback : List.of("Rita", "Lucía", "Martín", "Sofía", "Carlos")) {
                    if (!esNombreDelConductorActual(fallback)) {
                        nombresClientes.add(fallback);
                    }
                }
            }
            if (nombresClientes.isEmpty()) {
                nombresClientes.add("Pasajero");
            }

            String[] puntos = {"UNPSJB", "Costanera", "Terminal", "Centro", "Ecocentro", "Puerto Madryn"};
            String[] vehiculos = {"Auto", "Moto"};
            Random rnd = new Random();

            int cantidadViajes = 3;
            for (int i = 1; i <= cantidadViajes; i++) {
                String cliente = nombresClientes.get(rnd.nextInt(nombresClientes.size()));
                String origen = puntos[rnd.nextInt(puntos.length)];
                String destino = puntos[rnd.nextInt(puntos.length)];
                while (destino.equals(origen)) {
                    destino = puntos[rnd.nextInt(puntos.length)];
                }

                // Garantiza al menos un viaje en Auto (#1) y uno en Moto (#2); el #3 es aleatorio
                String vehiculo;
                if (i == 1) {
                    vehiculo = "Auto";
                } else if (i == 2) {
                    vehiculo = "Moto";
                } else {
                    vehiculo = vehiculos[rnd.nextInt(vehiculos.length)];
                }

                double distKm = 1.5 + (rnd.nextDouble() * 6.5);
                String viajeTexto = String.format(
                        Locale.US,
                        "Viaje #%d [%s] (%s): %s ➔ %s (Aprox. %.1f km)",
                        i, vehiculo, cliente, origen, destino, distKm
                );
                VIAJES_DISPONIBLES_SESION.add(viajeTexto);
            }

            viajesInicializadosEnSesion = true;
        }

        listViajesDisponibles.getItems().addAll(VIAJES_DISPONIBLES_SESION);
    }

    private double[] obtenerCoordenadasConocidas(String lugar) {
        if (lugar == null) return null;
        String l = lugar.toLowerCase(Locale.ROOT);
        if (l.contains("unpsjb")) {
            return new double[]{-42.7856, -65.0065}; // Blvd. Brown (UNPSJB, tierra firme)
        }
        if (l.contains("costanera")) {
            return new double[]{-42.7685, -65.0355}; // Av. Roca y 28 de Julio (sobre calle)
        }
        if (l.contains("terminal")) {
            return new double[]{-42.7758, -65.0525}; // Terminal de Ómnibus
        }
        if (l.contains("centro")) {
            return new double[]{-42.7692, -65.0385}; // Plaza San Martín / Centro
        }
        if (l.contains("ecocentro")) {
            return new double[]{-42.7935, -65.0085}; // Ecocentro Pampa Azul / Punta Cuevas (tierra firme)
        }
        if (l.contains("puerto madryn")) {
            return new double[]{-42.7670, -65.0410}; // Zona urbana céntrica
        }
        return null;
    }

    private void extraerDatosViajeSeleccionado(String textoViaje) {
        origenActual = "Origen del viaje";
        destinoActual = "Destino del viaje";
        distanciaActualKm = 0.0;
        costoActual = 2000.0;
        vehiculoActual = md.modelo.TipoVehiculo.AUTO;

        if (textoViaje == null || textoViaje.isBlank()) {
            return;
        }

        if (textoViaje.toLowerCase(Locale.ROOT).contains("moto")) {
            vehiculoActual = md.modelo.TipoVehiculo.MOTO;
        }

        Matcher matcherDist = Pattern
                .compile("(\\d+(?:[.,]\\d+)?)\\s*km", Pattern.CASE_INSENSITIVE)
                .matcher(textoViaje);
        if (matcherDist.find()) {
            String num = matcherDist.group(1).replace(',', '.');
            try {
                distanciaActualKm = Double.parseDouble(num);
            } catch (NumberFormatException ignored) {
                distanciaActualKm = 0.0;
            }
        }

        String[] lugares = parsearOrigenDestino(textoViaje);
        origenActual = lugares[0];
        destinoActual = lugares[1];

        double[] cOrigen = obtenerCoordenadasConocidas(origenActual);
        double[] cDestino = obtenerCoordenadasConocidas(destinoActual);
        if (cOrigen != null && cDestino != null) {
            try {
                ResponsePath ruta = RutaGraphHopperService.calcularRuta(
                        cOrigen[0], cOrigen[1], cDestino[0], cDestino[1]
                );
                if (ruta != null && ruta.getDistance() > 0) {
                    distanciaActualKm = ruta.getDistance() / 1000.0;
                }
            } catch (Exception ignored) {
            }
        }

        double base = (vehiculoActual == md.modelo.TipoVehiculo.MOTO) ? 1500.0 : 2000.0;
        double porKm = (vehiculoActual == md.modelo.TipoVehiculo.MOTO) ? 150.0 : 200.0;
        costoActual = base + (distanciaActualKm * porKm);

        System.out.println("📏 [Resumen] Vehículo: " + vehiculoActual
                + " | Origen: " + origenActual + " | Destino: " + destinoActual
                + " | Distancia: " + distanciaActualKm + " km | Costo: $" + costoActual);
    }

    @FXML
    private void handleAceptarViaje() {
        if (conductorActual == null) return;

        EstadoConductor estadoActual = conductorActual.getEstadoConductor();

        if (estadoActual == EstadoConductor.DISPONIBLE || estadoActual == EstadoConductor.FUERA_DE_SERVICIO) {
            String viajeSeleccionado = listViajesDisponibles != null
                    ? listViajesDisponibles.getSelectionModel().getSelectedItem()
                    : null;

            if (viajeSeleccionado == null && listViajesDisponibles != null && !listViajesDisponibles.getItems().isEmpty()) {
                viajeSeleccionado = listViajesDisponibles.getItems().get(0);
                System.out.println("AVISO: No habías seleccionado un viaje, se tomó el primero automáticamente.");
            }

            if (viajeSeleccionado != null) {
                md.modelo.TipoVehiculo tipoViaje = viajeSeleccionado.toLowerCase(Locale.ROOT).contains("moto")
                        ? md.modelo.TipoVehiculo.MOTO
                        : md.modelo.TipoVehiculo.AUTO;

                md.modelo.TipoVehiculo tipoVehiculoConductor = obtenerVehiculoActivoConductor();

                if (tipoVehiculoConductor != null && tipoVehiculoConductor != tipoViaje) {
                    Alert alerta = new Alert(Alert.AlertType.WARNING);
                    alerta.setTitle("Vehículo incompatible");
                    alerta.setHeaderText("No podés aceptar este viaje");
                    alerta.setContentText("Tu vehículo activo es " + tipoVehiculoConductor
                            + " y este viaje requiere " + tipoViaje + ".");
                    alerta.showAndWait();
                    return;
                }

                extraerDatosViajeSeleccionado(viajeSeleccionado);
                System.out.println("¡Viaje aceptado con éxito!: " + viajeSeleccionado);

                conductorActual.setEstadoConductor(EstadoConductor.VIAJE_A_ORIGEN);
                actualizarTextoEstado(EstadoConductor.VIAJE_A_ORIGEN);

                VIAJES_DISPONIBLES_SESION.remove(viajeSeleccionado);
                if (listViajesDisponibles != null) {
                    listViajesDisponibles.getItems().remove(viajeSeleccionado);
                }

                if (btnAceptarViaje != null) {
                    btnAceptarViaje.setText("Confirmar Llegada a Destino / Finalizar");
                }
            } else {
                System.out.println("No hay viajes disponibles en la lista para aceptar.");
            }

        } else if (estadoActual == EstadoConductor.VIAJE_A_ORIGEN) {
            conductorActual.setEstadoConductor(EstadoConductor.VIAJE_A_DESTINO);
            actualizarTextoEstado(EstadoConductor.VIAJE_A_DESTINO);
            System.out.println("Trayecto en curso hacia el destino...");
            if (btnAceptarViaje != null) {
                btnAceptarViaje.setText("Finalizar Viaje");
            }

        } else if (estadoActual == EstadoConductor.VIAJE_A_DESTINO) {
            finalizarYMostrarResumen();
        }
    }

    @FXML
    private void handleFinalizarViaje() {
        finalizarYMostrarResumen();
    }

    private void finalizarYMostrarResumen() {
        double[] cDActual = obtenerCoordenadasConocidas(destinoActual);
        if (cDActual != null) {
            latConductor = cDActual[0];
            lonConductor = cDActual[1];
        }

        if (conductorActual != null) {
            conductorActual.setEstadoConductor(EstadoConductor.DISPONIBLE);
            actualizarTextoEstado(EstadoConductor.DISPONIBLE);
        }
        System.out.println("¡Viaje finalizado con éxito! Conductor nuevamente disponible.");

        try {
            if (usuarioActual != null) {
                String url = "jdbc:postgresql://pgs.fi.mdn.unp.edu.ar:30000/bd1";
                String user = "estudiante";
                String password = "bd1estudiante";

                md.datos.DAO.bd.ConexionBD conexion = new md.datos.DAO.bd.ConexionBD(url, user, password);
                md.datos.DAO.bd.ServicioDaoBd dao = new md.datos.DAO.bd.ServicioDaoBd(conexion);

                double[] cO = obtenerCoordenadasConocidas(origenActual);
                double[] cD = obtenerCoordenadasConocidas(destinoActual);

                Ubicacion origen = new Ubicacion(
                        origenActual,
                        cO != null ? cO[0] : -42.7692,
                        cO != null ? cO[1] : -65.0385
                );
                Ubicacion destino = new Ubicacion(
                        destinoActual,
                        cD != null ? cD[0] : -42.7685,
                        cD != null ? cD[1] : -65.0320
                );

                md.modelo.TipoVehiculo tipoVehiculoElegido = (vehiculoActual != null)
                        ? vehiculoActual
                        : md.modelo.TipoVehiculo.AUTO;

                md.modelo.TipoServicio tipoServicioElegido = (tipoVehiculoElegido == md.modelo.TipoVehiculo.MOTO)
                        ? md.modelo.TipoServicio.ENVIOS
                        : md.modelo.TipoServicio.PASAJEROS;

                md.modelo.Servicio servicioViaje = null;
                try {
                    md.logica.GestorMovilidad gestor = md.aplicacion.Aplicacion.getGestor();
                    if (gestor != null) {
                        servicioViaje = gestor.buscarServicioAdecuado(
                                tipoServicioElegido,
                                tipoVehiculoElegido,
                                md.modelo.CategoriaVehiculo.ESTANDAR
                        );
                    }
                } catch (Exception ignored) {
                }

                if (servicioViaje == null) {
                    String nombreServicioFallback = (tipoVehiculoElegido == md.modelo.TipoVehiculo.MOTO)
                            ? "Moto Standard"
                            : "Standard";
                    double baseFallback = (tipoVehiculoElegido == md.modelo.TipoVehiculo.MOTO) ? 1500.0 : 2000.0;
                    double kmFallback = (tipoVehiculoElegido == md.modelo.TipoVehiculo.MOTO) ? 150.0 : 200.0;

                    servicioViaje = new md.modelo.Servicio(
                            nombreServicioFallback,
                            tipoServicioElegido,
                            tipoVehiculoElegido,
                            md.modelo.CategoriaVehiculo.ESTANDAR,
                            baseFallback,
                            kmFallback,
                            100.0
                    );
                }

                Viaje viajeTerminado = new Viaje(usuarioActual, servicioViaje, origen, destino);
                viajeTerminado.finalizar(
                        java.time.LocalDateTime.now(),
                        md.modelo.CalificacionViaje.NO_CALIFICADO,
                        md.modelo.CalificacionViaje.NO_CALIFICADO
                );
                String emailConductor = usuarioActual.getEmail();

                dao.guardarViajeRealizado(viajeTerminado, emailConductor);
                System.out.println("💾 [DAO] Viaje (" + tipoVehiculoElegido + ") guardado para conductor ("
                        + emailConductor + "): " + origenActual + " ➔ " + destinoActual);
            } else {
                System.out.println("AVISO: usuarioActual es NULL, no se pudo guardar el registro.");
            }
        } catch (Exception e) {
            System.err.println("❌ Error al guardar viaje realizado: " + e.getMessage());
            e.printStackTrace();
        }

        if (btnAceptarViaje != null) {
            btnAceptarViaje.setText("Aceptar Viaje Seleccionado");
        }

        Alert alertaFinalizado = new Alert(Alert.AlertType.INFORMATION);
        alertaFinalizado.setTitle("Estado del Viaje");
        alertaFinalizado.setHeaderText("Viaje Finalizado!");
        alertaFinalizado.setContentText("El viaje se completó correctamente.");
        alertaFinalizado.showAndWait();

        String resumen = String.format(
                Locale.US,
                "🏍️/🚗 Vehículo: %s%n" +
                "📍 Origen: %s%n" +
                "🏁 Destino: %s%n" +
                "📏 Distancia: %.2f km%n" +
                "💰 Costo: $%.2f",
                vehiculoActual != null ? vehiculoActual.name() : "AUTO",
                origenActual,
                destinoActual,
                distanciaActualKm,
                costoActual
        );

        Alert alertaResumen = new Alert(Alert.AlertType.INFORMATION);
        alertaResumen.setTitle("Resumen del Viaje");
        alertaResumen.setHeaderText("Detalle del viaje finalizado");
        alertaResumen.setContentText(resumen);
        alertaResumen.showAndWait();
    }
    @FXML
    private void handleVerHistorial(javafx.event.ActionEvent event) {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/vistas/PanelHistorialConductor.fxml"));
            javafx.scene.Parent root = loader.load();

            PanelHistorialConductorController historialController = loader.getController();
            if (historialController != null && usuarioActual != null) {
                historialController.setUsuarioActual(usuarioActual);
                historialController.initData(usuarioActual.getEmail());
            }

            stage.setTitle("Movilidad Digital - Historial de Viajes");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverLogin(javafx.event.ActionEvent event) {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/vistas/Login.fxml"));
            javafx.scene.Parent root = loader.load();
            stage.setTitle("Movilidad Digital - Iniciar Sesión");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleVolverInicio(javafx.event.ActionEvent event) {
        try {
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/vistas/VentanaPrincipal.fxml"));
            javafx.scene.Parent root = loader.load();

            VentanaPrincipalController principalController = loader.getController();
            if (principalController != null && usuarioActual != null) {
                principalController.setUsuarioActual(usuarioActual);
            }

            stage.setTitle("Movilidad Digital - Inicio");
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private md.modelo.TipoVehiculo obtenerVehiculoActivoConductor() {
        if (conductorActual != null && conductorActual.getVehiculoActivo() != null) {
            md.modelo.TipoVehiculo tipo = conductorActual.getVehiculoActivo().getTipoVehiculo();
            if (tipo != null) {
                return tipo;
            }
        }
        return null;
    }
}