package md.aplicacion;

import md.rutas.api.IRutasService;
import md.rutas.impl.RutasServiceImpl; // <--- Import correcto de la implementación

public class ContextoMovilidad {
    
    private static ContextoMovilidad instancia;
    
    // Servicios globales
    private IRutasService rutaService;
    
    // Constructor privado (patrón Singleton)
    private ContextoMovilidad() {
        this.rutaService = new RutasServiceImpl(); // <--- Corregido aquí
        // Aquí podrías inicializar listas de viajes, conductores, etc.
    }
    
    public static synchronized ContextoMovilidad getInstancia() {
        if (instancia == null) {
            instancia = new ContextoMovilidad();
        }
        return instancia;
    }
    
    public IRutasService getRutaService() {
        return rutaService;
    }
}