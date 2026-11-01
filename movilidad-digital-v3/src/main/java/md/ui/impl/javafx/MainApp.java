
package md.ui.impl.javafx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

	    @Override
	    public void start(Stage primaryStage) {
	        try {
	            // Asegúrate de que la ruta coincida con la ubicación de tu archivo FXML
	            Parent root = FXMLLoader.load(getClass().getResource("/ui/javafx/vistas/VentanaPrincipal.fxml"));
	            
	            primaryStage.setTitle("Movilidad Digital");
	            primaryStage.setScene(new Scene(root));
	            primaryStage.show();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    public static void main(String[] args) {
	        launch(args);
	    }
	}

