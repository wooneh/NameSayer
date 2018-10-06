package NameSayer.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Controller for the Home screen. Welcome screen when the user starts up NameSayer.
 */
public class HomeScreen {
	@FXML Button startButton;
	@FXML Button singleButton;
	@FXML Button helpButton;

	public void initialize() {
    	startButton.setOnAction(event -> {
    		try {
				startButton.getScene().setRoot(new FXMLLoader(getClass().getResource("/NameSayer/view/InputNames.fxml")).load());
			} catch (IOException e) {
    			e.printStackTrace();
			}
    	});

    	singleButton.setOnAction(event -> {
			try {
				singleButton.getScene().setRoot(new FXMLLoader(getClass().getResource("/NameSayer/view/SingleInput.fxml")).load());
			} catch (IOException e) {
				e.printStackTrace();
			}
		});

		helpButton.setOnAction(event -> {
			try {
				Stage stage = new Stage();
				stage.setScene(new Scene(FXMLLoader.load(getClass().getResource("/NameSayer/view/Help.fxml"))));
				stage.show();
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}
}