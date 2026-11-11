package md.rutas.impl;

import md.rutas.api.IRutasService;

public class RutasServiceImpl implements IRutasService {

    @Override
    public String calcularRuta(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
        System.out.println("Calculando ruta óptima en RutasServiceImpl...");
        double distancia = calcularDistanciaKm(latOrigen, lonOrigen, latDestino, lonDestino);
        
        // Lógica de simulación o detalle de la ruta
        return "Ruta óptima desde (" + latOrigen + ", " + lonOrigen + ") " +
               "hasta (" + latDestino + ", " + lonDestino + "). " +
               "Distancia total estimada: " + distancia + " km.";
    }

    @Override
    public double calcularDistanciaKm(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
        // Cálculo de distancia utilizando la fórmula de Haversine
        double dLat = Math.toRadians(latDestino - latOrigen);
        double dLon = Math.toRadians(lonDestino - lonOrigen);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(latOrigen)) * Math.cos(Math.toRadians(latDestino)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
                   
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        double radioTierraKm = 6371.0;
        double distancia = radioTierraKm * c;
        
        // Retorna la distancia redondeada a dos decimales
        return Math.round(distancia * 100.0) / 100.0;
    }
}