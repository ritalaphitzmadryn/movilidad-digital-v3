package md.ui.impl.javafx;

import md.ui.api.IInterfazUsuario;
import javafx.application.Application;

public class JavaFXUserInterface implements IInterfazUsuario {

    @Override
    public void iniciar() {
        // se lanza aplicación JavaFX, o llamando a MainApp o lanzando el launch() de JavaFX
        MainApp.launch(new String[0]); 
    }
    @Override
    public void detener() {
        // Lógica para cerrar o limpiar recursos de JavaFX si hace falta
        // O dejarlo vacío si el cierre lo maneja directamente el Stage/Ventana
    }
}