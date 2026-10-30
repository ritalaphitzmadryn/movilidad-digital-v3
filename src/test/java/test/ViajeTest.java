package test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import md.modelo.CalificacionViaje;
import md.modelo.CategoriaVehiculo;
import md.modelo.EstadoConductor;
import md.modelo.EstadoViaje;
import md.modelo.Servicio;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Viaje;

public class ViajeTest {

    private Usuario clienteUsuario;
    private Usuario conductorUsuario;
    private Servicio servicio;
    private Ubicacion origen;
    private Ubicacion destino;

    @BeforeEach
    public void setUp() {
        clienteUsuario = new Usuario("U001", "Juan Perez", "2804123456", "juan@gmail.com");
        conductorUsuario = new Usuario("U002", "Pedro Gomez", "2804987654", "pedro@gmail.com");

        servicio = new Servicio(
            "Servicio Estándar",
            TipoServicio.PASAJEROS,
            TipoVehiculo.AUTO,
            CategoriaVehiculo.ESTANDAR,
            100.0, // Tarifa base
            50.0,  // Precio/km
            10.0   // Precio/min
        );

        origen = new Ubicacion(-42.769, -65.038, "Av. Roca 100");
        destino = new Ubicacion(-42.775, -65.045, "San Martín 500");
    }

    @Test
    public void crearViajeInicializaEstadoYSolicitanteCorrectamente() {
        Viaje viaje = new Viaje(clienteUsuario, servicio, origen, destino, new ArrayList<>());

        assertNotNull(viaje.getId(), "El ID del viaje debe generarse.");
        assertEquals(clienteUsuario, viaje.getCliente(), "El cliente del viaje debe ser el asignado.");
        assertEquals(EstadoViaje.SOLICITADO, viaje.getEstadoActual(), "El estado inicial del viaje debe ser SOLICITADO.");
    }

    @Test
    public void calcularCostoEstimadoAlCrearViaje() {
        Viaje viaje = new Viaje(clienteUsuario, servicio, origen, destino, new ArrayList<>());

        double kmEsperados = origen.calcularDistancia(destino);
        double minEsperados = kmEsperados * 2.0;
        double costoEsperado = servicio.calcularCosto(kmEsperados, minEsperados);

        assertEquals(costoEsperado, viaje.getCostoCalculado(), 0.001, "El costo calculado debe coincidir con el cálculo de la ubicación y el servicio.");
    }

    @Test
    public void aceptarViajeCambiaEstadoYConductor() {
        Viaje viaje = new Viaje(clienteUsuario, servicio, origen, destino, new ArrayList<>());
        LocalDateTime ahora = LocalDateTime.now();

        viaje.aceptar(ahora, conductorUsuario);

        assertEquals(EstadoViaje.ACEPTADO, viaje.getEstadoActual(), "El estado del viaje debe ser ACEPTADO.");
        assertEquals(conductorUsuario, viaje.getConductor(), "El conductor del viaje debe ser asignado al aceptar.");

        if (conductorUsuario.getConductor() != null) {
            assertEquals(EstadoConductor.VIAJE_A_ORIGEN, conductorUsuario.getConductor().getEstadoConductor(),
                "El conductor debe cambiar su estado a VIAJE_A_ORIGEN.");
        }
    }

    @Test
    public void iniciarViajeCambiaEstadoYFecha() {
        Viaje viaje = new Viaje(clienteUsuario, servicio, origen, destino, new ArrayList<>());
        LocalDateTime ahora = LocalDateTime.now();

        viaje.iniciar(ahora);

        assertEquals(EstadoViaje.INICIADO, viaje.getEstadoActual(), "El estado debe cambiar a INICIADO.");
        assertEquals(ahora, viaje.getFechaHoraInicio(), "La fecha de inicio debe quedar registrada.");
    }

    @Test
    public void finalizarViajeRegistraCalificacionesYLiberaConductor() {
        Viaje viaje = new Viaje(clienteUsuario, servicio, origen, destino, new ArrayList<>());
        LocalDateTime ahora = LocalDateTime.now();

        viaje.aceptar(ahora, conductorUsuario);
        viaje.finalizar(ahora, CalificacionViaje.EXCELENTE, CalificacionViaje.BUENO);

        assertEquals(EstadoViaje.FINALIZADO, viaje.getEstadoActual(), "El estado debe ser FINALIZADO.");
        assertEquals(CalificacionViaje.EXCELENTE, viaje.getCalificacionConductor(), "Debe guardar la calificación del conductor.");
        assertEquals(CalificacionViaje.BUENO, viaje.getCalificacionCliente(), "Debe guardar la calificación del cliente.");

        if (conductorUsuario.getConductor() != null) {
            assertEquals(EstadoConductor.DISPONIBLE, conductorUsuario.getConductor().getEstadoConductor(),
                "El conductor debe quedar DISPONIBLE al finalizar el viaje.");
        }
    }

    @Test
    public void cancelarViajeRegistraMotivoYFechaFin() {
        Viaje viaje = new Viaje(clienteUsuario, servicio, origen, destino, new ArrayList<>());
        LocalDateTime ahora = LocalDateTime.now();

        viaje.cancelar(ahora, clienteUsuario, "Cambio de planes");

        assertEquals(EstadoViaje.CANCELADO, viaje.getEstadoActual(), "El estado debe cambiar a CANCELADO.");
        assertEquals("Cambio de planes", viaje.getMotivoCancelacion(), "Debe registrarse el motivo de cancelación.");
        assertEquals(ahora, viaje.getFechaHoraFin(), "Debe registrarse la fecha y hora fin.");
    }

    @Test
    public void rechazarViajeCambiaEstadoARechazado() {
        Viaje viaje = new Viaje(clienteUsuario, servicio, origen, destino, new ArrayList<>());
        LocalDateTime ahora = LocalDateTime.now();

        viaje.rechazar(ahora);

        assertEquals(EstadoViaje.RECHAZADO, viaje.getEstadoActual(), "El estado del viaje debe cambiar a RECHAZADO.");
    }

    @Test
    public void agregarRegistroNullLanzaExcepcion() {
        Viaje viaje = new Viaje(clienteUsuario, servicio, origen, destino, new ArrayList<>());

        assertThrows(IllegalArgumentException.class, () -> {
            viaje.agregarRegistroViaje(null);
        }, "Agregar un registro de viaje nulo debe lanzar IllegalArgumentException.");
    }
}
