package NameSayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

public class Main extends Application {
	public static final File NAMES_CORPUS = new File("names");
	public static final File CLASSES = new File("classes");
	public static final File BAD_RATINGS = new File("BadRatings.txt");

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

		Scene scene = new Scene(FXMLLoader.load(getClass().getResource("/NameSayer/view/HomeScreen.fxml")));
		stage.setTitle("NameSayer");
		stage.setScene(scene);
		stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
