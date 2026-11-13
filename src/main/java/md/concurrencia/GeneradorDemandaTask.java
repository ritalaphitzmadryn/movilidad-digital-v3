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
    private final int cantidadDemandas;
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
                                int cantidadDemandas, int cantidadConductores) {
        this.tablero = tablero;
        this.gestor = gestor;
        this.clientes = clientes;
        this.servicios = servicios;
        this.cantidadDemandas = cantidadDemandas;
        this.cantidadConductores = cantidadConductores;
        this.random = new Random();
    }

    @Override
    public void run() {
        try {
            for (int i = 1; i <= cantidadDemandas; i++) {
                Thread.sleep(700 + random.nextInt(600));

                Usuario cliente = clientes.get(random.nextInt(clientes.size()));
                Servicio servicio = servicios.get(random.nextInt(servicios.size()));

                Ubicacion origen = PUNTOS_URBANOS[random.nextInt(PUNTOS_URBANOS.length)];
                Ubicacion destino;
                do {
                    destino = PUNTOS_URBANOS[random.nextInt(PUNTOS_URBANOS.length)];
                } while (origen == destino);

                Viaje viaje = gestor.solicitarViaje(cliente, servicio, origen, destino, LocalDateTime.now());
                tablero.publicarViaje(viaje);
            }

            System.out.println("\n[GeneradorDemanda] Se emitieron todas las solicitudes (" + cantidadDemandas + "). Esperando asignación total...\n");

            // Espera hasta que todos los viajes hayan sido aceptados por un conductor compatible
            while (tablero.getViajesSinAsignar() > 0) {
                Thread.sleep(300);
            }

            // Envía la señal de cierre a todos los conductores una vez que no quedan viajes pendientes
            for (int i = 0; i < cantidadConductores; i++) {
                tablero.publicarViaje(TableroServicios.FIN_DE_JORNADA);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("[GeneradorDemanda] Ejecución interrumpida.");
        }
    }
}