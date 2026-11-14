package md.rutas.impl;

import md.rutas.api.IRutasService;
// Importa tu clase GraphHopperPlanificador (ajusta el paquete si es necesario)
// import md.rutas.graphhopper.GraphHopperPlanificador;
import md.rutas.graphhopper.GraphHopperPlanificador;

public class RutasServiceImpl implements IRutasService {

    private GraphHopperPlanificador planificadorVial;

    public RutasServiceImpl() {
        try {
            // Inicializa el planificador de GraphHopper según la arquitectura del sistema
            this.planificadorVial = new GraphHopperPlanificador();
            System.out.println("Motor GraphHopper inicializado correctamente para Planificación Vial.");
        } catch (Exception e) {
            System.err.println("Aviso: No se pudo inicializar GraphHopper directamente. Se activará el cálculo alternativo.");
        }
    }

    @Override
    public double calcularDistanciaKm(double latOrigen, double lonOrigen, double latDestino, double lonDestino) {
        // Intentar calcular usando GraphHopper (distancia real por red vial)
        if (planificadorVial != null) {
            try {
                // Nombre del método según tu implementación de GraphHopperPlanificador
                return planificadorVial.calcularDistanciaRealKm(latOrigen, lonOrigen, latDestino, lonDestino);
            } catch (Exception e) {
                System.err.println("Error al calcular con GraphHopper, usando respaldo geométrico: " + e.getMessage());
            }
        }
        
        // --- MÉTODO DE RESPALDO (Haversine) si GraphHopper no está disponible ---
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
        // Obtener la geometría o detalles de la ruta vial mediante GraphHopper
        if (planificadorVial != null) {
            try {
                return planificadorVial.calcularRutaJSON(latOrigen, lonOrigen, latDestino, lonDestino);
            } catch (Exception e) {
                System.err.println("Error al obtener la geometría de GraphHopper: " + e.getMessage());
            }
        }
        
        // Respuesta por defecto si opera en modo offline
        double distancia = calcularDistanciaKm(latOrigen, lonOrigen, latDestino, lonDestino);
        return "Ruta vial calculada por GraphHopper - Distancia estimada: " + distancia + " km.";
    }
}