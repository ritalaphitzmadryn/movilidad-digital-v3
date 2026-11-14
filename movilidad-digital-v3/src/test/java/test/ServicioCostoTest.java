package test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import md.modelo.CategoriaVehiculo;
import md.modelo.Servicio;
import md.modelo.TipoServicio;
import md.modelo.TipoVehiculo;

public class ServicioCostoTest {

    private Servicio servicio;

    @BeforeEach
    public void setUp() {
        // Tarifa base: 100.0, Precio/km: 50.0, Precio/minuto: 10.0
        servicio = new Servicio(
            "Servicio Estándar",
            TipoServicio.PASAJEROS,
            TipoVehiculo.AUTO,
            CategoriaVehiculo.ESTANDAR,
            100.0,
            50.0,
            10.0
        );
    }

    @Test
    public void calcularCostoConDistanciaYTiempoCeroDevuelveTarifaBase() {
        // 100.0 + (0 * 50.0) + (0 * 10.0) = 100.0
        double costo = servicio.calcularCosto(0.0, 0.0);
        assertEquals(100.0, costo, 0.001, "El costo con 0 km y 0 minutos debe ser igual a la tarifa base.");
    }

    @Test
    public void calcularCostoSoloConKilometros() {
        // 100.0 + (5.0 * 50.0) + (0 * 10.0) = 350.0
        double costo = servicio.calcularCosto(5.0, 0.0);
        assertEquals(350.0, costo, 0.001, "El costo con 5 km y 0 min debe ser 350.0.");
    }

    @Test
    public void calcularCostoSoloConMinutos() {
        // 100.0 + (0 * 50.0) + (15.0 * 10.0) = 250.0
        double costo = servicio.calcularCosto(0.0, 15.0);
        assertEquals(250.0, costo, 0.001, "El costo con 0 km y 15 min debe ser 250.0.");
    }

    @Test
    public void calcularCostoCompletoConKmYMinutos() {
        // 100.0 + (10.0 * 50.0) + (20.0 * 10.0) = 800.0
        double costo = servicio.calcularCosto(10.0, 20.0);
        assertEquals(800.0, costo, 0.001, "El costo con 10 km y 20 min debe ser 800.0.");
    }

    @Test
    public void calcularCostoConValoresDecimales() {
        // 100.0 + (2.5 * 50.0) + (12.5 * 10.0) = 350.0
        double costo = servicio.calcularCosto(2.5, 12.5);
        assertEquals(350.0, costo, 0.001, "El costo con decimales debe calcularse correctamente.");
    }
}
