
package md.aplicacion;

import md.rutas.api.*;
import md.rutas.impl.*;


public class ContextoMovilidad {
    
    private static ContextoMovilidad instancia;
    
    // Servicios globales
    private IRutasService rutaService;
    
    // Constructor privado (patrón Singleton)
    private ContextoMovilidad() {
        this.rutaService = new IRutasServiceImpl();
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