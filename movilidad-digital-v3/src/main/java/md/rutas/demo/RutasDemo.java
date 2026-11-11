package md.rutas.demo;

import md.rutas.api.IRutasService;
import md.rutas.impl.RutasServiceImpl;
import java.util.Locale;

public class RutasDemo {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   DEMOSTRACIÓN DE CÁLCULO DE RUTAS - MADRYN");
        System.out.println("=========================================");

        // 1. Instanciamos el servicio real de rutas que acabamos de crear
        IRutasService rutasService = new RutasServiceImpl();

        // 2. Definimos par de puntos de prueba en Puerto Madryn
        // Municipalidad (-42.7692, -65.0385) a Terminal de Ómnibus (-42.7710, -65.0700)
        String origenNombre = "Municipalidad de Puerto Madryn";
        double latOrigen = -42.7692;
        double lonOrigen = -65.0385;

        String destinoNombre = "Terminal de Ómnibus";
        double latDestino = -42.7710;
        double lonDestino = -65.0700;

        System.out.println("Origen: " + origenNombre + " (" + latOrigen + ", " + lonOrigen + ")");
        System.out.println("Destino: " + destinoNombre + " (" + latDestino + ", " + lonDestino + ")");
        System.out.println("-----------------------------------------");

        // 3. Ejecutamos los métodos del servicio
        String detalleRuta = rutasService.calcularRuta(latOrigen, lonOrigen, latDestino, lonDestino);
        double distanciaKm = rutasService.calcularDistanciaKm(latOrigen, lonOrigen, latDestino, lonDestino);
        double tarifaEstimada = calcularTarifaSimulada(distanciaKm, "Standard");

        // 4. Mostramos los resultados por consola
        System.out.println("Resultado del Servicio:");
        System.out.println(" -> " + detalleRuta);
        System.out.println(String.format(Locale.US, " -> Distancia total: %.2f km", distanciaKm));
        System.out.println(String.format(Locale.US, " -> Tarifa estimada (Standard): $%.2f", tarifaEstimada));
        System.out.println("=========================================");
    }

    private static double calcularTarifaSimulada(double km, String tipoServicio) {
        double base = 800.0;
        double costoPorKm = 350.0;
        double multiplicador = 1.0;

        switch (tipoServicio) {
            case "Económico": multiplicador = 0.85; break;
            case "Confort": multiplicador = 1.20; break;
            case "VIP": multiplicador = 1.50; break;
            default: multiplicador = 1.0; break;
        }

        return (base + (km * costoPorKm)) * multiplicador;
    }
}