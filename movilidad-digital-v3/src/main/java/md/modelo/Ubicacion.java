package md.modelo;

public class Ubicacion {
    private double latitud;
    private double longitud;
    private String direccion;

    // Constructor 1: Recibe (latitud, longitud, direccion)
    public Ubicacion(double latitud, double longitud, String direccion) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.direccion = direccion;
    }

    // Constructor 2: Recibe (direccion, latitud, longitud) 
    // ¡Para que coincida con el orden que prefieras usar en tus clases!
    public Ubicacion(String direccion, double latitud, double longitud) {
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
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
        double dLat = this.latitud - otra.getLatitud();
        double dLon = this.longitud - otra.getLongitud();
        return Math.sqrt(dLat * dLat + dLon * dLon) * 111.0; // aprox. km por grado
    }
}