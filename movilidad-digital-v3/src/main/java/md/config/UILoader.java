package md.config;

import md.ui.api.IInterfazUsuario;
import md.ui.impl.cli.InterfazUsuario;
import md.ui.impl.javafx.JavaFXUserInterface;
import md.logica.*;

public class UILoader {

    public static IInterfazUsuario crearInterfaz(GestorMovilidad gestor) {
        // Usamos la instancia o método de tu clase Configuracion existente
        Configuracion config = new Configuracion("config.properties"); // O si es estático, lo llamás según corresponda
        String tipoUi = config.getTipoUI();

        // Si no está definido en el archivo, por defecto levantamos "cli"
        if (tipoUi == null) {
            tipoUi = "cli";
        }

        tipoUi = tipoUi.trim().toLowerCase();

        if (tipoUi.equals("javafx")) {
            return new JavaFXUserInterface();
        } else {
            return new InterfazUsuario(gestor);
        }
    }
}