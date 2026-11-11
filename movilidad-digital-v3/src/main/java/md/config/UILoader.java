package md.config;

import md.logica.GestorMovilidad;
import md.ui.api.IInterfazUsuario;
import md.ui.impl.cli.InterfazUsuario;
import md.ui.impl.javafx.JavaFXUserInterface;

public class UILoader {

    public static IInterfazUsuario crearInterfaz(GestorMovilidad gestor) {
        // Obtenemos el parámetro normalizado desde CargaParametros
        String tipoUi = CargaParametros.ui();

        if (tipoUi == null || tipoUi.isBlank()) {
            tipoUi = "javafx"; // Por defecto si está vacío
        }

        tipoUi = tipoUi.trim().toLowerCase();

        System.out.println("Fábrica UILoader: Configuración leída de app.ui = [" + tipoUi + "]");

        if (tipoUi.equals("cli")) {
            System.out.println("-> Seleccionada interfaz de Consola (CLI)");
            return new InterfazUsuario(gestor);
        } else if (tipoUi.equals("javafx")) {
            System.out.println("-> Seleccionada interfaz Gráfica (JavaFX)");
            return new JavaFXUserInterface();
        } else {
            throw new IllegalStateException("Tipo de interfaz no reconocido en application.properties: '" + tipoUi + "'. Debe ser 'cli' o 'javafx'.");
        }
    }
}