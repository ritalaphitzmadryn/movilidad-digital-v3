package md.concurrencia;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import md.logica.GestorMovilidad;
import md.modelo.Servicio;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Viaje;

public class GeneradorDemandaTask implements Runnable {

    private final TableroServicios tablero;
    private final GestorMovilidad gestor;
    private final List<Usuario> clientes;
    private final List<Servicio> servicios;
    private final long duracionMs;
    private final int intervaloMinMs;
    private final int intervaloMaxMs;
    private final int cantidadConductores;
    private final Random random;

    private static final Ubicacion[] PUNTOS_URBANOS = {
        new Ubicacion(-42.7692, -65.0385, "Terminal de Ómnibus"),
        new Ubicacion(-42.7745, -65.0412, "Plaza San Martín"),
        new Ubicacion(-42.7810, -65.0290, "Costanera Sur"),
        new Ubicacion(-42.7615, -65.0520, "Parque Industrial"),
        new Ubicacion(-42.7780, -65.0480, "Hospital Zonal")
    };

    public GeneradorDemandaTask(TableroServicios tablero, GestorMovilidad gestor,
                                List<Usuario> clientes, List<Servicio> servicios,
                                int duracionSegundos, int intervaloMinMs, int intervaloMaxMs,
                                int cantidadConductores) {
        this.tablero = tablero;
        this.gestor = gestor;
        this.clientes = clientes;
        this.servicios = servicios;
        this.duracionMs = duracionSegundos * 1000L;
        this.intervaloMinMs = intervaloMinMs;
        this.intervaloMaxMs = intervaloMaxMs;
        this.cantidadConductores = cantidadConductores;
        this.random = new Random();
    }

    @Override
    public void run() {
        long tiempoFin = System.currentTimeMillis() + duracionMs;
        int emitidos = 0;

        try {
            while (System.currentTimeMillis() < tiempoFin && !Thread.currentThread().isInterrupted()) {
                int espera = intervaloMinMs + random.nextInt(Math.max(1, intervaloMaxMs - intervaloMinMs));
                Thread.sleep(espera);

                if (System.currentTimeMillis() >= tiempoFin) {
                    break;
                }

                Usuario cliente = clientes.get(random.nextInt(clientes.size()));
                Servicio servicio = servicios.get(random.nextInt(servicios.size()));

                Ubicacion origen = PUNTOS_URBANOS[random.nextInt(PUNTOS_URBANOS.length)];
                Ubicacion destino;
                do {
                    destino = PUNTOS_URBANOS[random.nextInt(PUNTOS_URBANOS.length)];
                } while (origen == destino);

                Viaje viaje;
                tablero.adquirirLock();
                try {
                    viaje = gestor.solicitarViaje(cliente, servicio, origen, destino, LocalDateTime.now());
                } finally {
                    tablero.liberarLock();
                }

                tablero.publicarViaje(viaje);
                emitidos++;
            }

            System.out.println("\n[GeneradorDemanda] Tiempo de emisión cumplido (" + emitidos + " solicitudes generadas). Esperando cierre de viajes activos...\n");

            while (tablero.getViajesSinAsignar() > 0) {
                Thread.sleep(300);
            }

            for (int i = 0; i < cantidadConductores; i++) {
                tablero.publicarViaje(TableroServicios.FIN_DE_JORNADA);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[GeneradorDemanda] Ejecución interrumpida.");
        }
    }
}