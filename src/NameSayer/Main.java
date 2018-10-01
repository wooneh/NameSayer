package NameSayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

	@Override
	public void start(Stage stage) throws Exception {
		Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/NameSayer/view/HomeScreen.fxml")));
		stage.setTitle("NameSayer");
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
