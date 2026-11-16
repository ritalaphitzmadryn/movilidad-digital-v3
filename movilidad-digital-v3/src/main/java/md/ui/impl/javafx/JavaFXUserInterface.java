package md.ui.impl.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import md.ui.api.IInterfazUsuario;

public class JavaFXUserInterface implements IInterfazUsuario {

    @Override
    public void iniciar() {
        // Lanza la aplicación JavaFX interna sin que esta clase herede de Application
        Application.launch(AppFX.class);
    }

    @Override
    public void detener() {
        System.out.println("Cerrando interfaz gráfica...");
        Platform.exit();
    }

    // Clase interna que maneja el ciclo de vida de JavaFX
    public static class AppFX extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/ui/javafx/vistas/Login.fxml"));
            Parent root = loader.load();

            primaryStage.setTitle("Movilidad Digital - Iniciar Sesión");
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.show();
        }
    }
}