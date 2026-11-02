package md.ui.api;

/**
 * Contrato para la gestión y renderizado del menú principal 
 * de interacción con el sistema.
 */
public interface IMenuPrincipal {

    /**
        * Muestra las opciones principales disponibles al usuario.
        */
    void mostrarMenu();

    /**
        * Procesa la opción seleccionada por el usuario.
        * @param opcion código o número de la opción elegida.
        */
    void procesarOpcion(int opcion);
}