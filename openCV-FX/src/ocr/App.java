package ocr;

import org.opencv.core.Core;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

	static {System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	@Override
	public void start(Stage stage) {
		
		ViewOcr widok = new ViewOcr();
		ControllerOcr kontroler = new ControllerOcr();
		kontroler.setWidok(widok);
		Scene scene = new Scene(widok.getPane(),500,400);
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}
