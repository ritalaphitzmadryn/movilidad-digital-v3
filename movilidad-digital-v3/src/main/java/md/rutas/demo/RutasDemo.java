package md.rutas.demo;

import md.rutas.api.IRutasService;
import java.util.Locale;

public class RutasDemo {

    public static void main(String[] args) {
        System.out.println("=========================================");
        System.out.println("   DEMOSTRACIÓN DE CÁLCULO DE RUTAS - MADRYN");
        System.out.println("=========================================");

        // 1. Instanciamos el servicio de rutas (puedes reemplazar con tu implementación real: new RutasServiceImpl())
        IRutasService rutasService = new IRutasService() {
            @Override
            public String calcularRuta(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
                // Lógica provisional de simulación de tramos para la demo
                double distancia = calcularDistanciaKm(latOrigen, lonOrigen, latDestino, lonDestino);
                return String.format(Locale.US, "Ruta óptima calculada. Tramos principales por Av. Gral. Roca y Bv. Brown. Distancia: %.2f km.", distancia);
            }

            @Override
            public double calcularDistanciaKm(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
                // Fórmula de Haversine simplificada o cálculo euclidiano aproximado para pruebas locales
                final int R = 6371; // Radio de la Tierra en km
                double latDistance = Math.toRadians(latDestino - latOrigen);
                double lonDistance = Math.toRadians(lonDestino - lonLon(lonOrigen, lonDestino)); // corrección simple
                
                double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                        + Math.cos(Math.toRadians(latOrigen)) * Math.cos(Math.toRadians(latDestino))
                        * Math.sin(latDistance / 2) * Math.sin(latDistance / 2);
                
                // Aproximación lineal rápida en grados para Chubut (1 grado lat ~= 111km)
                double dLat = (latDestino - latOrigen) * 111.0;
                double dLon = (lonDestino - lonOrigen) * 111.0 * Math.cos(Math.toRadians((latOrigen + latDestino) / 2));
                
                return Math.sqrt(dLat * dLat + dLon * dLon);
            }
            
            private double lonLon(double o, double d) { return d; }
        };

        // 2. Definimos par de puntos de prueba en Puerto Madryn
        // Ejemplo: Municipalidad (-42.7692, -65.0385) a Terminal de Ómnibus (-42.7710, -65.0700)
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