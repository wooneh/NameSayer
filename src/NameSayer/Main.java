package NameSayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Main class for the NameSayer application.
 * @author Samantha Woon and Clayton Lan, Team 34
 */
public class Main extends Application {
	public static final String NAMES_CORPUS = "names";
	public static final String CLASSES ="classes";
	public static final String TEMP = "temp";

	public static String SHELL = "/bin/bash";
	public static String COMMAND = "-c";
	public static String OS = "Linux";

	@Override
	public void start(Stage stage) throws Exception {
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			SHELL = "CMD";
			COMMAND = "/C";
			OS = "Windows";
		}

		stage.setTitle("NameSayer");
		stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/NameSayer/view/HomeScreen.fxml"))));
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
