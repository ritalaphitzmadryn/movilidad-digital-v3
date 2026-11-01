package md.modelo;

public class Ubicacion {
    private double latitud;
    private double longitud;
    private String direccion;

    public Ubicacion(double latitud, double longitud, String direccion) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.direccion = direccion;
    }

    public double getLatitud() {
        return latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public String getDireccion() {
        return direccion;
    }

    public double calcularDistancia(Ubicacion otra) {
        if (otra == null) return 0.0;
        // Distancia simplificada para simulación en km
        double dLat = this.latitud - otra.getLatitud();
        double dLon = this.longitud - otra.getLongitud();
        return Math.sqrt(dLat * dLat + dLon * dLon) * 111.0; // aprox. km por grado
    }
}