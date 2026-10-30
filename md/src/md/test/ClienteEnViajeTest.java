package md.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import md.modelo.CalificacionViaje;
import md.modelo.CategoriaVehiculo;
import md.modelo.Cliente;
import md.modelo.EstadoViaje;
import md.modelo.Servicio;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;
import md.modelo.Ubicacion;
import md.modelo.Usuario;
import md.modelo.Viaje;

public class ClienteEnViajeTest {

    private Usuario usuario;
    private Cliente cliente;
    private Servicio servicio;
    private Ubicacion origen;
    private Ubicacion destino;

    @BeforeEach
    public void setUp() {
        usuario = new Usuario("U001", "Juan Perez", "2804123456", "juan@gmail.com");
        cliente = usuario.getCliente();
        
        servicio = new Servicio(
            "Servicio Estándar",
            TipoServicio.PASAJEROS,
            TipoVehiculo.AUTO,
            CategoriaVehiculo.ESTANDAR,
            100.0,
            50.0,
            10.0
        ); 
        
        // Instanciación ajustada a la firma Ubicacion(double, double, String)
        origen = new Ubicacion(-42.769, -65.038, "Av. Roca 100");
        destino = new Ubicacion(-42.775, -65.045, "San Martín 500");
    }

    private Viaje crearViajeConEstado(EstadoViaje estado) {
        Viaje viaje = new Viaje(usuario, servicio, origen, destino, new ArrayList<>());
        LocalDateTime ahora = LocalDateTime.now();

        switch (estado) {
            case ACEPTADO:
                viaje.aceptar(ahora, usuario);
                break;
            case INICIADO:
                viaje.iniciar(ahora);
                break;
            case FINALIZADO:
                viaje.finalizar(ahora, CalificacionViaje.EXCELENTE, CalificacionViaje.EXCELENTE);
                break;
            case CANCELADO:
                viaje.cancelar(ahora, usuario, "Cancelado por prueba");
                break;
            case RECHAZADO:
                viaje.rechazar(ahora);
                break;
            case SOLICITADO:
            default:
                viaje.solicitar(ahora);
                break;
        }

        return viaje;
    }

    @Test
    public void clienteNuevoSinViajesNoEstaEnViaje() {
        assertFalse(cliente.enViaje(), "Un cliente sin viajes registrados no debe estar en viaje.");
    }

    @Test
    public void clienteConViajeSolicitadoEstaEnViaje() {
        Viaje viaje = crearViajeConEstado(EstadoViaje.SOLICITADO);
        cliente.agregarViaje(viaje);

        assertTrue(cliente.enViaje(), "El cliente debe estar 'en viaje' si tiene un viaje en estado SOLICITADO.");
    }

    @Test
    public void clienteConViajeAceptadoEstaEnViaje() {
        Viaje viaje = crearViajeConEstado(EstadoViaje.ACEPTADO);
        cliente.agregarViaje(viaje);

        assertTrue(cliente.enViaje(), "El cliente debe estar 'en viaje' si tiene un viaje en estado ACEPTADO.");
    }

    @Test
    public void clienteConViajeIniciadoEstaEnViaje() {
        Viaje viaje = crearViajeConEstado(EstadoViaje.INICIADO);
        cliente.agregarViaje(viaje);

        assertTrue(cliente.enViaje(), "El cliente debe estar 'en viaje' si tiene un viaje en estado INICIADO.");
    }

    @Test
    public void clienteConViajeFinalizadoNoEstaEnViaje() {
        Viaje viaje = crearViajeConEstado(EstadoViaje.FINALIZADO);
        cliente.agregarViaje(viaje);

        assertFalse(cliente.enViaje(), "El cliente no debe estar 'en viaje' si su viaje ya está FINALIZADO.");
    }

    @Test
    public void clienteConViajeCanceladoNoEstaEnViaje() {
        Viaje viaje = crearViajeConEstado(EstadoViaje.CANCELADO);
        cliente.agregarViaje(viaje);

        assertFalse(cliente.enViaje(), "El cliente no debe estar 'en viaje' si su viaje fue CANCELADO.");
    }

    @Test
    public void clienteConHistorialDeViajesYUnoActivoEstaEnViaje() {
        Viaje viajeViejo = crearViajeConEstado(EstadoViaje.FINALIZADO);
        cliente.agregarViaje(viajeViejo);

        Viaje viajeNuevo = crearViajeConEstado(EstadoViaje.INICIADO);
        cliente.agregarViaje(viajeNuevo);

        assertTrue(cliente.enViaje(), "Si tiene al menos un viaje activo entre varios finalizados, debe retornar true.");
    }
}