package md.ui.impl.javafx;

import javafx.application.Application;
import javafx.stage.Stage;
import md.aplicacion.Aplicacion;
import md.ui.api.IInterfazUsuario;

public class JavaFXUserInterface extends Application implements IInterfazUsuario {

    @Override
    public void iniciar() {
        // Al invocar launch aquí, JavaFX arranca el ciclo de vida 
        // y llamará automáticamente al método start(Stage) de esta misma clase.
        Application.launch(JavaFXUserInterface.class);
    }

    @Override
    public void detener() {
        System.out.println("Cerrando interfaz gráfica...");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Corregido el error de tipeo (Aplicacion en lugar de Aplcacion)
        Aplicacion mainApp = new Aplicacion();
        mainApp.start(primaryStage);
    }
}