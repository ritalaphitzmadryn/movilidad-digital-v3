package md.rutas.api;

public interface IRutasService {

    /**
     * Calcula y retorna los detalles de la ruta entre un origen y un destino.
     */
    String calcularRuta(double latOrigen, double lonOrigen, double latDestino, double lonDestino);

    /**
     * Calcula la distancia estimada en kilómetros entre dos coordenadas.
     */
    double calcularDistanciaKm(double latOrigen, double lonOrigen, double latDestino, double lonDestino);
}