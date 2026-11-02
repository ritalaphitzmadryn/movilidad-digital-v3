package md.ui.api;

/**
 * Contrato general para cualquier tipo de interfaz de usuario 
 * (CLI, JavaFX, Web, etc.) en el sistema Movilidad Digital.
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

