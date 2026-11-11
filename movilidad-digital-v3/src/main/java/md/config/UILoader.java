package md.config;

import md.logica.GestorMovilidad;
import md.ui.api.IInterfazUsuario;
import md.ui.impl.cli.InterfazUsuario;
import md.ui.impl.javafx.JavaFXUserInterface;

public class UILoader {

    public static IInterfazUsuario crearInterfaz(GestorMovilidad gestor) {
        // Llamamos al método estático de la guía
        String tipoUi = CargaParametros.ui();

        if (tipoUi.equals("cli")) {
            System.out.println("Fábrica UILoader: Seleccionada interfaz de Consola (CLI)");
            return new InterfazUsuario(gestor);
        } else {
            System.out.println("Fábrica UILoader: Seleccionada interfaz Gráfica (JavaFX)");
            return new JavaFXUserInterface();
        }
    }
}