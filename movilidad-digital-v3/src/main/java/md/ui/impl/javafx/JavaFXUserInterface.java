package md.ui.impl.javafx;

import javafx.application.Application;
import javafx.stage.Stage;
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
        // Aquí delegamos la inicialización de la ventana a tu MainApp o lógica existente
        MainApp mainApp = new MainApp();
        mainApp.start(primaryStage);
    }
}