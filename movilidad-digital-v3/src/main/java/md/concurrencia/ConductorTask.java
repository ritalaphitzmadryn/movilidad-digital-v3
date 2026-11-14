package md.concurrencia;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import md.logica.GestorMovilidad;
import md.modelo.CalificacionViaje;
import md.modelo.Conductor;
import md.modelo.EstadoConductor;
import md.modelo.Servicio;
import md.modelo.Usuario;
import md.modelo.Vehiculo;
import md.modelo.Viaje;

public class ConductorTask implements Runnable {

    private final Usuario usuarioConductor;
    private final TableroServicios tablero;
    private final GestorMovilidad gestor;
    private final Random random;

    private int viajesRealizados = 0;
    private double recaudacionTotal = 0.0;
    private int contadorRechazosLocales = 0;
    
    // Lista para almacenar el detalle de los viajes completados por este conductor
    private final List<Viaje> viajesCompletados = new ArrayList<>();

    public ConductorTask(Usuario usuarioConductor, TableroServicios tablero, GestorMovilidad gestor) {
        this.usuarioConductor = usuarioConductor;
        this.tablero = tablero;
        this.gestor = gestor;
        this.random = new Random();
    }

    @Override
    public void run() {
        Conductor perfilConductor = usuarioConductor.getConductor();
        if (perfilConductor == null) {
            return;
        }

        perfilConductor.setEstadoConductor(EstadoConductor.DISPONIBLE);
        Vehiculo vehiculo = perfilConductor.getVehiculoActivo();
        String infoVehiculo = (vehiculo != null)
                ? vehiculo.getModelo() + " [" + vehiculo.getTipoVehiculo() + " - " + vehiculo.getCategoriaVehiculo() + " | Patente: " + vehiculo.getPatente() + "]"
                : "Sin vehículo asignado";

        try {
            while (!Thread.currentThread().isInterrupted()) {
                Viaje viaje = tablero.tomarViaje();

                if (viaje == TableroServicios.FIN_DE_JORNADA) {
                    perfilConductor.setEstadoConductor(EstadoConductor.FUERA_DE_SERVICIO);
                    System.out.println("[Turno Finalizado] Conductor " + usuarioConductor.getNombre() + " se desconecta.");
                    break;
                }

                String idCorto = viaje.getId().toString().substring(0, 8);
                Servicio serv = viaje.getServicio();

                boolean compatible = vehiculo != null
                        && vehiculo.getTipoVehiculo() == serv.getTipoVehiculo()
                        && vehiculo.getCategoriaVehiculo() == serv.getCategoriaVehiculo()
                        && vehiculo.getTiposServicios().contains(serv.getTipoServicio());

                if (!compatible) {
                    int intento = tablero.registrarIntentoRechazo(viaje.getId());
                    String motivo = (vehiculo == null)
                            ? "sin vehículo asignado"
                            : "vehículo " + vehiculo.getTipoVehiculo() + " " + vehiculo.getCategoriaVehiculo() + " incompatible con " + serv.getNombre();

                    if (intento <= 2) {
                        System.out.println("   [Rechazado] " + usuarioConductor.getNombre()
                                + " (" + motivo + ") rechaza viaje #" + idCorto + ".");
                    } else if (intento == 3) {
                        System.out.println("   [Esperando Match] (...buscando conductor compatible para solicitud #" + idCorto + "...)");
                    }

                    tablero.reencolarViaje(viaje);
                    Thread.sleep(150);
                    continue;
                }

                tablero.confirmarAsignacion(viaje);
                String nombreCliente = viaje.getCliente() != null ? viaje.getCliente().getNombre() : "Cliente";

                tablero.adquirirLock();
                try {
                    gestor.aceptarViaje(viaje.getId(), usuarioConductor, LocalDateTime.now());
                } finally {
                    tablero.liberarLock();
                }

                System.out.println("--> [Aceptado] Conductor: " + usuarioConductor.getNombre()
                        + " | Vehículo: " + infoVehiculo
                        + " | Viaje #" + idCorto + " (Cliente: " + nombreCliente + ")");

                Thread.sleep(700 + random.nextInt(500));

                tablero.adquirirLock();
                try {
                    gestor.iniciarViaje(viaje.getId(), LocalDateTime.now());
                } finally {
                    tablero.liberarLock();
                }
                System.out.println("    [En curso] Viaje #" + idCorto + " iniciado hacia " + viaje.getDestino().getDireccion());

                Thread.sleep(900 + random.nextInt(700));

                tablero.adquirirLock();
                try {
                    gestor.finalizarViaje(viaje.getId(), LocalDateTime.now(), CalificacionViaje.EXCELENTE, CalificacionViaje.MUY_BUENO);
                } finally {
                    tablero.liberarLock();
                }

                viajesRealizados++;
                recaudacionTotal += viaje.getCostoCalculado();
                
                // Guardamos el viaje finalizado en la lista del conductor
                viajesCompletados.add(viaje);

                String costoFinal = String.format("%.2f", viaje.getCostoCalculado());
                System.out.println("<-- [Finalizado] Viaje #" + idCorto
                        + " completado por " + usuarioConductor.getNombre()
                        + " | Cliente: " + nombreCliente
                        + " | Importe: $" + costoFinal);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public int getViajesRealizados() {
        return viajesRealizados;
    }

    public double getRecaudacionTotal() {
        return recaudacionTotal;
    }

    public Usuario getUsuarioConductor() {
        return usuarioConductor;
    }

    public List<Viaje> getViajesCompletados() {
        return viajesCompletados;
    }
}