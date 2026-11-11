package md.ui.api;

/**
 * IInterfazUsuario funciona como el UIEngine:
Contrato general para cualquier tipo de interfaz de usuario 
 * (CLI, JavaFX, Web, etc.) .
 */

public interface IInterfazUsuario {
    
    /**
        * Inicia y despliega la interfaz de usuario.
        */
    void iniciar();

    /**
        * Detiene o cierra la interfaz de usuario de forma limpia.
        */
    void detener();
}

