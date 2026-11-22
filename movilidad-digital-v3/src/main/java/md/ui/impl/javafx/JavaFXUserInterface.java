package md.ui.impl.javafx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import md.aplicacion.Aplicacion;
import md.logica.GestorMovilidad;
import md.ui.api.IInterfazUsuario;

public class JavaFXUserInterface implements IInterfazUsuario {

    private static Stage primaryStageStatic;

    @Override
    public void iniciar() {
        // Asegura que el gestor ya inicializado en Aplicacion esté disponible
        GestorMovilidad gestor = Aplicacion.getGestor();
        if (gestor == null) {
            System.err.println("Advertencia: Aplicacion.getGestor() es null al iniciar JavaFX.");
        }
        Application.launch(AppFX.class);
    }

    @Override
    public void detener() {
        System.out.println("Cerrando interfaz gráfica...");
        Platform.exit();
    }

    public static class AppFX extends Application {
        @Override
        public void start(Stage primaryStage) throws Exception {
            JavaFXUserInterface.primaryStageStatic = primaryStage;
            cargarVistaInicial("/ui/javafx/vistas/Login.fxml", "Movilidad Digital - Iniciar Sesión");
        }
    }

    public static void cambiarVista(String fxmlPath, String tituloVentana) {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(JavaFXUserInterface.class.getResource(fxmlPath));
                Parent root = loader.load();

                if (primaryStageStatic != null) {
                    primaryStageStatic.setTitle(tituloVentana);
                    primaryStageStatic.setScene(new Scene(root));
                    primaryStageStatic.show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void cargarVistaInicial(String fxmlPath, String titulo) {
        try {
            FXMLLoader loader = new FXMLLoader(JavaFXUserInterface.class.getResource(fxmlPath));
            Parent root = loader.load();

            primaryStageStatic.setTitle(titulo);
            primaryStageStatic.setScene(new Scene(root));
            primaryStageStatic.setResizable(false);
            primaryStageStatic.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}