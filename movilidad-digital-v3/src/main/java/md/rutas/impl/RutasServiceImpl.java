package md.rutas.impl;

import md.rutas.api.IRutasService;

public class RutasServiceImpl implements IRutasService {

    public RutasServiceImpl() {
        System.out.println("Servicio de rutas inicializado (Modo geométrico optimizado).");
    }

    @Override
    public double calcularDistanciaKm(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
        // --- CÁLCULO GEOMÉTRICO (Haversine) ---
        double dLat = Math.toRadians(latDestino - latOrigen);
        double dLon = Math.toRadians(lonDestino - lonOrigen);
        
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(latOrigen)) * Math.cos(Math.toRadians(latDestino)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);
                   
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double radioTierraKm = 6371.0;
        double distancia = radioTierraKm * c;
        
        return Math.round(distancia * 100.0) / 100.0;
    }

    @Override
    public String calcularRuta(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
        // Devuelve el detalle estimado basado en la distancia calculada
        double distancia = calcularDistanciaKm(latOrigen, lonOrigen, latDestino, lonDestino);
        return "Ruta urbana simulada - Distancia estimada: " + distancia + " km.";
    }
}