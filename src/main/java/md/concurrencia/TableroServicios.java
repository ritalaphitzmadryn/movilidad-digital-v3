package md.concurrencia;

import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import md.modelo.Viaje;

public class TableroServicios {

    private final ArrayBlockingQueue<Viaje> colaViajes;
    private final AtomicInteger viajesSinAsignar = new AtomicInteger(0);
    private final ConcurrentHashMap<UUID, AtomicInteger> conteoRechazosPorViaje = new ConcurrentHashMap<>();

    public static final Viaje FIN_DE_JORNADA = new Viaje(null, null, null, null, null);

    public TableroServicios(int capacidad) {
        this.colaViajes = new ArrayBlockingQueue<>(capacidad);
    }

    public void publicarViaje(Viaje viaje) throws InterruptedException {
        if (viaje != FIN_DE_JORNADA) {
            viajesSinAsignar.incrementAndGet();
            conteoRechazosPorViaje.put(viaje.getId(), new AtomicInteger(0));

            String idCorto = viaje.getId().toString().substring(0, 8);
            String cliente = viaje.getCliente() != null ? viaje.getCliente().getNombre() : "Desconocido";
            String servicio = viaje.getServicio().getNombre() + " (" + viaje.getServicio().getTipoServicio() + ")";
            String costo = String.format("%.2f", viaje.getCostoCalculado());

            System.out.println("[Tablero] Nueva solicitud #" + idCorto
                    + " | Cliente: " + cliente
                    + " | Servicio: " + servicio
                    + " | Origen: " + viaje.getOrigen().getDireccion()
                    + " -> Destino: " + viaje.getDestino().getDireccion()
                    + " | Tarifa est.: $" + costo);
        }
        colaViajes.put(viaje);
    }

    public Viaje tomarViaje() throws InterruptedException {
        return colaViajes.take();
    }

    public void confirmarAsignacion(Viaje viaje) {
        viajesSinAsignar.decrementAndGet();
        conteoRechazosPorViaje.remove(viaje.getId());
    }

    public void reencolarViaje(Viaje viaje) throws InterruptedException {
        colaViajes.put(viaje);
    }

    public int registrarIntentoRechazo(UUID idViaje) {
        AtomicInteger contador = conteoRechazosPorViaje.get(idViaje);
        return (contador != null) ? contador.incrementAndGet() : 0;
    }

    public int getViajesSinAsignar() {
        return viajesSinAsignar.get();
    }
}