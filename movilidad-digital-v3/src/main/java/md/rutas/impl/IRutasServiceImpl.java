package md.rutas.impl;

import md.rutas.api.*;

public class IRutasServiceImpl implements IRutasService {

    @Override
    public String calcularRuta(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
        System.out.println("Calculando ruta en RutasServiceImpl...");
        // Lógica de cálculo o simulación de trayecto
        return "Ruta óptima desde (" + latOrigen + ", " + lonOrigen + ") hasta (" + latDestino + ", " + lonDestino + ")";
    }

    @Override
    public double calcularDistanciaKm(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
        // Cálculo aproximado de distancia utilizando la fórmula de Haversine
        double dLat = Math.toRadians(latDestino - latOrigen);
        double dLon = Math.toRadians(lonDestino - lonOrigen);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(latOrigen)) * Math.cos(Math.toRadians(latDestino)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        double distancia = 6371 * c; // Radio aproximado de la Tierra en km
        return Math.round(distancia * 100.0) / 100.0;
    }
}