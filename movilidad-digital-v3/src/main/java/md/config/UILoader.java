package md.config;

import md.logica.GestorMovilidad;
import md.ui.api.IInterfazUsuario;
import md.ui.impl.cli.InterfazUsuario;
import md.ui.impl.javafx.MainApp;

public class UILoader {

    public static IInterfazUsuario crearInterfaz(GestorMovilidad gestor) {
        Configuracion config = new Configuracion("config.properties");
        String tipoUi = config.getTipoUI();

        if (tipoUi == null || tipoUi.isBlank()) {
            tipoUi = "javafx"; // Por defecto si no encuentra nada
        }

        tipoUi = tipoUi.trim().toLowerCase();

        if (tipoUi.equals("cli")) {
            System.out.println("Fábrica UILoader: Seleccionada interfaz de Consola (CLI)");
            return new InterfazUsuario(gestor);
        } else {
            System.out.println("Fábrica UILoader: Seleccionada interfaz Gráfica (JavaFX)");
            // Retornamos la implementación que lanza JavaFX de forma segura
            return new IInterfazUsuario() {
                @Override
                public void iniciar() {
                    javafx.application.Application.launch(MainApp.class);
                }

                @Override
                public void detener() {
                    System.out.println("Cerrando aplicación JavaFX...");
                }
            };
        }
    }
}